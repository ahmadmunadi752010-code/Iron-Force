package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.CampaignRepository
import com.example.data.attemptAutoSignIn
import com.example.model.CampaignState
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CampaignHubScreen
import com.example.ui.screens.TheaterScreen
import com.example.ui.theme.IronFrontTheme
import com.example.viewmodel.CampaignHubViewModel
import com.example.viewmodel.GameViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IronFrontTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = CredentialManager.create(context),
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onUnauthenticated = {},
                scope = this
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
    } else {
        key(user.uid) {
            AuthenticatedTheaterRoot(
                currentUserId = user.uid,
                displayName = user.displayName,
                onSignedOut = { currentUser = null }
            )
        }
    }
}

@Composable
private fun AuthenticatedTheaterRoot(
    currentUserId: String,
    displayName: String?,
    onSignedOut: () -> Unit
) {
    var activeCampaign by remember { mutableStateOf<CampaignState?>(null) }

    val hubViewModel: CampaignHubViewModel = viewModel(
        key = "hub_$currentUserId",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val dbId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                CampaignHubViewModel(
                    repository = CampaignRepository(db),
                    currentUserId = currentUserId,
                    displayName = displayName
                )
            }
        }
    )

    val loaded = activeCampaign
    if (loaded == null) {
        CampaignHubScreen(
            viewModel = hubViewModel,
            commanderName = displayName?.takeIf { it.isNotBlank() } ?: "Supreme Commander",
            onLaunchCampaign = { campaignState ->
                activeCampaign = campaignState
            },
            onSignedOut = onSignedOut
        )
    } else {
        key(loaded.campaignId) {
            val gameViewModel: GameViewModel = viewModel(
                key = "game_${currentUserId}_${loaded.campaignId}",
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val dbId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(dbId)
                        GameViewModel(
                            repository = CampaignRepository(db),
                            initialCampaign = loaded
                        )
                    }
                }
            )
            TheaterScreen(
                viewModel = gameViewModel,
                onExitToHub = { activeCampaign = null }
            )
        }
    }
}
