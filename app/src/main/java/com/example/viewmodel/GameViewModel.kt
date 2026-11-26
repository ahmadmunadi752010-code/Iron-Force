package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CampaignRepository
import com.example.data.TheaterWorldData
import com.example.engine.GrandStrategyEngine
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

enum class TheaterTab(val label: String) {
    MAP("Theater"),
    POLITICS("Politics"),
    PRODUCTION("Industry"),
    RESEARCH("Research"),
    DIPLOMACY("Diplomacy")
}

class GameViewModel(
    private val repository: CampaignRepository,
    initialCampaign: CampaignState
) : ViewModel() {

    private val _campaign = MutableStateFlow(initialCampaign)
    val campaign: StateFlow<CampaignState> = _campaign.asStateFlow()

    private val _gameSpeed = MutableStateFlow(GameSpeed.PAUSED)
    val gameSpeed: StateFlow<GameSpeed> = _gameSpeed.asStateFlow()

    private val _mapMode = MutableStateFlow(MapMode.POLITICAL)
    val mapMode: StateFlow<MapMode> = _mapMode.asStateFlow()

    private val _activeTab = MutableStateFlow(TheaterTab.MAP)
    val activeTab: StateFlow<TheaterTab> = _activeTab.asStateFlow()

    private val _selectedProvinceId = MutableStateFlow<String?>(
        TheaterWorldData.nations[initialCampaign.playerNationId]?.capitalProvinceId
    )
    val selectedProvinceId: StateFlow<String?> = _selectedProvinceId.asStateFlow()

    private val _isOrderingMoveFrom = MutableStateFlow<String?>(null)
    val isOrderingMoveFrom: StateFlow<String?> = _isOrderingMoveFrom.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var simJob: Job? = null

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setActiveTab(tab: TheaterTab) {
        _activeTab.value = tab
    }

    fun setMapMode(mode: MapMode) {
        _mapMode.value = mode
    }

    fun setGameSpeed(speed: GameSpeed) {
        _gameSpeed.value = speed
        simJob?.cancel()
        if (speed != GameSpeed.PAUSED) {
            simJob = viewModelScope.launch {
                while (isActive) {
                    delay(speed.tickDelayMs)
                    val prevStatus = _campaign.value.status
                    val next = GrandStrategyEngine.advanceHour(_campaign.value)
                    _campaign.value = next
                    if (prevStatus == "active" && next.status != "active") {
                        _gameSpeed.value = GameSpeed.PAUSED
                        saveCampaignToCloud(silent = true)
                        break
                    }
                }
            }
        }
    }

    fun togglePause() {
        if (_gameSpeed.value == GameSpeed.PAUSED) {
            setGameSpeed(GameSpeed.SPEED_2)
        } else {
            setGameSpeed(GameSpeed.PAUSED)
        }
    }

    fun onProvinceTapped(provinceId: String) {
        val fromId = _isOrderingMoveFrom.value
        if (fromId != null && fromId != provinceId) {
            issueDivisionMove(fromId, provinceId)
            _isOrderingMoveFrom.value = null
            _selectedProvinceId.value = provinceId
        } else {
            _isOrderingMoveFrom.value = null
            _selectedProvinceId.value = provinceId
        }
    }

    fun startMoveOrderFrom(provinceId: String) {
        val hasUnits = _campaign.value.divisions.any {
            it.provinceId == provinceId && it.nationId == _campaign.value.playerNationId
        }
        if (!hasUnits) {
            _toastMessage.value = "No friendly divisions stationed here."
            return
        }
        _isOrderingMoveFrom.value = provinceId
        _toastMessage.value = "Tap an adjacent province to march or attack."
    }

    fun cancelMoveOrder() {
        _isOrderingMoveFrom.value = null
    }

    fun issueDivisionMove(fromProvinceId: String, toProvinceId: String) {
        val state = _campaign.value
        val fromProv = TheaterWorldData.provinceMap[fromProvinceId] ?: return
        if (toProvinceId !in fromProv.neighbors) {
            _toastMessage.value = "Target province is not adjacent."
            return
        }
        val targetOwner = state.provinceOwners[toProvinceId] ?: return
        val canEnter = targetOwner == state.playerNationId ||
            state.diplomacyStates[targetOwner] == DiplomacyStatus.WAR ||
            state.diplomacyStates[targetOwner] == DiplomacyStatus.ALLIANCE
        if (!canEnter) {
            val nationName = TheaterWorldData.nations[targetOwner]?.name ?: targetOwner
            _toastMessage.value = "Not at war with $nationName. Justify & Declare War in Diplomacy first."
            return
        }

        var movedCount = 0
        val updatedDivs = state.divisions.map { d ->
            if (d.provinceId == fromProvinceId && d.nationId == state.playerNationId) {
                movedCount++
                d.copy(targetProvinceId = toProvinceId, moveProgress = 0.05)
            } else {
                d
            }
        }
        if (movedCount > 0) {
            _campaign.value = state.copy(divisions = updatedDivs)
            val targetName = TheaterWorldData.provinceMap[toProvinceId]?.name ?: toProvinceId
            _toastMessage.value = "Ordered $movedCount division(s) to $targetName."
        }
    }

    fun haltDivisionsInProvince(provinceId: String) {
        _campaign.update { state ->
            state.copy(
                divisions = state.divisions.map { d ->
                    if (d.provinceId == provinceId && d.nationId == state.playerNationId) {
                        d.copy(targetProvinceId = "", moveProgress = 0.0)
                    } else d
                }
            )
        }
        _toastMessage.value = "Divisions ordered to hold and entrench."
    }

    fun toggleBattleplan() {
        _campaign.update { state ->
            val next = !state.battleplanAutoAdvance
            _toastMessage.value = if (next) "Battleplan Auto-Advance EXECUTING" else "Battleplan Halted"
            state.copy(battleplanAutoAdvance = next)
        }
    }

    fun selectNationalFocus(focusId: String) {
        val state = _campaign.value
        val node = TheaterWorldData.focusTree.find { it.id == focusId } ?: return
        if (focusId in state.completedFocusIds) {
            _toastMessage.value = "Focus already completed."
            return
        }
        if (state.activeFocusId.isNotBlank()) {
            _toastMessage.value = "Finish current focus before starting another."
            return
        }
        if (!node.prerequisites.all { it in state.completedFocusIds }) {
            _toastMessage.value = "Prerequisites not yet completed."
            return
        }
        _campaign.value = state.copy(activeFocusId = focusId, activeFocusProgressDays = 0)
        _toastMessage.value = "National Focus started: ${node.title}"
    }

    fun enactConscriptionLaw(law: ConscriptionLaw) {
        val state = _campaign.value
        if (state.conscriptionLaw == law) return
        if (state.politicalPower < law.ppCost) {
            _toastMessage.value = "Need ${law.ppCost.toInt()} Political Power."
            return
        }
        _campaign.value = state.copy(
            politicalPower = state.politicalPower - law.ppCost,
            conscriptionLaw = law
        )
        _toastMessage.value = "Enacted ${law.displayName}"
    }

    fun enactEconomyLaw(law: EconomyLaw) {
        val state = _campaign.value
        if (state.economyLaw == law) return
        if (state.politicalPower < law.ppCost) {
            _toastMessage.value = "Need ${law.ppCost.toInt()} Political Power."
            return
        }
        if ((law == EconomyLaw.WAR_ECONOMY || law == EconomyLaw.TOTAL_MOB) && !state.isAtWar && state.warSupport < 0.5) {
            _toastMessage.value = "Requires War or 50% War Support."
            return
        }
        _campaign.value = state.copy(
            politicalPower = state.politicalPower - law.ppCost,
            economyLaw = law
        )
        _toastMessage.value = "Enacted ${law.displayName}"
    }

    fun enactTradeLaw(law: TradeLaw) {
        val state = _campaign.value
        if (state.tradeLaw == law) return
        if (state.politicalPower < law.ppCost) {
            _toastMessage.value = "Need ${law.ppCost.toInt()} Political Power."
            return
        }
        _campaign.value = state.copy(
            politicalPower = state.politicalPower - law.ppCost,
            tradeLaw = law
        )
        _toastMessage.value = "Enacted ${law.displayName}"
    }

    fun queueConstruction(provinceId: String, type: BuildingType) {
        val state = _campaign.value
        if (state.provinceOwners[provinceId] != state.playerNationId) {
            _toastMessage.value = "Select an owned province to build."
            return
        }
        if (state.constructionQueue.size >= 25) {
            _toastMessage.value = "Construction queue is full."
            return
        }
        val order = ConstructionOrder(provinceId = provinceId, buildingType = type)
        _campaign.value = state.copy(constructionQueue = state.constructionQueue + order)
        val pName = TheaterWorldData.provinceMap[provinceId]?.name ?: provinceId
        _toastMessage.value = "Queued ${type.displayName} in $pName"
    }

    fun adjustProductionLine(equipmentType: EquipmentType, delta: Int) {
        val state = _campaign.value
        if (delta > 0 && state.freeMilFactories <= 0) {
            _toastMessage.value = "No free Military Factories available."
            return
        }
        val updated = state.productionLines.map { line ->
            if (line.equipmentType == equipmentType) {
                val nextCount = (line.assignedFactories + delta).coerceAtLeast(0)
                line.copy(assignedFactories = nextCount)
            } else line
        }
        _campaign.value = state.copy(productionLines = updated)
    }

    fun trainDivision(template: DivisionTemplate) {
        val state = _campaign.value
        if (state.manpower < template.manpowerCost) {
            _toastMessage.value = "Insufficient Manpower (need ${template.manpowerCost / 1000}K)."
            return
        }
        // Check equipment stockpile
        for ((eq, needed) in template.equipmentCost) {
            val have = state.equipmentStockpile[eq] ?: 0
            if (have < needed) {
                _toastMessage.value = "Need $needed ${eq.displayName} (have $have)."
                return
            }
        }
        if (state.trainingQueue.size >= 16) {
            _toastMessage.value = "Training queue full (max 16)."
            return
        }

        val nextStockpile = state.equipmentStockpile.toMutableMap()
        for ((eq, needed) in template.equipmentCost) {
            nextStockpile[eq] = (nextStockpile[eq] ?: 0) - needed
        }
        val capId = TheaterWorldData.nations[state.playerNationId]?.capitalProvinceId
        val deployProv = if (capId != null && state.provinceOwners[capId] == state.playerNationId) {
            capId
        } else {
            state.provinceOwners.entries.firstOrNull { it.value == state.playerNationId }?.key ?: "berlin"
        }

        val adjDays = (template.trainDays / state.conscriptionLaw.trainingSpeedMod).toInt().coerceAtLeast(15)
        val order = TrainingOrder(
            template = template,
            deployProvinceId = deployProv,
            progressDays = 0,
            totalDays = adjDays
        )
        _campaign.value = state.copy(
            manpower = state.manpower - template.manpowerCost,
            equipmentStockpile = nextStockpile,
            trainingQueue = state.trainingQueue + order
        )
        _toastMessage.value = "Training ${template.displayName} ($adjDays days)"
    }

    fun selectResearch(techId: String) {
        val state = _campaign.value
        val tech = TheaterWorldData.techTree.find { it.id == techId } ?: return
        if (techId in state.unlockedTechIds) {
            _toastMessage.value = "Technology already researched."
            return
        }
        if (state.activeResearchId.isNotBlank()) {
            _toastMessage.value = "Research slot already active."
            return
        }
        if (!tech.prerequisites.all { it in state.unlockedTechIds }) {
            _toastMessage.value = "Research prerequisite first."
            return
        }
        _campaign.value = state.copy(activeResearchId = techId, activeResearchProgressDays = 0)
        _toastMessage.value = "Researching ${tech.title}"
    }

    fun improveRelations(targetNationId: String) {
        val state = _campaign.value
        if (state.politicalPower < 25.0) {
            _toastMessage.value = "Need 25 Political Power."
            return
        }
        val cur = state.nationRelations[targetNationId] ?: 0
        val nextRel = state.nationRelations.toMutableMap().apply { put(targetNationId, min(100, cur + 25)) }
        _campaign.value = state.copy(
            politicalPower = state.politicalPower - 25.0,
            nationRelations = nextRel
        )
        _toastMessage.value = "Improved diplomatic relations (+25)"
    }

    fun justifyWarGoal(targetNationId: String) {
        val state = _campaign.value
        if (state.diplomacyStates[targetNationId] == DiplomacyStatus.WAR) return
        if (targetNationId in state.warGoalProgress) {
            _toastMessage.value = "War justification already active."
            return
        }
        if (state.politicalPower < 35.0) {
            _toastMessage.value = "Need 35 Political Power to justify war goal."
            return
        }
        val days = if ("focus_hegemony" in state.completedFocusIds) 12 else 25
        val nextGoals = state.warGoalProgress.toMutableMap().apply { put(targetNationId, days) }
        val nextTension = (state.worldTension + 0.06).coerceAtMost(1.0)
        _campaign.value = state.copy(
            politicalPower = state.politicalPower - 35.0,
            warGoalProgress = nextGoals,
            worldTension = nextTension
        )
        val nName = TheaterWorldData.nations[targetNationId]?.name ?: targetNationId
        _toastMessage.value = "Justifying war goal against $nName ($days days)"
    }

    fun declareWar(targetNationId: String) {
        val state = _campaign.value
        if (state.diplomacyStates[targetNationId] == DiplomacyStatus.WAR) return
        val goalDays = state.warGoalProgress[targetNationId]
        val canImmediate = state.worldTension >= 0.45 || (goalDays != null && goalDays <= 0)
        if (!canImmediate) {
            _toastMessage.value = "Justify a war goal first (or wait for 45% World Tension)."
            return
        }
        val nextDip = state.diplomacyStates.toMutableMap().apply { put(targetNationId, DiplomacyStatus.WAR) }
        val nextGoals = state.warGoalProgress.toMutableMap().apply { remove(targetNationId) }
        val nextRel = state.nationRelations.toMutableMap().apply { put(targetNationId, -100) }
        val pName = TheaterWorldData.nations[state.playerNationId]?.name ?: state.playerNationId
        val tName = TheaterWorldData.nations[targetNationId]?.name ?: targetNationId
        val dateStr = GrandStrategyEngine.formatShortDate(state.gameDateEpochDay)
        _campaign.value = state.copy(
            diplomacyStates = nextDip,
            warGoalProgress = nextGoals,
            nationRelations = nextRel,
            worldTension = (state.worldTension + 0.14).coerceAtMost(1.0),
            newsLog = listOf("$dateStr — WAR! $pName declared war upon $tName!") + state.newsLog.take(29)
        )
        _toastMessage.value = "War declared on $tName!"
    }

    fun formAlliance(targetNationId: String) {
        val state = _campaign.value
        val rel = state.nationRelations[targetNationId] ?: 0
        if (state.diplomacyStates[targetNationId] == DiplomacyStatus.WAR) {
            _toastMessage.value = "Cannot ally while at war."
            return
        }
        if (rel < 40) {
            _toastMessage.value = "Requires +40 opinion to form military alliance (current: $rel)."
            return
        }
        if (state.politicalPower < 40.0) {
            _toastMessage.value = "Need 40 Political Power."
            return
        }
        val nextDip = state.diplomacyStates.toMutableMap().apply { put(targetNationId, DiplomacyStatus.ALLIANCE) }
        val nName = TheaterWorldData.nations[targetNationId]?.name ?: targetNationId
        val pName = TheaterWorldData.nations[state.playerNationId]?.name ?: state.playerNationId
        val dateStr = GrandStrategyEngine.formatShortDate(state.gameDateEpochDay)
        _campaign.value = state.copy(
            politicalPower = state.politicalPower - 40.0,
            diplomacyStates = nextDip,
            newsLog = listOf("$dateStr — Alliance forged between $pName and $nName.") + state.newsLog.take(29)
        )
        _toastMessage.value = "Alliance formed with $nName!"
    }

    fun saveCampaignToCloud(silent: Boolean = false) {
        val current = _campaign.value
        if (current.campaignId.isBlank()) return
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveCampaign(current)
            _isSaving.value = false
            if (!silent) {
                result.onSuccess {
                    _toastMessage.value = "Campaign saved to cloud."
                }.onFailure { err ->
                    _toastMessage.value = "Save failed: ${err.localizedMessage ?: "error"}"
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        simJob?.cancel()
    }
}
