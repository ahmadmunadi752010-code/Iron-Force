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
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TheaterWorldData
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private const val WORLD_ASPECT_RATIO = 1.85f

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
    val capitalId = TheaterWorldData.nations[campaign.playerNationId]?.capitalProvinceId
    val capitalProv = TheaterWorldData.provinceMap[capitalId]

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1.35f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var didInitialFocus by remember(campaign.campaignId, campaign.playerNationId) { mutableStateOf(false) }

    val textMeasurer = rememberTextMeasurer()

    fun worldDims(w: Float, h: Float): Pair<Float, Float> {
        val baseH = minOf(h * 0.78f, w * 0.96f).coerceAtLeast(1f)
        val baseW = baseH * WORLD_ASPECT_RATIO
        return baseW to baseH
    }

    fun clampPan(raw: Offset, curScale: Float, w: Float, h: Float): Offset {
        if (w <= 0f || h <= 0f) return raw
        val (baseW, baseH) = worldDims(w, h)
        val maxPanX = maxOf(w * 0.45f, baseW * 0.56f * curScale)
        val maxPanY = maxOf(h * 0.45f, baseH * 0.56f * curScale)
        return Offset(
            x = raw.x.coerceIn(-maxPanX, maxPanX),
            y = raw.y.coerceIn(-maxPanY, maxPanY)
        )
    }

    fun focusOnCapital(targetScale: Float = 1.45f) {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return
        val w = canvasSize.width.toFloat()
        val h = canvasSize.height.toFloat()
        val (baseW, baseH) = worldDims(w, h)
        val tx = capitalProv?.centerX ?: 0.5f
        val ty = capitalProv?.centerY ?: 0.38f
        scale = targetScale
        panOffset = clampPan(
            Offset(
                x = -(tx - 0.5f) * baseW * targetScale,
                y = -(ty - 0.5f) * baseH * targetScale
            ),
            targetScale,
            w,
            h
        )
    }

    fun fitWorldOverview() {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return
        val w = canvasSize.width.toFloat()
        val h = canvasSize.height.toFloat()
        val (baseW, _) = worldDims(w, h)
        val fitScale = ((w * 0.95f) / baseW).coerceIn(0.48f, 1.0f)
        scale = fitScale
        panOffset = Offset.Zero
    }

    LaunchedEffect(canvasSize, campaign.campaignId, campaign.playerNationId) {
        if (!didInitialFocus && canvasSize.width > 0 && canvasSize.height > 0) {
            focusOnCapital(targetScale = 1.40f)
            didInitialFocus = true
        }
    }

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
            .onSizeChanged { canvasSize = it }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val nextScale = (scale * zoom).coerceIn(0.45f, 4.5f)
                        scale = nextScale
                        panOffset = clampPan(panOffset + pan, nextScale, w, h)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tap ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val (baseW, baseH) = worldDims(w, h)
                        val originX = (w - baseW) / 2f
                        val originY = (h - baseH) / 2f

                        val cx = w / 2f
                        val cy = h / 2f
                        val worldX = (tap.x - cx - panOffset.x) / scale + cx
                        val worldY = (tap.y - cy - panOffset.y) / scale + cy
                        val normX = (worldX - originX) / baseW
                        val normY = (worldY - originY) / baseH

                        val polyHit = TheaterWorldData.provinces.firstOrNull { prov ->
                            pointInPolygon(normX, normY, prov.polygon)
                        }
                        if (polyHit != null) {
                            onProvinceTapped(polyHit.id)
                            return@detectTapGestures
                        }

                        val nearest = TheaterWorldData.provinces.minByOrNull { prov ->
                            hypot(
                                ((prov.centerX - normX) * WORLD_ASPECT_RATIO).toDouble(),
                                (prov.centerY - normY).toDouble()
                            )
                        }
                        if (nearest != null) {
                            val dist = hypot(
                                ((nearest.centerX - normX) * WORLD_ASPECT_RATIO).toDouble(),
                                (nearest.centerY - normY).toDouble()
                            )
                            if (dist < 0.12) {
                                onProvinceTapped(nearest.id)
                            }
                        }
                    }
                }
                .testTag("theater_map_canvas")
        ) {
            val w = size.width
            val h = size.height
            val (baseW, baseH) = worldDims(w, h)
            val originX = (w - baseW) / 2f
            val originY = (h - baseH) / 2f
            val cx = w / 2f
            val cy = h / 2f

            fun toScreen(nx: Float, ny: Float): Offset {
                val wx = originX + nx * baseW
                val wy = originY + ny * baseH
                return Offset(
                    x = cx + (wx - cx) * scale + panOffset.x,
                    y = cy + (wy - cy) * scale + panOffset.y
                )
            }

            // 1. Cartographic World Latitude / Longitude Graticule Lines
            val gridColor = OceanCoastal.copy(alpha = 0.65f)
            for (i in 0..12) {
                val tx = i / 12f
                drawLine(gridColor, toScreen(tx, 0f), toScreen(tx, 1f), strokeWidth = 1f)
            }
            for (j in 0..6) {
                val ty = j / 6f
                drawLine(gridColor, toScreen(0f, ty), toScreen(1f, ty), strokeWidth = 1f)
            }
            // Subtle Equator dashed guide
            drawLine(
                color = BrassGold.copy(alpha = 0.18f),
                start = toScreen(0f, 0.59f),
                end = toScreen(1f, 0.59f),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
            )

            // 2. Stylized World Continent Landmasses Backdrop
            for (continent in TheaterWorldData.continentSilhouettes) {
                val landPath = Path().apply {
                    continent.forEachIndexed { idx, pt ->
                        val sPt = toScreen(pt.x, pt.y)
                        if (idx == 0) moveTo(sPt.x, sPt.y) else lineTo(sPt.x, sPt.y)
                    }
                    close()
                }
                drawPath(path = landPath, color = WorldLandBase)
                drawPath(
                    path = landPath,
                    color = OliveSteel.copy(alpha = 0.45f),
                    style = Stroke(width = 1.6f * scale.coerceAtMost(1.5f), join = StrokeJoin.Round)
                )
            }

            // Subtle Ocean & Region Cartographic Labels
            val regionMarks = listOf(
                Triple("PACIFIC", 0.08f, 0.66f),
                Triple("ATLANTIC", 0.36f, 0.46f),
                Triple("INDIAN OCEAN", 0.72f, 0.72f)
            )
            for ((label, rx, ry) in regionMarks) {
                val pos = toScreen(rx, ry)
                val layout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = ParchmentText.copy(alpha = 0.16f),
                        fontSize = (10f * scale.coerceIn(0.7f, 1.3f)).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(pos.x - layout.size.width / 2f, pos.y - layout.size.height / 2f)
                )
            }

            // 3. Adjacency & Global Sea/Supply Links (with clean trans-Pacific edge wrapping)
            val drawnLinks = HashSet<String>()
            for (prov in TheaterWorldData.provinces) {
                val pA = toScreen(prov.centerX, prov.centerY)
                for (nId in prov.neighbors) {
                    val key = if (prov.id < nId) "${prov.id}-$nId" else "$nId-${prov.id}"
                    if (drawnLinks.add(key)) {
                        val nb = TheaterWorldData.provinceMap[nId] ?: continue
                        val sameOwner = campaign.provinceOwners[prov.id] == campaign.provinceOwners[nId]
                        val linkColor = if (sameOwner) ParchmentText.copy(alpha = 0.18f) else FrontlineCrimson.copy(alpha = 0.36f)
                        val strokeW = if (sameOwner) 1.3f * scale.coerceAtMost(1.5f) else 2.0f * scale.coerceAtMost(1.6f)
                        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))

                        if (abs(nb.centerX - prov.centerX) > 0.60f) {
                            // Trans-Pacific wrap-around edge stubs (instead of slashing across continents)
                            val aToWest = prov.centerX < 0.5f
                            val edgeA = toScreen(if (aToWest) 0.0f else 1.0f, (prov.centerY + nb.centerY) / 2f)
                            val edgeB = toScreen(if (aToWest) 1.0f else 0.0f, (prov.centerY + nb.centerY) / 2f)
                            val pB = toScreen(nb.centerX, nb.centerY)
                            drawLine(linkColor, pA, edgeA, strokeWidth = strokeW, pathEffect = dash)
                            drawLine(linkColor, pB, edgeB, strokeWidth = strokeW, pathEffect = dash)
                        } else {
                            val pB = toScreen(nb.centerX, nb.centerY)
                            drawLine(
                                color = linkColor,
                                start = pA,
                                end = pB,
                                strokeWidth = strokeW,
                                pathEffect = if (sameOwner) null else dash
                            )
                        }
                    }
                }
            }

            // 4. Province Polygons
            for (prov in TheaterWorldData.provinces) {
                val ownerId = campaign.provinceOwners[prov.id] ?: prov.initialOwnerId
                val fillColor = when (mapMode) {
                    MapMode.POLITICAL -> {
                        val base = TheaterWorldData.nations[ownerId]?.color ?: NationMinorColor
                        base.copy(alpha = 0.88f)
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
                    else -> WarRoomObsidian.copy(alpha = 0.88f)
                }
                val borderWidth = when {
                    isSelected -> 2.8f * scale.coerceAtMost(1.8f)
                    isMoveTarget -> 2.4f * scale.coerceAtMost(1.8f)
                    else -> 1.1f * scale.coerceAtMost(1.5f)
                }
                drawPath(
                    path = path,
                    color = borderCol,
                    style = Stroke(width = borderWidth, join = StrokeJoin.Round)
                )
            }

            // 5. Capital Stars, City Dots, Forts & Labels
            for (prov in TheaterWorldData.provinces) {
                val center = toScreen(prov.centerX, prov.centerY)
                val forts = campaign.provinceForts[prov.id] ?: 0
                val markerScale = scale.coerceIn(0.7f, 1.5f)
                if (prov.isCapital) {
                    drawCircle(
                        color = BrassGold,
                        radius = 3.8f * markerScale,
                        center = center + Offset(0f, -8f * markerScale)
                    )
                } else if (prov.victoryPoints >= 7) {
                    drawCircle(
                        color = ParchmentText.copy(alpha = 0.8f),
                        radius = 2.2f * markerScale,
                        center = center + Offset(0f, -7.5f * markerScale)
                    )
                }
                if (forts > 0 && scale >= 0.8f) {
                    drawRect(
                        color = AmberSupply,
                        topLeft = center + Offset(-13f * markerScale, -11f * markerScale),
                        size = Size(4.2f * markerScale, 4.2f * markerScale)
                    )
                }

                // Show all labels when zoomed in; show only capitals in World Overview to avoid clutter
                val showLabel = scale >= 1.05f || (prov.isCapital && scale >= 0.55f)
                if (showLabel) {
                    val labelLayout = textMeasurer.measure(
                        text = prov.name,
                        style = TextStyle(
                            color = if (prov.isCapital) BrassGold.copy(alpha = 0.96f) else ParchmentText.copy(alpha = 0.90f),
                            fontSize = (7.2f * scale.coerceIn(0.85f, 1.35f)).sp,
                            fontWeight = if (prov.isCapital) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                    drawText(
                        textLayoutResult = labelLayout,
                        topLeft = Offset(
                            x = center.x - labelLayout.size.width / 2f,
                            y = center.y + 9f * markerScale
                        )
                    )
                }
            }

            // 6. Movement / Attack Arrows
            val movingDivs = campaign.divisions.filter { it.targetProvinceId.isNotBlank() }
            val groupedArrows = movingDivs.groupBy { it.provinceId to it.targetProvinceId }
            for ((edge, divs) in groupedArrows) {
                val fromProv = TheaterWorldData.provinceMap[edge.first] ?: continue
                val toProv = TheaterWorldData.provinceMap[edge.second] ?: continue
                val isAttack = campaign.provinceOwners[edge.second] != divs.first().nationId
                val arrowColor = if (isAttack) FrontlineCrimson else VictoryEmerald
                val start = toScreen(fromProv.centerX, fromProv.centerY)
                if (abs(toProv.centerX - fromProv.centerX) > 0.60f) {
                    val goWest = fromProv.centerX < 0.5f
                    val edgeExit = toScreen(if (goWest) 0.01f else 0.99f, (fromProv.centerY + toProv.centerY) / 2f)
                    drawTacticalArrow(start, edgeExit, arrowColor, scale)
                } else {
                    val end = toScreen(toProv.centerX, toProv.centerY)
                    drawTacticalArrow(start, end, arrowColor, scale)
                }
            }

            // 7. NATO-Style Division Counters
            val divsByProvince = campaign.divisions.groupBy { it.provinceId }
            val counterScale = scale.coerceIn(0.65f, 1.45f)
            for ((provId, stack) in divsByProvince) {
                val prov = TheaterWorldData.provinceMap[provId] ?: continue
                val center = toScreen(prov.centerX, prov.centerY)
                val ownerNation = stack.first().nationId
                val badgeColor = TheaterWorldData.nations[ownerNation]?.color ?: NationMinorColor
                val avgHp = stack.map { it.hp }.average().toFloat() / 100f
                val avgOrg = stack.map { it.org / it.template.maxOrg }.average().toFloat().coerceIn(0f, 1f)
                val hasArmor = stack.any { it.template == DivisionTemplate.ARMOR }

                val boxW = 21f * counterScale
                val boxH = 12.5f * counterScale
                val topLeft = Offset(center.x - boxW / 2f, center.y - boxH / 2f)

                drawRect(color = WarRoomObsidian.copy(alpha = 0.92f), topLeft = topLeft, size = Size(boxW, boxH))
                drawRect(
                    color = badgeColor,
                    topLeft = topLeft + Offset(1f, 1f),
                    size = Size(boxW - 2f, boxH - 3.5f)
                )
                drawRect(
                    color = if (ownerNation == campaign.playerNationId) BrassGold else ParchmentText.copy(alpha = 0.65f),
                    topLeft = topLeft,
                    size = Size(boxW, boxH),
                    style = Stroke(width = 1.3f)
                )

                if (hasArmor) {
                    drawOval(
                        color = ParchmentText,
                        topLeft = topLeft + Offset(boxW * 0.25f, 2.5f),
                        size = Size(boxW * 0.5f, (boxH - 7f).coerceAtLeast(2f)),
                        style = Stroke(width = 1.2f)
                    )
                } else {
                    drawLine(
                        color = ParchmentText,
                        start = topLeft + Offset(2.5f, 2f),
                        end = topLeft + Offset(boxW - 2.5f, boxH - 4.5f),
                        strokeWidth = 1.1f
                    )
                    drawLine(
                        color = ParchmentText,
                        start = topLeft + Offset(boxW - 2.5f, 2f),
                        end = topLeft + Offset(2.5f, boxH - 4.5f),
                        strokeWidth = 1.1f
                    )
                }

                // HP (green) & ORG (orange) micro-bars
                drawRect(
                    color = VictoryEmerald,
                    topLeft = topLeft + Offset(1f, boxH - 3.2f),
                    size = Size((boxW - 2f) * avgHp.coerceIn(0.1f, 1f), 1.4f)
                )
                drawRect(
                    color = AmberSupply,
                    topLeft = topLeft + Offset(1f, boxH - 1.6f),
                    size = Size((boxW - 2f) * avgOrg.coerceIn(0.05f, 1f), 1.3f)
                )

                val countText = textMeasurer.measure(
                    text = "${stack.size}",
                    style = TextStyle(color = ParchmentText, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                )
                drawCircle(
                    color = WarRoomObsidian,
                    radius = 5.8f,
                    center = topLeft + Offset(boxW, 0f)
                )
                drawText(
                    textLayoutResult = countText,
                    topLeft = topLeft + Offset(boxW - countText.size.width / 2f, -countText.size.height / 2f)
                )
            }

            // 8. Active Frontline Combat Bubbles
            for (bubble in campaign.combatBubbles) {
                val prov = TheaterWorldData.provinceMap[bubble.provinceId] ?: continue
                val center = toScreen(prov.centerX, prov.centerY) + Offset(0f, -15f * counterScale)
                val isWinning = if (bubble.attackerNationId == campaign.playerNationId) {
                    bubble.attackerAdvantagePct >= 50
                } else {
                    bubble.attackerAdvantagePct < 50
                }
                val bubbleColor = if (isWinning) VictoryEmerald else FrontlineCrimson
                drawCircle(color = WarRoomObsidian, radius = 11f, center = center)
                drawCircle(color = bubbleColor, radius = 9.5f, center = center)
                val pctLayout = textMeasurer.measure(
                    text = "${bubble.attackerAdvantagePct}%",
                    style = TextStyle(color = ParchmentText, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = pctLayout,
                    topLeft = Offset(center.x - pctLayout.size.width / 2f, center.y - pctLayout.size.height / 2f)
                )
            }
        }

        // Top-right Map Mode Switcher & Camera Controls
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

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = { fitWorldOverview() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("world_overview_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "World map overview",
                        tint = BrassGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = { focusOnCapital(targetScale = 1.45f) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reset_map_zoom_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Center on capital",
                        tint = BrassGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun pointInPolygon(x: Float, y: Float, poly: List<MapPoint>): Boolean {
    if (poly.size < 3) return false
    var inside = false
    var j = poly.size - 1
    for (i in poly.indices) {
        val xi = poly[i].x
        val yi = poly[i].y
        val xj = poly[j].x
        val yj = poly[j].y
        val intersects = ((yi > y) != (yj > y)) &&
            (x < (xj - xi) * (y - yi) / ((yj - yi).takeIf { abs(it) > 1e-6f } ?: 1e-6f) + xi)
        if (intersects) inside = !inside
        j = i
    }
    return inside
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
        strokeWidth = 3.0f * scale.coerceIn(0.75f, 1.5f),
        cap = StrokeCap.Round
    )
    val headLen = 8.5f * scale.coerceIn(0.75f, 1.4f)
    val leftWing = Offset(
        x = shortenedEnd.x - headLen * cos(angle - 0.55f),
        y = shortenedEnd.y - headLen * sin(angle - 0.55f)
    )
    val rightWing = Offset(
        x = shortenedEnd.x - headLen * cos(angle + 0.55f),
        y = shortenedEnd.y - headLen * sin(angle + 0.55f)
    )
    drawLine(color, shortenedEnd, leftWing, strokeWidth = 2.4f, cap = StrokeCap.Round)
    drawLine(color, shortenedEnd, rightWing, strokeWidth = 2.4f, cap = StrokeCap.Round)
}
