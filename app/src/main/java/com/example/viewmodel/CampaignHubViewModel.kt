package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CampaignRepository
import com.example.data.CampaignSave
import com.example.data.TheaterWorldData
import com.example.model.CampaignState
import com.example.model.Difficulty
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class CampaignHubViewModel(
    private val repository: CampaignRepository,
    private val currentUserId: String,
    private val displayName: String?
) : ViewModel() {

    val campaignsState: StateFlow<UiState<List<CampaignSave>>> = repository.observeCampaigns(currentUserId)
        .map<List<CampaignSave>, UiState<List<CampaignSave>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing campaigns", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load saved campaigns"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    private val _busyMessage = MutableStateFlow<String?>(null)
    val busyMessage: StateFlow<String?> = _busyMessage.asStateFlow()

    private val _errorBanner = MutableStateFlow<String?>(null)
    val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(currentUserId, displayName)
        }
    }

    fun clearError() {
        _errorBanner.value = null
    }

    fun createAndLaunchCampaign(
        nationId: String,
        campaignName: String,
        difficulty: Difficulty,
        onLaunched: (CampaignState) -> Unit
    ) {
        viewModelScope.launch {
            _busyMessage.value = "Deploying High Command..."
            _errorBanner.value = null
            val initialState = TheaterWorldData.createInitialCampaignState(
                playerNationId = nationId,
                campaignName = campaignName.trim().ifBlank { "${TheaterWorldData.nations[nationId]?.name ?: nationId} 1936" },
                difficulty = difficulty
            )
            val result = repository.createCampaign(initialState)
            _busyMessage.value = null
            result.onSuccess { newId ->
                onLaunched(initialState.copy(campaignId = newId))
            }.onFailure { err ->
                _errorBanner.value = err.localizedMessage ?: "Unable to create cloud campaign"
            }
        }
    }

    fun loadAndLaunchCampaign(campaignId: String, onLaunched: (CampaignState) -> Unit) {
        viewModelScope.launch {
            _busyMessage.value = "Decrypting Theater Orders..."
            _errorBanner.value = null
            val result = repository.loadCampaign(currentUserId, campaignId)
            _busyMessage.value = null
            result.onSuccess { state ->
                onLaunched(state)
            }.onFailure { err ->
                _errorBanner.value = err.localizedMessage ?: "Unable to load campaign"
            }
        }
    }

    fun deleteCampaign(campaignId: String) {
        viewModelScope.launch {
            val result = repository.deleteCampaign(currentUserId, campaignId)
            result.onFailure { err ->
                _errorBanner.value = err.localizedMessage ?: "Failed to delete campaign"
            }
        }
    }

    private companion object {
        const val TAG = "CampaignHubVM"
    }
}
