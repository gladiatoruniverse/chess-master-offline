package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChessDarkColorScheme = darkColorScheme(
    primary = ChampionshipGold,
    onPrimary = DeepSlateTeal,
    primaryContainer = ElevatedTealCard,
    onPrimaryContainer = BrightGoldHighlight,
    secondary = WarmAmberWood,
    onSecondary = DeepSlateTeal,
    secondaryContainer = Color(0xFF3A2A1D),
    onSecondaryContainer = Color(0xFFF6DEC2),
    tertiary = EmeraldSuccess,
    onTertiary = DeepSlateTeal,
    background = DeepSlateTeal,
    onBackground = IvoryCreamText,
    surface = CypressTealSurface,
    onSurface = IvoryCreamText,
    surfaceVariant = ElevatedTealCard,
    onSurfaceVariant = MutedSageText,
    error = CrimsonCheckAlert,
    onError = Color.White,
    outline = Color(0xFF2E4F56)
)

private val ChessLightColorScheme = lightColorScheme(
    primary = ForestTealPrimary,
    onPrimary = IvoryCreamText,
    primaryContainer = WarmSandCard,
    onPrimaryContainer = ForestTealPrimary,
    secondary = BurnishedGoldAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E5AB),
    onSecondaryContainer = Color(0xFF3E2B00),
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = WarmParchmentBg,
    onBackground = DeepCharcoalText,
    surface = IvoryWoodSurface,
    onSurface = DeepCharcoalText,
    surfaceVariant = WarmSandCard,
    onSurfaceVariant = SlateMutedText,
    error = CrimsonCheckAlert,
    onError = Color.White,
    outline = Color(0xFFD2C7B2)
)

@Composable
fun ChessMasterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ChessDarkColorScheme else ChessLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ChessMasterTheme(darkTheme = darkTheme, content = content)
}
