package com.example.domain

enum class PieceColor(val displayName: String, val fenChar: Char) {
    WHITE("White", 'w'),
    BLACK("Black", 'b');

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

enum class PieceType(
    val displayName: String,
    val sanChar: String,
    val fenLower: Char,
    val materialValue: Int,
    val centipawnValue: Int
) {
    KING("King", "K", 'k', 0, 20000),
    QUEEN("Queen", "Q", 'q', 9, 900),
    ROOK("Rook", "R", 'r', 5, 500),
    BISHOP("Bishop", "B", 'b', 3, 330),
    KNIGHT("Knight", "N", 'n', 3, 320),
    PAWN("Pawn", "", 'p', 1, 100);

    companion object {
        fun fromFenChar(c: Char): PieceType? = when (c.lowercaseChar()) {
            'k' -> KING
            'q' -> QUEEN
            'r' -> ROOK
            'b' -> BISHOP
            'n' -> KNIGHT
            'p' -> PAWN
            else -> null
        }
    }
}

data class Piece(
    val type: PieceType,
    val color: PieceColor
) {
    fun toFenChar(): Char =
        if (color == PieceColor.WHITE) type.fenLower.uppercaseChar() else type.fenLower
}

data class Square(
    val file: Int, // 0 = 'a' .. 7 = 'h'
    val rank: Int  // 0 = '1' .. 7 = '8'
) {
    val isValid: Boolean get() = file in 0..7 && rank in 0..7
    val index: Int get() = rank * 8 + file
    val isLightSquare: Boolean get() = (file + rank) % 2 != 0

    fun toAlgebraic(): String {
        if (!isValid) return "--"
        val fileChar = ('a'.code + file).toChar()
        val rankChar = ('1'.code + rank).toChar()
        return "$fileChar$rankChar"
    }

    companion object {
        fun fromIndex(index: Int): Square = Square(index % 8, index / 8)

        fun fromAlgebraic(alg: String): Square? {
            val cleaned = alg.trim().lowercase()
            if (cleaned.length != 2) return null
            val file = cleaned[0].code - 'a'.code
            val rank = cleaned[1].code - '1'.code
            val sq = Square(file, rank)
            return if (sq.isValid) sq else null
        }
    }
}

data class CastlingRights(
    val whiteKingside: Boolean = true,
    val whiteQueenside: Boolean = true,
    val blackKingside: Boolean = true,
    val blackQueenside: Boolean = true
) {
    fun toFenString(): String {
        val sb = StringBuilder()
        if (whiteKingside) sb.append('K')
        if (whiteQueenside) sb.append('Q')
        if (blackKingside) sb.append('k')
        if (blackQueenside) sb.append('q')
        return if (sb.isEmpty()) "-" else sb.toString()
    }

    companion object {
        fun fromFenString(str: String): CastlingRights {
            if (str == "-" || str.isBlank()) {
                return CastlingRights(false, false, false, false)
            }
            return CastlingRights(
                whiteKingside = str.contains('K'),
                whiteQueenside = str.contains('Q'),
                blackKingside = str.contains('k'),
                blackQueenside = str.contains('q')
            )
        }
    }
}

enum class GameStatus(val isTerminal: Boolean, val label: String) {
    ONGOING(false, "In Progress"),
    CHECK(false, "Check"),
    CHECKMATE(true, "Checkmate"),
    STALEMATE(true, "Stalemate"),
    DRAW_FIFTY_MOVE(true, "Draw (50-Move Rule)"),
    DRAW_REPETITION(true, "Draw (Threefold Repetition)"),
    DRAW_INSUFFICIENT_MATERIAL(true, "Draw (Insufficient Material)"),
    DRAW_AGREEMENT(true, "Draw by Agreement"),
    RESIGNED(true, "Resignation"),
    TIMEOUT(true, "Time Out")
}

data class ChessMove(
    val from: Square,
    val to: Square,
    val movedPiece: Piece,
    val capturedPiece: Piece? = null,
    val promotion: PieceType? = null,
    val isKingsideCastle: Boolean = false,
    val isQueensideCastle: Boolean = false,
    val isEnPassant: Boolean = false,
    val san: String = "",
    val previousFen: String = "",
    val resultingFen: String = ""
) {
    fun toUci(): String {
        val promoSuffix = promotion?.fenLower?.toString() ?: ""
        return "${from.toAlgebraic()}${to.toAlgebraic()}$promoSuffix"
    }
}

enum class GameMode(val displayName: String) {
    VS_COMPUTER("Play vs Computer"),
    TWO_PLAYER("Two Players")
}

enum class PlayerColorChoice(val displayName: String) {
    WHITE("White"),
    BLACK("Black"),
    RANDOM("Random")
}

data class DifficultyConfig(
    val level: Int,
    val title: String,
    val subtitle: String,
    val searchDepth: Int,
    val maxNodes: Int,
    val movetimeMs: Long,
    val noiseCentipawns: Int,
    val useQuiescence: Boolean
)

object EngineDifficultyLevels {
    val ALL: List<DifficultyConfig> = listOf(
        DifficultyConfig(1, "Beginner", "Depth 1 • Casual introductory play", 1, 250, 220L, 180, false),
        DifficultyConfig(2, "Novice", "Depth 1+ • Spots basic captures", 1, 600, 260L, 120, false),
        DifficultyConfig(3, "Casual", "Depth 2 • Avoids immediate blunders", 2, 1500, 320L, 85, false),
        DifficultyConfig(4, "Club Hobbyist", "Depth 2+ • Tactical awareness", 2, 3500, 380L, 50, true),
        DifficultyConfig(5, "Intermediate", "Depth 3 • Solid positional foundation", 3, 7500, 450L, 30, true),
        DifficultyConfig(6, "Tournament Player", "Depth 3+ • Calculates tactical exchanges", 3, 14000, 550L, 15, true),
        DifficultyConfig(7, "Advanced", "Depth 4 • Strong tactical vision", 4, 28000, 700L, 8, true),
        DifficultyConfig(8, "Expert", "Depth 4+ • Deep quiescence & endgame skill", 4, 50000, 900L, 3, true),
        DifficultyConfig(9, "Master", "Depth 5 • Sharp calculation & zero noise", 5, 90000, 1200L, 0, true),
        DifficultyConfig(10, "Champion", "Depth 5+ • Full search strength & deep PV", 5, 160000, 1600L, 0, true)
    )

    fun getByLevel(level: Int): DifficultyConfig =
        ALL.firstOrNull { it.level == level.coerceIn(1, 10) } ?: ALL[4]
}

data class TimeControlOption(
    val minutes: Int, // 0 = Untimed
    val incrementSeconds: Int,
    val label: String
) {
    val isTimed: Boolean get() = minutes > 0
    val initialTimeMs: Long get() = minutes * 60_000L
    val incrementMs: Long get() = incrementSeconds * 1_000L
}

object ClockPresets {
    val MINUTE_OPTIONS = listOf(0, 1, 3, 5, 10, 15)
    val INCREMENT_OPTIONS = listOf(0, 2, 3, 5, 10)

    fun formatLabel(minutes: Int, incrementSeconds: Int): String {
        if (minutes <= 0) return "Untimed"
        return if (incrementSeconds > 0) "${minutes}m + ${incrementSeconds}s" else "${minutes} min"
    }
}
