package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GoogleSignInButton
import com.example.data.TheaterWorldData
import com.example.model.CampaignState
import com.example.model.Difficulty
import com.example.ui.theme.BrassGold
import com.example.ui.theme.WarRoomCardBorder
import com.example.ui.theme.WarRoomObsidian
import com.example.ui.theme.WarRoomSurface
import com.example.ui.theme.WarRoomSurfaceElevated

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onPlayOffline: (CampaignState) -> Unit,
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOfflinePicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarRoomObsidian)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_war_room_banner),
            contentDescription = "Grand strategy war room command table",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            WarRoomObsidian.copy(alpha = 0.2f),
                            WarRoomObsidian.copy(alpha = 0.72f),
                            WarRoomObsidian
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            Text(
                text = "1936 THEATER COMMAND",
                style = MaterialTheme.typography.labelMedium,
                color = BrassGold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "IRON FRONT",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Grand strategy WWII theater wargame: command divisions, national focus trees, war production, & diplomacy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 460.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Primary CTAs immediately above the fold
            GoogleSignInButton(
                onAuthSuccess = {
                    errorMessage = null
                    onAuthSuccess()
                },
                onAuthError = { msg ->
                    errorMessage = msg
                },
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showOfflinePicker = true },
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("play_offline_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = BrassGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY IMMEDIATELY (LOCAL SKIRMISH)",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrassGold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarRoomSurface.copy(alpha = 0.92f))
                    .border(1.dp, WarRoomCardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureRow(
                    icon = Icons.Default.Map,
                    title = "Interactive 70-Province World Map",
                    desc = "Pan, zoom, and direct divisions across real terrain, forts, and supply lines."
                )
                FeatureRow(
                    icon = Icons.Default.AccountTree,
                    title = "National Focus & Tech Trees",
                    desc = "Enact mobilization laws, research armor doctrines, and expand factories."
                )
                FeatureRow(
                    icon = Icons.Default.MilitaryTech,
                    title = "Frontline Combat & Battleplans",
                    desc = "Live hourly combat bubbles, organization breakthroughs, and encirclements."
                )
                FeatureRow(
                    icon = Icons.Default.CloudDone,
                    title = "Cloud Campaign Saves",
                    desc = "Sign in with Google to save & resume campaigns across devices."
                )
            }
        }
    }

    if (showOfflinePicker) {
        OfflineNationPickerDialog(
            onDismiss = { showOfflinePicker = false },
            onDeploy = { nationId, difficulty ->
                showOfflinePicker = false
                val nationName = TheaterWorldData.nations[nationId]?.name ?: nationId
                onPlayOffline(
                    TheaterWorldData.createInitialCampaignState(
                        playerNationId = nationId,
                        campaignName = "$nationName 1936 (Local)",
                        difficulty = difficulty
                    )
                )
            }
        )
    }
}

@Composable
private fun OfflineNationPickerDialog(
    onDismiss: () -> Unit,
    onDeploy: (nationId: String, difficulty: Difficulty) -> Unit
) {
    var selectedNation by remember { mutableStateOf(TheaterWorldData.playableNations.first()) }
    var difficulty by remember { mutableStateOf(Difficulty.REGULAR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarRoomSurface,
        titleContentColor = BrassGold,
        title = {
            Text("SELECT 1936 NATION", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                TheaterWorldData.playableNations.forEach { nation ->
                    val selected = selectedNation.id == nation.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) WarRoomSurfaceElevated else WarRoomObsidian)
                            .border(
                                if (selected) 1.5.dp else 1.dp,
                                if (selected) BrassGold else WarRoomCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedNation = nation }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(nation.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(nation.shortTag, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nation.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = nation.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDeploy(selectedNation.id, difficulty) },
                colors = ButtonDefaults.buttonColors(containerColor = BrassGold, contentColor = WarRoomObsidian),
                modifier = Modifier.height(48.dp)
            ) {
                Text("START GAME", style = MaterialTheme.typography.labelLarge)
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
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrassGold,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
