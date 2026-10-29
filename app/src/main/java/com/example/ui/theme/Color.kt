package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Dark Teal & Artisanal Wood Grandmaster Palette
val DeepSlateTeal = Color(0xFF0B1D21)
val CypressTealSurface = Color(0xFF132A30)
val ElevatedTealCard = Color(0xFF1A363E)
val ChampionshipGold = Color(0xFFD4AF37)
val BrightGoldHighlight = Color(0xFFF0C441)
val WarmAmberWood = Color(0xFFC88A4B)
val IvoryCreamText = Color(0xFFF5F2EB)
val MutedSageText = Color(0xFF9FB3B8)

// Light Theme Parchment & Forest Teal Palette
val WarmParchmentBg = Color(0xFFF6F3EC)
val IvoryWoodSurface = Color(0xFFFFFFFF)
val WarmSandCard = Color(0xFFEDE6D6)
val ForestTealPrimary = Color(0xFF0F3D3E)
val BurnishedGoldAccent = Color(0xFFB8860B)
val DeepCharcoalText = Color(0xFF1B2628)
val SlateMutedText = Color(0xFF526568)

// Semantic Status & Board Highlights
val EmeraldSuccess = Color(0xFF2EBD85)
val CrimsonCheckAlert = Color(0xFFE05252)
val SelectedSquareOverlay = Color(0x88D4AF37)
val LastMoveSquareOverlay = Color(0x55E5C158)
val HintSquareOverlay = Color(0x882EBD85)

enum class BoardThemePalette(
    val id: String,
    val displayName: String,
    val lightSquare: Color,
    val darkSquare: Color,
    val borderWood: Color,
    val coordTextLight: Color,
    val coordTextDark: Color
) {
    WARM_WOOD(
        id = "WARM_WOOD",
        displayName = "Warm Royal Wood",
        lightSquare = Color(0xFFEDD6B0),
        darkSquare = Color(0xFF8B5A2B),
        borderWood = Color(0xFF4A2C11),
        coordTextLight = Color(0xFFEDD6B0),
        coordTextDark = Color(0xFF4A2C11)
    ),
    TOURNAMENT_GREEN(
        id = "TOURNAMENT_GREEN",
        displayName = "Tournament Green",
        lightSquare = Color(0xFFEEEED2),
        darkSquare = Color(0xFF769656),
        borderWood = Color(0xFF2E4021),
        coordTextLight = Color(0xFFEEEED2),
        coordTextDark = Color(0xFF2E4021)
    ),
    DARK_TEAL_GOLD(
        id = "DARK_TEAL_GOLD",
        displayName = "Dark Teal & Gold",
        lightSquare = Color(0xFFD8E6E7),
        darkSquare = Color(0xFF24525A),
        borderWood = Color(0xFF0E272C),
        coordTextLight = Color(0xFFD4AF37),
        coordTextDark = Color(0xFF0E272C)
    ),
    CLASSIC_MARBLE(
        id = "CLASSIC_MARBLE",
        displayName = "Classic Marble",
        lightSquare = Color(0xFFF0F0F0),
        darkSquare = Color(0xFF68737A),
        borderWood = Color(0xFF2B3236),
        coordTextLight = Color(0xFFF0F0F0),
        coordTextDark = Color(0xFF2B3236)
    );

    companion object {
        fun fromId(id: String): BoardThemePalette =
            entries.firstOrNull { it.id == id } ?: WARM_WOOD
    }
}

enum class PieceVisualStyle(
    val id: String,
    val displayName: String,
    val whiteFill: Color,
    val whiteStroke: Color,
    val whiteInnerAccent: Color,
    val blackFill: Color,
    val blackStroke: Color,
    val blackInnerAccent: Color
) {
    CLASSIC_STAUNTON(
        id = "CLASSIC_STAUNTON",
        displayName = "Classic Staunton Ivory",
        whiteFill = Color(0xFFFAF6EE),
        whiteStroke = Color(0xFF2B1E16),
        whiteInnerAccent = Color(0xFFD6C5A9),
        blackFill = Color(0xFF23272A),
        blackStroke = Color(0xFFF5E6C8),
        blackInnerAccent = Color(0xFF6A737B)
    ),
    ROYAL_GOLD(
        id = "ROYAL_GOLD",
        displayName = "Royal Gold & Obsidian",
        whiteFill = Color(0xFFF9E596),
        whiteStroke = Color(0xFF3A2807),
        whiteInnerAccent = Color(0xFFD4AF37),
        blackFill = Color(0xFF141D21),
        blackStroke = Color(0xFFD4AF37),
        blackInnerAccent = Color(0xFF456268)
    ),
    WARM_WALNUT(
        id = "WARM_WALNUT",
        displayName = "Carved Maple & Ebony",
        whiteFill = Color(0xFFF5DEB3),
        whiteStroke = Color(0xFF3E2411),
        whiteInnerAccent = Color(0xFFC89D66),
        blackFill = Color(0xFF361F10),
        blackStroke = Color(0xFFEAC896),
        blackInnerAccent = Color(0xFF7C4D2B)
    );

    companion object {
        fun fromId(id: String): PieceVisualStyle =
            entries.firstOrNull { it.id == id } ?: CLASSIC_STAUNTON
    }
}
