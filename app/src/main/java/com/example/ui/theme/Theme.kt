package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IronFrontColorScheme = darkColorScheme(
    primary = BrassGold,
    onPrimary = WarRoomObsidian,
    primaryContainer = BrassGoldContainer,
    onPrimaryContainer = BrassGold,
    secondary = OliveSteelLight,
    onSecondary = WarRoomObsidian,
    secondaryContainer = WarRoomSurfaceElevated,
    onSecondaryContainer = ParchmentText,
    tertiary = AlliedCobalt,
    onTertiary = ParchmentText,
    error = FrontlineCrimson,
    onError = ParchmentText,
    errorContainer = FrontlineCrimsonDark,
    onErrorContainer = ParchmentText,
    background = WarRoomObsidian,
    onBackground = ParchmentText,
    surface = WarRoomSurface,
    onSurface = ParchmentText,
    surfaceVariant = WarRoomSurfaceElevated,
    onSurfaceVariant = MutedSteelText,
    outline = WarRoomCardBorder,
    outlineVariant = BrassGoldMuted
)

@Composable
fun IronFrontTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IronFrontColorScheme,
        typography = IronFrontTypography,
        content = content
    )
}
