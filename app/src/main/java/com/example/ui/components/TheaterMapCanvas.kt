package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TheaterWorldData
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun TheaterMapCanvas(
    campaign: CampaignState,
    mapMode: MapMode,
    selectedProvinceId: String?,
    orderingMoveFromId: String?,
    onProvinceTapped: (String) -> Unit,
    onMapModeSelected: (MapMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.15f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()

    val validMoveTargets = remember(orderingMoveFromId, campaign.provinceOwners, campaign.diplomacyStates) {
        if (orderingMoveFromId == null) emptySet()
        else {
            val fromProv = TheaterWorldData.provinceMap[orderingMoveFromId]
            fromProv?.neighbors?.filter { nId ->
                val nOwner = campaign.provinceOwners[nId]
                nOwner == campaign.playerNationId ||
                    campaign.diplomacyStates[nOwner] == DiplomacyStatus.WAR ||
                    campaign.diplomacyStates[nOwner] == DiplomacyStatus.ALLIANCE
            }?.toSet() ?: emptySet()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDeep)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val nextScale = (scale * zoom).coerceIn(0.85f, 3.2f)
                        scale = nextScale
                        val maxPanX = size.width * 0.55f * nextScale
                        val maxPanY = size.height * 0.55f * nextScale
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                            y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                        )
                    }
                }
                .pointerInput(scale, panOffset) {
                    detectTapGestures { tap ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val mapSide = minOf(w, h * 1.15f).coerceAtLeast(1f)
                        val originX = (w - mapSide) / 2f
                        val originY = (h - mapSide) / 2f

                        // Invert transform: screen = center + (world - center) * scale + panOffset
                        val cx = w / 2f
                        val cy = h / 2f
                        val worldX = (tap.x - cx - panOffset.x) / scale + cx
                        val worldY = (tap.y - cy - panOffset.y) / scale + cy
                        val normX = (worldX - originX) / mapSide
                        val normY = (worldY - originY) / mapSide

                        val hit = TheaterWorldData.provinces.minByOrNull { prov ->
                            hypot((prov.centerX - normX).toDouble(), (prov.centerY - normY).toDouble())
                        }
                        if (hit != null) {
                            val dist = hypot((hit.centerX - normX).toDouble(), (hit.centerY - normY).toDouble())
                            if (dist < 0.12) {
                                onProvinceTapped(hit.id)
                            }
                        }
                    }
                }
                .testTag("theater_map_canvas")
        ) {
            val w = size.width
            val h = size.height
            val mapSide = minOf(w, h * 1.15f)
            val originX = (w - mapSide) / 2f
            val originY = (h - mapSide) / 2f
            val cx = w / 2f
            val cy = h / 2f

            fun toScreen(nx: Float, ny: Float): Offset {
                val wx = originX + nx * mapSide
                val wy = originY + ny * mapSide
                return Offset(
                    x = cx + (wx - cx) * scale + panOffset.x,
                    y = cy + (wy - cy) * scale + panOffset.y
                )
            }

            // 1. Cartographic Latitude / Longitude Graticule Lines
            val gridColor = OceanCoastal.copy(alpha = 0.7f)
            for (i in 0..10) {
                val t = i / 10f
                val pStartH = toScreen(0f, t)
                val pEndH = toScreen(1f, t)
                drawLine(gridColor, pStartH, pEndH, strokeWidth = 1f)
                val pStartV = toScreen(t, 0f)
                val pEndV = toScreen(t, 1f)
                drawLine(gridColor, pStartV, pEndV, strokeWidth = 1f)
            }

            // 2. Adjacency & Supply Network Links
            val drawnLinks = HashSet<String>()
            for (prov in TheaterWorldData.provinces) {
                val pA = toScreen(prov.centerX, prov.centerY)
                for (nId in prov.neighbors) {
                    val key = if (prov.id < nId) "${prov.id}-$nId" else "$nId-${prov.id}"
                    if (drawnLinks.add(key)) {
                        val nb = TheaterWorldData.provinceMap[nId] ?: continue
                        val pB = toScreen(nb.centerX, nb.centerY)
                        val sameOwner = campaign.provinceOwners[prov.id] == campaign.provinceOwners[nId]
                        drawLine(
                            color = if (sameOwner) ParchmentText.copy(alpha = 0.18f) else FrontlineCrimson.copy(alpha = 0.35f),
                            start = pA,
                            end = pB,
                            strokeWidth = if (sameOwner) 1.5f * scale else 2.2f * scale,
                            pathEffect = if (sameOwner) null else PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )
                    }
                }
            }

            // 3. Province Polygons
            for (prov in TheaterWorldData.provinces) {
                val ownerId = campaign.provinceOwners[prov.id] ?: prov.initialOwnerId
                val fillColor = when (mapMode) {
                    MapMode.POLITICAL -> {
                        val base = TheaterWorldData.nations[ownerId]?.color ?: NationMinorColor
                        base.copy(alpha = 0.86f)
                    }
                    MapMode.TERRAIN_SUPPLY -> {
                        Color(prov.terrain.mapTint).copy(alpha = 0.88f)
                    }
                    MapMode.DIPLOMACY -> {
                        when {
                            ownerId == campaign.playerNationId -> VictoryEmerald.copy(alpha = 0.85f)
                            campaign.diplomacyStates[ownerId] == DiplomacyStatus.WAR -> FrontlineCrimson.copy(alpha = 0.85f)
                            campaign.diplomacyStates[ownerId] == DiplomacyStatus.ALLIANCE -> AlliedCobalt.copy(alpha = 0.85f)
                            campaign.diplomacyStates[ownerId] == DiplomacyStatus.NON_AGGRESSION -> AmberSupply.copy(alpha = 0.65f)
                            else -> NationMinorColor.copy(alpha = 0.55f)
                        }
                    }
                }

                val path = Path().apply {
                    prov.polygon.forEachIndexed { idx, pt ->
                        val sPt = toScreen(pt.x, pt.y)
                        if (idx == 0) moveTo(sPt.x, sPt.y) else lineTo(sPt.x, sPt.y)
                    }
                    close()
                }

                drawPath(path = path, color = fillColor)

                val isSelected = prov.id == selectedProvinceId
                val isMoveTarget = prov.id in validMoveTargets
                val borderCol = when {
                    isSelected -> BrassGold
                    isMoveTarget -> VictoryEmerald
                    else -> WarRoomObsidian.copy(alpha = 0.85f)
                }
                val borderWidth = when {
                    isSelected -> 3.2f * scale
                    isMoveTarget -> 2.8f * scale
                    else -> 1.3f * scale
                }
                drawPath(
                    path = path,
                    color = borderCol,
                    style = Stroke(width = borderWidth, join = StrokeJoin.Round)
                )
            }

            // 4. Capital Stars, City Dots & Labels
            for (prov in TheaterWorldData.provinces) {
                val center = toScreen(prov.centerX, prov.centerY)
                val forts = campaign.provinceForts[prov.id] ?: 0
                if (prov.isCapital) {
                    drawCircle(
                        color = BrassGold,
                        radius = 4.5f * scale,
                        center = center + Offset(0f, -10f * scale)
                    )
                } else if (prov.victoryPoints >= 6) {
                    drawCircle(
                        color = ParchmentText.copy(alpha = 0.8f),
                        radius = 2.5f * scale,
                        center = center + Offset(0f, -9f * scale)
                    )
                }
                if (forts > 0) {
                    drawRect(
                        color = AmberSupply,
                        topLeft = center + Offset(-16f * scale, -14f * scale),
                        size = Size(5f * scale, 5f * scale)
                    )
                }

                // Draw province name when readable
                if (scale >= 1.0f) {
                    val labelLayout = textMeasurer.measure(
                        text = prov.name,
                        style = TextStyle(
                            color = ParchmentText.copy(alpha = 0.92f),
                            fontSize = (8.5f * scale.coerceAtMost(1.45f)).sp,
                            fontWeight = if (prov.isCapital) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                    drawText(
                        textLayoutResult = labelLayout,
                        topLeft = Offset(
                            x = center.x - labelLayout.size.width / 2f,
                            y = center.y + 11f * scale
                        )
                    )
                }
            }

            // 5. Movement / Attack Arrows
            val movingDivs = campaign.divisions.filter { it.targetProvinceId.isNotBlank() }
            val groupedArrows = movingDivs.groupBy { it.provinceId to it.targetProvinceId }
            for ((edge, divs) in groupedArrows) {
                val fromProv = TheaterWorldData.provinceMap[edge.first] ?: continue
                val toProv = TheaterWorldData.provinceMap[edge.second] ?: continue
                val start = toScreen(fromProv.centerX, fromProv.centerY)
                val end = toScreen(toProv.centerX, toProv.centerY)
                val isAttack = campaign.provinceOwners[edge.second] != divs.first().nationId
                val arrowColor = if (isAttack) FrontlineCrimson else VictoryEmerald
                drawTacticalArrow(start, end, arrowColor, scale)
            }

            // 6. NATO-Style Division Counters
            val divsByProvince = campaign.divisions.groupBy { it.provinceId }
            for ((provId, stack) in divsByProvince) {
                val prov = TheaterWorldData.provinceMap[provId] ?: continue
                val center = toScreen(prov.centerX, prov.centerY)
                val ownerNation = stack.first().nationId
                val badgeColor = TheaterWorldData.nations[ownerNation]?.color ?: NationMinorColor
                val avgHp = stack.map { it.hp }.average().toFloat() / 100f
                val avgOrg = stack.map { it.org / it.template.maxOrg }.average().toFloat().coerceIn(0f, 1f)
                val hasArmor = stack.any { it.template == DivisionTemplate.ARMOR }

                val boxW = 24f * scale.coerceAtMost(1.5f)
                val boxH = 14f * scale.coerceAtMost(1.5f)
                val topLeft = Offset(center.x - boxW / 2f, center.y - boxH / 2f)

                // Counter body
                drawRect(color = WarRoomObsidian.copy(alpha = 0.92f), topLeft = topLeft, size = Size(boxW, boxH))
                drawRect(
                    color = badgeColor,
                    topLeft = topLeft + Offset(1f, 1f),
                    size = Size(boxW - 2f, boxH - 4f)
                )
                drawRect(
                    color = if (ownerNation == campaign.playerNationId) BrassGold else ParchmentText.copy(alpha = 0.65f),
                    topLeft = topLeft,
                    size = Size(boxW, boxH),
                    style = Stroke(width = 1.4f)
                )

                // NATO symbol inside counter (X for infantry, oval for armor)
                if (hasArmor) {
                    drawOval(
                        color = ParchmentText,
                        topLeft = topLeft + Offset(boxW * 0.25f, 3f),
                        size = Size(boxW * 0.5f, boxH - 8f),
                        style = Stroke(width = 1.3f)
                    )
                } else {
                    drawLine(
                        color = ParchmentText,
                        start = topLeft + Offset(3f, 2f),
                        end = topLeft + Offset(boxW - 3f, boxH - 5f),
                        strokeWidth = 1.2f
                    )
                    drawLine(
                        color = ParchmentText,
                        start = topLeft + Offset(boxW - 3f, 2f),
                        end = topLeft + Offset(3f, boxH - 5f),
                        strokeWidth = 1.2f
                    )
                }

                // HP (green) & ORG (orange) micro-bars at bottom of counter
                drawRect(
                    color = VictoryEmerald,
                    topLeft = topLeft + Offset(1f, boxH - 3.5f),
                    size = Size((boxW - 2f) * avgHp.coerceIn(0.1f, 1f), 1.6f)
                )
                drawRect(
                    color = AmberSupply,
                    topLeft = topLeft + Offset(1f, boxH - 1.8f),
                    size = Size((boxW - 2f) * avgOrg.coerceIn(0.05f, 1f), 1.5f)
                )

                // Division count pill
                val countText = textMeasurer.measure(
                    text = "${stack.size}",
                    style = TextStyle(color = ParchmentText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                )
                drawCircle(
                    color = WarRoomObsidian,
                    radius = 6.5f,
                    center = topLeft + Offset(boxW, 0f)
                )
                drawText(
                    textLayoutResult = countText,
                    topLeft = topLeft + Offset(boxW - countText.size.width / 2f, -countText.size.height / 2f)
                )
            }

            // 7. Active Frontline Combat Bubbles
            for (bubble in campaign.combatBubbles) {
                val prov = TheaterWorldData.provinceMap[bubble.provinceId] ?: continue
                val center = toScreen(prov.centerX, prov.centerY) + Offset(0f, -18f * scale)
                val isWinning = if (bubble.attackerNationId == campaign.playerNationId) {
                    bubble.attackerAdvantagePct >= 50
                } else {
                    bubble.attackerAdvantagePct < 50
                }
                val bubbleColor = if (isWinning) VictoryEmerald else FrontlineCrimson
                drawCircle(color = WarRoomObsidian, radius = 12f, center = center)
                drawCircle(color = bubbleColor, radius = 10.5f, center = center)
                val pctLayout = textMeasurer.measure(
                    text = "${bubble.attackerAdvantagePct}%",
                    style = TextStyle(color = ParchmentText, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = pctLayout,
                    topLeft = Offset(center.x - pctLayout.size.width / 2f, center.y - pctLayout.size.height / 2f)
                )
            }
        }

        // Top-right Map Mode Switcher & Zoom Reset
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarRoomSurface.copy(alpha = 0.92f))
                    .border(1.dp, WarRoomCardBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MapMode.entries.forEach { mode ->
                    val selected = mapMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { onMapModeSelected(mode) },
                        label = {
                            Text(
                                text = when (mode) {
                                    MapMode.POLITICAL -> "POL"
                                    MapMode.TERRAIN_SUPPLY -> "TER"
                                    MapMode.DIPLOMACY -> "DIP"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            FilledTonalIconButton(
                onClick = {
                    scale = 1.15f
                    panOffset = Offset.Zero
                },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("reset_map_zoom_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Reset map camera",
                    tint = BrassGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawTacticalArrow(
    start: Offset,
    end: Offset,
    color: Color,
    scale: Float
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val angle = atan2(dy, dx)
    val len = hypot(dx, dy)
    if (len < 12f) return
    val shortenedEnd = Offset(
        x = start.x + cos(angle) * (len - 10f),
        y = start.y + sin(angle) * (len - 10f)
    )

    drawLine(
        color = color.copy(alpha = 0.9f),
        start = start,
        end = shortenedEnd,
        strokeWidth = 3.2f * scale.coerceAtMost(1.5f),
        cap = StrokeCap.Round
    )
    val headLen = 9f * scale.coerceAtMost(1.4f)
    val leftWing = Offset(
        x = shortenedEnd.x - headLen * cos(angle - 0.55f),
        y = shortenedEnd.y - headLen * sin(angle - 0.55f)
    )
    val rightWing = Offset(
        x = shortenedEnd.x - headLen * cos(angle + 0.55f),
        y = shortenedEnd.y - headLen * sin(angle + 0.55f)
    )
    drawLine(color, shortenedEnd, leftWing, strokeWidth = 2.6f, cap = StrokeCap.Round)
    drawLine(color, shortenedEnd, rightWing, strokeWidth = 2.6f, cap = StrokeCap.Round)
}
