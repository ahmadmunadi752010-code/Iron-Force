package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CampaignSave
import com.example.data.TheaterWorldData
import com.example.data.signOut
import com.example.engine.GrandStrategyEngine
import com.example.model.CampaignState
import com.example.model.Difficulty
import com.example.model.NationDef
import com.example.ui.theme.BrassGold
import com.example.ui.theme.FrontlineCrimson
import com.example.ui.theme.VictoryEmerald
import com.example.ui.theme.WarRoomCardBorder
import com.example.ui.theme.WarRoomObsidian
import com.example.ui.theme.WarRoomSurface
import com.example.ui.theme.WarRoomSurfaceElevated
import com.example.viewmodel.CampaignHubViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignHubScreen(
    viewModel: CampaignHubViewModel,
    commanderName: String,
    onLaunchCampaign: (CampaignState) -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val campaignsState by viewModel.campaignsState.collectAsStateWithLifecycle()
    val busyMessage by viewModel.busyMessage.collectAsStateWithLifecycle()
    val errorBanner by viewModel.errorBanner.collectAsStateWithLifecycle()

    var showNewCampaignSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    Scaffold(
        containerColor = WarRoomObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarRoomSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Column {
                        Text(
                            text = "IRON FRONT — COMMAND HUB",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrassGold
                        )
                        Text(
                            text = "Commander: $commanderName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            signOut(credentialManager, scope, onSignOutComplete = onSignedOut)
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewCampaignSheet = true },
                containerColor = BrassGold,
                contentColor = WarRoomObsidian,
                modifier = Modifier.testTag("new_campaign_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("NEW 1936 CAMPAIGN", style = MaterialTheme.typography.labelLarge)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (errorBanner != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorBanner ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("DISMISS")
                        }
                    }
                }
            }

            if (busyMessage != null) {
                LinearProgressIndicator(
                    color = BrassGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
                Text(
                    text = busyMessage ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = BrassGold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Compact War Room Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(124.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, WarRoomCardBorder, RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_war_room_banner),
                    contentDescription = "Theater command map banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    WarRoomObsidian.copy(alpha = 0.92f),
                                    WarRoomObsidian.copy(alpha = 0.45f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(
                            text = "EUROPEAN THEATER 1936",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrassGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select a Saved Campaign or Deploy New Forces",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "40 Provinces • 8 Playable Nations • Cloud Synced",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "CLOUD SAVED CAMPAIGNS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (val state = campaignsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrassGold)
                    }
                }
                is UiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is UiState.Success -> {
                    val saves = state.data
                    if (saves.isEmpty()) {
                        EmptyCampaignsCard(onStartNew = { showNewCampaignSheet = true })
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 92.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(saves, key = { it.id }) { save ->
                                SavedCampaignCard(
                                    save = save,
                                    onContinue = {
                                        viewModel.loadAndLaunchCampaign(save.id, onLaunchCampaign)
                                    },
                                    onDelete = {
                                        viewModel.deleteCampaign(save.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewCampaignSheet) {
        NewCampaignDialog(
            onDismiss = { showNewCampaignSheet = false },
            onConfirm = { nationId, name, diff ->
                showNewCampaignSheet = false
                viewModel.createAndLaunchCampaign(nationId, name, diff, onLaunchCampaign)
            }
        )
    }
}

@Composable
private fun EmptyCampaignsCard(onStartNew: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WarRoomSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WarRoomCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = BrassGold,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Active Campaigns in Command Dossier",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose one of 8 European powers in the 1936 scenario to begin conquering, industrializing, and forging alliances.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartNew,
                colors = ButtonDefaults.buttonColors(containerColor = BrassGold, contentColor = WarRoomObsidian),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("empty_start_campaign_button")
            ) {
                Text("CHOOSE NATION (1936)", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SavedCampaignCard(
    save: CampaignSave,
    onContinue: () -> Unit,
    onDelete: () -> Unit
) {
    val nation = TheaterWorldData.nations[save.playerNationId]
    val nationColor = nation?.color ?: BrassGold
    val provCount = save.provinceOwners.count { it.value == save.playerNationId }
    val dateStr = GrandStrategyEngine.formatDate(save.gameDateEpochDay, save.gameHour.toInt())

    Card(
        colors = CardDefaults.cardColors(containerColor = WarRoomSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WarRoomCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onContinue)
            .testTag("saved_campaign_card_${save.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(nationColor)
                    .border(1.5.dp, BrassGold.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nation?.shortTag ?: save.playerNationId,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = save.campaignName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${nation?.name ?: save.playerNationId} • $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrassGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "$provCount Prov",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${save.divisionsData.count { (it["nationId"] as? String) == save.playerNationId }} Divs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val statusColor = when (save.status) {
                        "victory" -> VictoryEmerald
                        "defeat" -> FrontlineCrimson
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = save.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_campaign_${save.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete campaign",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onContinue,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("load_campaign_${save.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Resume campaign",
                    tint = BrassGold
                )
            }
        }
    }
}

@Composable
private fun NewCampaignDialog(
    onDismiss: () -> Unit,
    onConfirm: (nationId: String, campaignName: String, difficulty: Difficulty) -> Unit
) {
    var selectedNation by remember { mutableStateOf(TheaterWorldData.playableNations.first()) }
    var difficulty by remember { mutableStateOf(Difficulty.REGULAR) }
    var campaignName by remember(selectedNation) {
        mutableStateOf("${selectedNation.name} 1936")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarRoomSurface,
        titleContentColor = BrassGold,
        title = {
            Text("1936 SCENARIO — SELECT NATION", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = campaignName,
                    onValueChange = { if (it.length <= 50) campaignName = it },
                    label = { Text("Campaign Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("campaign_name_input")
                )

                Text(
                    text = "DIFFICULTY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = "PLAYABLE POWERS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TheaterWorldData.playableNations.forEach { nation ->
                    NationChoiceRow(
                        nation = nation,
                        selected = selectedNation.id == nation.id,
                        onSelect = { selectedNation = nation }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedNation.id, campaignName, difficulty) },
                colors = ButtonDefaults.buttonColors(containerColor = BrassGold, contentColor = WarRoomObsidian),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("launch_campaign_button")
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("DEPLOY", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun NationChoiceRow(
    nation: NationDef,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (selected) BrassGold else WarRoomCardBorder
    val bgColor = if (selected) WarRoomSurfaceElevated else WarRoomObsidian
    val provCount = TheaterWorldData.provinces.count { it.initialOwnerId == nation.id }
    val milCount = TheaterWorldData.provinces.filter { it.initialOwnerId == nation.id }.sumOf { it.initialMil }
    val civCount = TheaterWorldData.provinces.filter { it.initialOwnerId == nation.id }.sumOf { it.initialCiv }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(if (selected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(10.dp)
            .testTag("nation_option_${nation.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(nation.color),
            contentAlignment = Alignment.Center
        ) {
            Text(nation.shortTag, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(nation.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    nation.ideology.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrassGold
                )
            }
            Text(
                text = nation.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$provCount Prov • $civCount Civ / $milCount Mil • ${(nation.startingManpower / 1000)}K MP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
