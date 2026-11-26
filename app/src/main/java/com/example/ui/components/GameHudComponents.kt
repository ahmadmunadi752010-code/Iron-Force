package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TheaterWorldData
import com.example.engine.GrandStrategyEngine
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.TheaterTab
import kotlin.math.roundToInt

@Composable
fun TopCommandBar(
    campaign: CampaignState,
    gameSpeed: GameSpeed,
    isSaving: Boolean,
    onTogglePause: () -> Unit,
    onSetSpeed: (GameSpeed) -> Unit,
    onCloudSave: () -> Unit,
    onExitToHub: () -> Unit
) {
    val nation = TheaterWorldData.nations[campaign.playerNationId]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarRoomSurface)
            .border(1.dp, WarRoomCardBorder)
    ) {
        // Row 1: Nation Crest, Date Clock, Speed Controls, Save & Exit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onExitToHub,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("exit_to_hub_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Return to Command Hub",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(nation?.color ?: BrassGold)
                    .border(1.dp, BrassGold, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = nation?.shortTag ?: campaign.playerNationId,
                    style = MaterialTheme.typography.labelMedium,
                    color = ParchmentText
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = GrandStrategyEngine.formatDate(campaign.gameDateEpochDay, campaign.gameHour),
                    style = MaterialTheme.typography.labelMedium,
                    color = BrassGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${nation?.name ?: ""} • WT ${(campaign.worldTension * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Pause / Play + Speed 1x-5x
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                FilledIconButton(
                    onClick = onTogglePause,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (gameSpeed == GameSpeed.PAUSED) FrontlineCrimson else VictoryEmerald,
                        contentColor = ParchmentText
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (gameSpeed == GameSpeed.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (gameSpeed == GameSpeed.PAUSED) "Resume simulation" else "Pause simulation",
                        modifier = Modifier.size(18.dp)
                    )
                }

                listOf(GameSpeed.SPEED_1, GameSpeed.SPEED_3, GameSpeed.SPEED_5).forEach { spd ->
                    val active = gameSpeed == spd
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (active) BrassGold else WarRoomSurfaceElevated)
                            .clickable { onSetSpeed(spd) }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .testTag("speed_${spd.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = spd.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) WarRoomObsidian else ParchmentText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onCloudSave,
                enabled = !isSaving,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("cloud_save_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = BrassGold)
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Save campaign to cloud",
                        tint = BrassGold
                    )
                }
            }
        }

        // Row 2: HOI4 Resource Counters Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarRoomObsidian)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResourcePill("PP", "${campaign.politicalPower.roundToInt()}", BrassGold)
            ResourcePill("STAB", "${(campaign.stability * 100).roundToInt()}%", OliveSteelLight)
            ResourcePill("WAR", "${(campaign.warSupport * 100).roundToInt()}%", AmberSupply)
            ResourcePill("MP", "${campaign.manpower / 1000}K", ParchmentText)
            ResourcePill("CP", "${campaign.commandPower.roundToInt()}", AlliedCobalt)
            ResourcePill("IC", "${campaign.playerCivFactories}C / ${campaign.playerMilFactories}M", BrassGold)
            ResourcePill("DIV", "${campaign.playerDivisionCount}", VictoryEmerald)
        }
    }
}

@Composable
private fun ResourcePill(label: String, value: String, valueColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AlertRibbon(
    campaign: CampaignState,
    onNavigateTab: (TheaterTab) -> Unit
) {
    val alerts = buildList {
        if (campaign.activeFocusId.isBlank()) add("No National Focus" to TheaterTab.POLITICS)
        if (campaign.activeResearchId.isBlank()) add("Free Research Slot" to TheaterTab.RESEARCH)
        if (campaign.freeMilFactories > 0) add("${campaign.freeMilFactories} Idle Mil Factory" to TheaterTab.PRODUCTION)
        if (campaign.isAtWar) add("AT WAR (${campaign.atWarNations.joinToString()})" to TheaterTab.DIPLOMACY)
    }
    if (alerts.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        alerts.forEach { (text, tab) ->
            val isWar = tab == TheaterTab.DIPLOMACY
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isWar) FrontlineCrimsonDark else BrassGoldContainer)
                    .border(
                        1.dp,
                        if (isWar) FrontlineCrimson else BrassGoldMuted,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onNavigateTab(tab) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = if (isWar) FrontlineCrimson else BrassGold,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = ParchmentText
                )
            }
        }
    }
}

@Composable
fun ProvinceInspectorCard(
    provinceId: String,
    campaign: CampaignState,
    isOrderingMoveFrom: String?,
    onStartMoveOrder: (String) -> Unit,
    onCancelMoveOrder: () -> Unit,
    onHaltDivisions: (String) -> Unit,
    onToggleBattleplan: () -> Unit,
    onQuickBuildMil: (String) -> Unit,
    onQuickBuildFort: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prov = TheaterWorldData.provinceMap[provinceId] ?: return
    val ownerId = campaign.provinceOwners[provinceId] ?: prov.initialOwnerId
    val ownerNation = TheaterWorldData.nations[ownerId]
    val isOwned = ownerId == campaign.playerNationId
    val civs = campaign.provinceCivFactories[provinceId] ?: 0
    val mils = campaign.provinceMilFactories[provinceId] ?: 0
    val forts = campaign.provinceForts[provinceId] ?: 0
    val infra = campaign.provinceInfra[provinceId] ?: 5
    val stationed = campaign.divisions.filter { it.provinceId == provinceId }
    val friendlyStationed = stationed.filter { it.nationId == campaign.playerNationId }

    Card(
        colors = CardDefaults.cardColors(containerColor = WarRoomSurface.copy(alpha = 0.96f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .border(1.dp, WarRoomCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(ownerNation?.color ?: NationMinorColor)
                    )
                    Text(
                        text = "${prov.name} (${ownerNation?.shortTag ?: ownerId})",
                        style = MaterialTheme.typography.titleMedium,
                        color = BrassGold
                    )
                    Text(
                        text = "${prov.terrain.label} • ${prov.victoryPoints} VP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Civ $civs | Mil $mils | Fort $forts | Inf $infra",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stationed Divisions Summary
            if (stationed.isEmpty()) {
                Text(
                    text = "No divisions stationed in this province.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val avgOrg = stationed.map { it.org }.average().roundToInt()
                val avgHp = stationed.map { it.hp }.average().roundToInt()
                val movingCount = stationed.count { it.targetProvinceId.isNotBlank() }
                Text(
                    text = "${stationed.size} Division(s) [${stationed.first().nationId}] • Avg ORG $avgOrg • HP $avgHp%" +
                        if (movingCount > 0) " • Marching ($movingCount)" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (friendlyStationed.isNotEmpty()) {
                    if (isOrderingMoveFrom == provinceId) {
                        Button(
                            onClick = onCancelMoveOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = FrontlineCrimson),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("CANCEL ORDER", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Button(
                            onClick = { onStartMoveOrder(provinceId) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrassGold, contentColor = WarRoomObsidian),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("move_attack_button")
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("MOVE / ATTACK", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    OutlinedButton(
                        onClick = { onHaltDivisions(provinceId) },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("HOLD", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (isOwned) {
                    OutlinedButton(
                        onClick = { onQuickBuildMil(provinceId) },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("+MIL", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { onQuickBuildFort(provinceId) },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("+FORT", style = MaterialTheme.typography.labelSmall)
                    }
                }

                FilterChip(
                    selected = campaign.battleplanAutoAdvance,
                    onClick = onToggleBattleplan,
                    label = {
                        Text(
                            text = if (campaign.battleplanAutoAdvance) "BATTLEPLAN: ON" else "BATTLEPLAN: OFF",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("battleplan_toggle_chip")
                )
            }
        }
    }
}
