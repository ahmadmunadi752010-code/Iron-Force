package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BuildingType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.TheaterTab

@Composable
fun TheaterScreen(
    viewModel: GameViewModel,
    onExitToHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val campaign by viewModel.campaign.collectAsStateWithLifecycle()
    val gameSpeed by viewModel.gameSpeed.collectAsStateWithLifecycle()
    val mapMode by viewModel.mapMode.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val selectedProvinceId by viewModel.selectedProvinceId.collectAsStateWithLifecycle()
    val isOrderingMoveFrom by viewModel.isOrderingMoveFrom.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    BackHandler {
        if (activeTab != TheaterTab.MAP) {
            viewModel.setActiveTab(TheaterTab.MAP)
        } else {
            viewModel.saveCampaignToCloud(silent = true)
            onExitToHub()
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2400L)
            viewModel.clearToast()
        }
    }

    Scaffold(
        containerColor = WarRoomObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = WarRoomSurface,
                contentColor = BrassGold,
                windowInsets = WindowInsets.navigationBars
            ) {
                TheaterTab.entries.forEach { tab ->
                    val icon = when (tab) {
                        TheaterTab.MAP -> Icons.Default.Map
                        TheaterTab.POLITICS -> Icons.Default.AccountTree
                        TheaterTab.PRODUCTION -> Icons.Default.Factory
                        TheaterTab.RESEARCH -> Icons.Default.Science
                        TheaterTab.DIPLOMACY -> Icons.Default.Public
                    }
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { viewModel.setActiveTab(tab) },
                        icon = { Icon(icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrassGold,
                            selectedTextColor = BrassGold,
                            indicatorColor = BrassGoldContainer,
                            unselectedIconColor = MutedSteelText,
                            unselectedTextColor = MutedSteelText
                        ),
                        modifier = Modifier.testTag("tab_${tab.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopCommandBar(
                campaign = campaign,
                gameSpeed = gameSpeed,
                isSaving = isSaving,
                onTogglePause = { viewModel.togglePause() },
                onSetSpeed = { viewModel.setGameSpeed(it) },
                onCloudSave = { viewModel.saveCampaignToCloud(silent = false) },
                onExitToHub = {
                    viewModel.saveCampaignToCloud(silent = true)
                    onExitToHub()
                }
            )

            AlertRibbon(
                campaign = campaign,
                onNavigateTab = { viewModel.setActiveTab(it) }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (activeTab) {
                    TheaterTab.MAP -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            TheaterMapCanvas(
                                campaign = campaign,
                                mapMode = mapMode,
                                selectedProvinceId = selectedProvinceId,
                                orderingMoveFromId = isOrderingMoveFrom,
                                onProvinceTapped = { viewModel.onProvinceTapped(it) },
                                onMapModeSelected = { viewModel.setMapMode(it) },
                                modifier = Modifier.fillMaxSize()
                            )

                            val selId = selectedProvinceId
                            if (selId != null) {
                                ProvinceInspectorCard(
                                    provinceId = selId,
                                    campaign = campaign,
                                    isOrderingMoveFrom = isOrderingMoveFrom,
                                    onStartMoveOrder = { viewModel.startMoveOrderFrom(it) },
                                    onCancelMoveOrder = { viewModel.cancelMoveOrder() },
                                    onHaltDivisions = { viewModel.haltDivisionsInProvince(it) },
                                    onToggleBattleplan = { viewModel.toggleBattleplan() },
                                    onQuickBuildMil = { viewModel.queueConstruction(it, BuildingType.MIL_FACTORY) },
                                    onQuickBuildFort = { viewModel.queueConstruction(it, BuildingType.LAND_FORT) },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }

                    TheaterTab.POLITICS -> {
                        PoliticsPanel(
                            campaign = campaign,
                            onSelectFocus = { viewModel.selectNationalFocus(it) },
                            onEnactConscription = { viewModel.enactConscriptionLaw(it) },
                            onEnactEconomy = { viewModel.enactEconomyLaw(it) },
                            onEnactTrade = { viewModel.enactTradeLaw(it) }
                        )
                    }

                    TheaterTab.PRODUCTION -> {
                        ProductionPanel(
                            campaign = campaign,
                            selectedProvinceId = selectedProvinceId,
                            onAdjustFactory = { eq, delta -> viewModel.adjustProductionLine(eq, delta) },
                            onQueueBuilding = { pId, bType -> viewModel.queueConstruction(pId, bType) },
                            onTrainDivision = { tpl -> viewModel.trainDivision(tpl) }
                        )
                    }

                    TheaterTab.RESEARCH -> {
                        ResearchPanel(
                            campaign = campaign,
                            onSelectResearch = { viewModel.selectResearch(it) }
                        )
                    }

                    TheaterTab.DIPLOMACY -> {
                        DiplomacyPanel(
                            campaign = campaign,
                            onImproveRelations = { viewModel.improveRelations(it) },
                            onJustifyWar = { viewModel.justifyWarGoal(it) },
                            onDeclareWar = { viewModel.declareWar(it) },
                            onFormAlliance = { viewModel.formAlliance(it) }
                        )
                    }
                }

                // Tactical Toast Feedback
                if (toastMessage != null) {
                    Surface(
                        color = WarRoomSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .border(1.dp, BrassGold, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = toastMessage ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = ParchmentText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (campaign.status != "active") {
        val isVictory = campaign.status == "victory"
        AlertDialog(
            onDismissRequest = {},
            containerColor = WarRoomSurface,
            title = {
                Text(
                    text = if (isVictory) "TOTAL VICTORY" else "NATION CAPITULATED",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isVictory) VictoryEmerald else FrontlineCrimson
                )
            },
            text = {
                Text(
                    text = if (isVictory) {
                        "Your armies have established global supremacy across the world theater. The campaign record has been archived in Cloud Command."
                    } else {
                        "Your capital and strategic heartland have fallen. High Command has surrendered."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = ParchmentText
                )
            },
            confirmButton = {
                Button(
                    onClick = onExitToHub,
                    colors = ButtonDefaults.buttonColors(containerColor = BrassGold, contentColor = WarRoomObsidian),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("RETURN TO COMMAND HUB", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}
