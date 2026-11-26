package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TheaterWorldData
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun PoliticsPanel(
    campaign: CampaignState,
    onSelectFocus: (String) -> Unit,
    onEnactConscription: (ConscriptionLaw) -> Unit,
    onEnactEconomy: (EconomyLaw) -> Unit,
    onEnactTrade: (TradeLaw) -> Unit,
    modifier: Modifier = Modifier
) {
    val nation = TheaterWorldData.nations[campaign.playerNationId]
    val activeFocus = TheaterWorldData.focusTree.find { it.id == campaign.activeFocusId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarRoomObsidian)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Nation Leadership Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarRoomSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarRoomCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(nation?.color ?: BrassGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(nation?.shortTag ?: "", style = MaterialTheme.typography.titleMedium, color = ParchmentText)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nation?.name ?: "", style = MaterialTheme.typography.titleLarge, color = BrassGold)
                            Text(
                                "${nation?.leaderName} • ${nation?.ideology?.label} • ${nation?.factionName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (activeFocus != null) {
                        val prog = (campaign.activeFocusProgressDays.toFloat() / activeFocus.daysRequired).coerceIn(0f, 1f)
                        Text(
                            text = "Active Focus: ${activeFocus.title} (${campaign.activeFocusProgressDays}/${activeFocus.daysRequired} days)",
                            style = MaterialTheme.typography.labelMedium,
                            color = VictoryEmerald
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { prog },
                            color = BrassGold,
                            trackColor = WarRoomSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    } else {
                        Text(
                            text = "No National Focus active — select one below to gain industrial & military bonuses.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberSupply
                        )
                    }
                }
            }
        }

        // National Focus Tree (3 Branches)
        item {
            Text("NATIONAL FOCUS TREE", style = MaterialTheme.typography.labelMedium, color = BrassGold)
            Spacer(modifier = Modifier.height(6.dp))
            val branches = listOf(0 to "Industry", 1 to "Military", 2 to "Political")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                branches.forEach { (col, branchName) ->
                    val nodes = TheaterWorldData.focusTree.filter { it.branchCol == col }.sortedBy { it.tierRow }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(WarRoomSurface)
                            .border(1.dp, WarRoomCardBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(branchName.uppercase(), style = MaterialTheme.typography.labelSmall, color = OliveSteelLight)
                        nodes.forEach { node ->
                            val isDone = node.id in campaign.completedFocusIds
                            val isActive = campaign.activeFocusId == node.id
                            val prereqsMet = node.prerequisites.all { it in campaign.completedFocusIds }
                            val canStart = !isDone && campaign.activeFocusId.isBlank() && prereqsMet

                            FocusNodeCard(
                                node = node,
                                isDone = isDone,
                                isActive = isActive,
                                canStart = canStart,
                                prereqsMet = prereqsMet,
                                onClick = { onSelectFocus(node.id) }
                            )
                        }
                    }
                }
            }
        }

        // Laws & Mobilization
        item {
            Text("CONSCRIPTION LAW", style = MaterialTheme.typography.labelMedium, color = BrassGold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConscriptionLaw.entries.forEach { law ->
                    val active = campaign.conscriptionLaw == law
                    LawChipCard(
                        title = law.displayName,
                        detail = "MP ${(law.recruitablePct * 100).roundToInt()}% • ${law.ppCost.toInt()} PP",
                        active = active,
                        onClick = { onEnactConscription(law) }
                    )
                }
            }
        }

        item {
            Text("ECONOMY MOBILIZATION", style = MaterialTheme.typography.labelMedium, color = BrassGold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EconomyLaw.entries.forEach { law ->
                    val active = campaign.economyLaw == law
                    LawChipCard(
                        title = law.displayName,
                        detail = "Civ Use ${((1 - law.consumerGoodsPct) * 100).roundToInt()}% • ${law.ppCost.toInt()} PP",
                        active = active,
                        onClick = { onEnactEconomy(law) }
                    )
                }
            }
        }

        item {
            Text("TRADE POLICY", style = MaterialTheme.typography.labelMedium, color = BrassGold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TradeLaw.entries.forEach { law ->
                    val active = campaign.tradeLaw == law
                    LawChipCard(
                        title = law.displayName,
                        detail = "+${(law.factoryOutputBonus * 100).roundToInt()}% Output • ${law.ppCost.toInt()} PP",
                        active = active,
                        onClick = { onEnactTrade(law) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusNodeCard(
    node: FocusNode,
    isDone: Boolean,
    isActive: Boolean,
    canStart: Boolean,
    prereqsMet: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isDone -> VictoryEmerald
        isActive -> BrassGold
        canStart -> OliveSteelLight
        else -> WarRoomCardBorder
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) BrassGoldContainer else WarRoomSurfaceElevated)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = canStart, onClick = onClick)
            .padding(10.dp)
            .testTag("focus_node_${node.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                isDone -> Icons.Default.CheckCircle
                !prereqsMet -> Icons.Default.Lock
                else -> Icons.Default.Add
            },
            contentDescription = null,
            tint = when {
                isDone -> VictoryEmerald
                isActive -> BrassGold
                prereqsMet -> ParchmentText
                else -> MutedSteelText
            },
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(node.title, style = MaterialTheme.typography.titleMedium, color = ParchmentText)
            Text(node.rewardText, style = MaterialTheme.typography.bodyMedium, color = BrassGold)
            Text(
                text = if (isDone) "Completed" else if (isActive) "In Progress" else "${node.daysRequired} Days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LawChipCard(
    title: String,
    detail: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(164.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) BrassGoldContainer else WarRoomSurface)
            .border(1.dp, if (active) BrassGold else WarRoomCardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (active) BrassGold else ParchmentText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProductionPanel(
    campaign: CampaignState,
    selectedProvinceId: String?,
    onAdjustFactory: (EquipmentType, Int) -> Unit,
    onQueueBuilding: (String, BuildingType) -> Unit,
    onTrainDivision: (DivisionTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    val ownedProvinces = remember(campaign.provinceOwners) {
        TheaterWorldData.provinces.filter { campaign.provinceOwners[it.id] == campaign.playerNationId }
    }
    var buildProvId by remember(selectedProvinceId, ownedProvinces) {
        mutableStateOf(
            if (selectedProvinceId != null && campaign.provinceOwners[selectedProvinceId] == campaign.playerNationId) {
                selectedProvinceId
            } else {
                ownedProvinces.firstOrNull()?.id ?: ""
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarRoomObsidian)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Military Production Lines
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MILITARY PRODUCTION", style = MaterialTheme.typography.labelMedium, color = BrassGold)
                Text(
                    text = "Free Mil: ${campaign.freeMilFactories} / ${campaign.playerMilFactories}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (campaign.freeMilFactories > 0) AmberSupply else VictoryEmerald
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                campaign.productionLines.forEach { line ->
                    val stock = campaign.equipmentStockpile[line.equipmentType] ?: 0
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarRoomSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WarRoomCardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.equipmentType.displayName, style = MaterialTheme.typography.titleMedium, color = ParchmentText)
                                Text(
                                    "Stockpile: $stock • IC Cost: ${line.equipmentType.icCost.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { onAdjustFactory(line.equipmentType, -1) },
                                    enabled = line.assignedFactories > 0,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Remove factory", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${line.assignedFactories}F",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = BrassGold,
                                    fontWeight = FontWeight.Bold
                                )
                                FilledTonalIconButton(
                                    onClick = { onAdjustFactory(line.equipmentType, 1) },
                                    enabled = campaign.freeMilFactories > 0,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Assign factory", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Construction Queue
        item {
            Text("STATE CONSTRUCTION (${campaign.playerCivFactories} CIV FACTORIES)", style = MaterialTheme.typography.labelMedium, color = BrassGold)
            Spacer(modifier = Modifier.height(6.dp))
            if (ownedProvinces.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ownedProvinces.forEach { prov ->
                        FilterChip(
                            selected = buildProvId == prov.id,
                            onClick = { buildProvId = prov.id },
                            label = { Text(prov.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BuildingType.entries.forEach { bType ->
                        OutlinedButton(
                            onClick = { if (buildProvId.isNotBlank()) onQueueBuilding(buildProvId, bType) },
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("+ ${bType.displayName}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            if (campaign.constructionQueue.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                campaign.constructionQueue.take(4).forEachIndexed { idx, order ->
                    val pName = TheaterWorldData.provinceMap[order.provinceId]?.name ?: order.provinceId
                    val pct = (order.progressDays.toFloat() / order.totalDays).coerceIn(0f, 1f)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarRoomSurface)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "${idx + 1}. ${order.buildingType.displayName} in $pName (${order.progressDays}/${order.totalDays})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(progress = { pct }, color = BrassGold, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Division Templates & Recruit Queue
        item {
            Text("RECRUIT & DEPLOY DIVISIONS", style = MaterialTheme.typography.labelMedium, color = BrassGold)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DivisionTemplate.entries.forEach { tpl ->
                    val costStr = tpl.equipmentCost.entries.joinToString(" | ") { "${it.value} ${it.key.shortCode}" }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarRoomSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WarRoomCardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tpl.displayName, style = MaterialTheme.typography.titleMedium, color = ParchmentText)
                                Text(
                                    "Soft ${tpl.softAttack.toInt()} | Hard ${tpl.hardAttack.toInt()} | Def ${tpl.defense.toInt()} | Org ${tpl.maxOrg.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OliveSteelLight
                                )
                                Text(
                                    "Cost: ${tpl.manpowerCost / 1000}K MP | $costStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { onTrainDivision(tpl) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrassGold, contentColor = WarRoomObsidian),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("train_${tpl.name}")
                            ) {
                                Text("TRAIN", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            if (campaign.trainingQueue.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("TRAINING QUEUE (${campaign.trainingQueue.size})", style = MaterialTheme.typography.labelSmall, color = OliveSteelLight)
                campaign.trainingQueue.forEach { t ->
                    val pct = (t.progressDays.toFloat() / t.totalDays).coerceIn(0f, 1f)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarRoomSurface)
                            .padding(8.dp)
                    ) {
                        Text(
                            "${t.template.displayName} (${t.progressDays}/${t.totalDays} days)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ParchmentText
                        )
                        LinearProgressIndicator(progress = { pct }, color = VictoryEmerald, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
