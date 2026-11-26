package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TheaterWorldData
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ResearchPanel(
    campaign: CampaignState,
    onSelectResearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTech = TheaterWorldData.techTree.find { it.id == campaign.activeResearchId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarRoomObsidian)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarRoomSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarRoomCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("RESEARCH SLOT 1", style = MaterialTheme.typography.labelMedium, color = BrassGold)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (activeTech != null) {
                        val prog = (campaign.activeResearchProgressDays.toFloat() / activeTech.daysRequired).coerceIn(0f, 1f)
                        Text(
                            text = "${activeTech.title} (${campaign.activeResearchProgressDays}/${activeTech.daysRequired} days)",
                            style = MaterialTheme.typography.titleMedium,
                            color = ParchmentText
                        )
                        Text(activeTech.bonusText, style = MaterialTheme.typography.bodyMedium, color = VictoryEmerald)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { prog },
                            color = AlliedCobalt,
                            trackColor = WarRoomSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    } else {
                        Text(
                            text = "Research slot idle — select an available technology below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberSupply
                        )
                    }
                }
            }
        }

        TechCategory.entries.forEach { cat ->
            item {
                Text(cat.label.uppercase(), style = MaterialTheme.typography.labelMedium, color = BrassGold)
                Spacer(modifier = Modifier.height(6.dp))
                val nodes = TheaterWorldData.techTree.filter { it.category == cat }.sortedBy { it.tier }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    nodes.forEach { tech ->
                        val unlocked = tech.id in campaign.unlockedTechIds
                        val active = campaign.activeResearchId == tech.id
                        val prereqMet = tech.prerequisites.all { it in campaign.unlockedTechIds }
                        val canStart = !unlocked && campaign.activeResearchId.isBlank() && prereqMet

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) BrassGoldContainer else WarRoomSurface)
                                .border(
                                    1.dp,
                                    when {
                                        unlocked -> VictoryEmerald
                                        active -> BrassGold
                                        canStart -> OliveSteelLight
                                        else -> WarRoomCardBorder
                                    },
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = canStart) { onSelectResearch(tech.id) }
                                .padding(12.dp)
                                .testTag("tech_node_${tech.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    unlocked -> Icons.Default.CheckCircle
                                    !prereqMet -> Icons.Default.Lock
                                    else -> Icons.Default.Science
                                },
                                contentDescription = null,
                                tint = when {
                                    unlocked -> VictoryEmerald
                                    active -> BrassGold
                                    prereqMet -> AlliedCobalt
                                    else -> MutedSteelText
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tier ${tech.tier}: ${tech.title}", style = MaterialTheme.typography.titleMedium, color = ParchmentText)
                                Text(tech.bonusText, style = MaterialTheme.typography.bodyMedium, color = BrassGold)
                                Text(
                                    text = if (unlocked) "Researched" else if (active) "Researching..." else "${tech.daysRequired} Days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiplomacyPanel(
    campaign: CampaignState,
    onImproveRelations: (String) -> Unit,
    onJustifyWar: (String) -> Unit,
    onDeclareWar: (String) -> Unit,
    onFormAlliance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val foreignNations = TheaterWorldData.nations.values.filter { it.id != campaign.playerNationId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarRoomObsidian)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // World News Feed
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarRoomSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarRoomCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("WORLD DISPATCHES", style = MaterialTheme.typography.labelMedium, color = BrassGold)
                    Spacer(modifier = Modifier.height(6.dp))
                    campaign.newsLog.take(4).forEach { headline ->
                        Text(
                            text = "• $headline",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentText,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        items(foreignNations, key = { it.id }) { nation ->
            val provCount = campaign.provinceOwners.count { it.value == nation.id }
            val divCount = campaign.divisions.count { it.nationId == nation.id }
            val status = campaign.diplomacyStates[nation.id] ?: DiplomacyStatus.PEACE
            val opinion = campaign.nationRelations[nation.id] ?: 0
            val warGoalDays = campaign.warGoalProgress[nation.id]
            val canImmediateWar = campaign.worldTension >= 0.45 || (warGoalDays != null && warGoalDays <= 0)

            Card(
                colors = CardDefaults.cardColors(containerColor = WarRoomSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        when (status) {
                            DiplomacyStatus.WAR -> FrontlineCrimson
                            DiplomacyStatus.ALLIANCE -> AlliedCobalt
                            else -> WarRoomCardBorder
                        },
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(nation.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(nation.shortTag, style = MaterialTheme.typography.labelMedium, color = ParchmentText)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nation.name, style = MaterialTheme.typography.titleMedium, color = ParchmentText)
                            Text(
                                "${nation.ideology.label} • $provCount Prov • $divCount Divs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = status.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (status) {
                                    DiplomacyStatus.WAR -> FrontlineCrimson
                                    DiplomacyStatus.ALLIANCE -> AlliedCobalt
                                    else -> BrassGold
                                }
                            )
                            Text(
                                text = "Opinion: ${if (opinion >= 0) "+$opinion" else "$opinion"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (opinion >= 0) VictoryEmerald else FrontlineCrimson
                            )
                        }
                    }

                    if (provCount > 0 && status != DiplomacyStatus.WAR) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onImproveRelations(nation.id) },
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("RELATIONS (25PP)", style = MaterialTheme.typography.labelSmall)
                            }
                            if (status != DiplomacyStatus.ALLIANCE) {
                                OutlinedButton(
                                    onClick = { onFormAlliance(nation.id) },
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Text("ALLY (40PP)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            if (canImmediateWar) {
                                Button(
                                    onClick = { onDeclareWar(nation.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FrontlineCrimson),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("declare_war_${nation.id}")
                                ) {
                                    Text("DECLARE WAR", style = MaterialTheme.typography.labelSmall)
                                }
                            } else if (warGoalDays != null) {
                                Text(
                                    text = "Justifying (${warGoalDays}d)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberSupply,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            } else {
                                Button(
                                    onClick = { onJustifyWar(nation.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FrontlineCrimsonDark, contentColor = ParchmentText),
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("justify_war_${nation.id}")
                                ) {
                                    Text("JUSTIFY (35PP)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
