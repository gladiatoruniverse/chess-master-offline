package com.example.domain

data class ChessBoardState(
    val pieces: List<Piece?>, // Size 64, indexed by rank * 8 + file
    val activeColor: PieceColor = PieceColor.WHITE,
    val castlingRights: CastlingRights = CastlingRights(),
    val enPassantTarget: Square? = null,
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1,
    val positionHistory: List<String> = emptyList(), // Core FEN signatures for 3-fold repetition
    val moveHistory: List<ChessMove> = emptyList(),
    val status: GameStatus = GameStatus.ONGOING,
    val winner: PieceColor? = null
) {
    fun pieceAt(square: Square): Piece? {
        if (!square.isValid) return null
        return pieces[square.index]
    }

    fun pieceAt(file: Int, rank: Int): Piece? {
        if (file !in 0..7 || rank !in 0..7) return null
        return pieces[rank * 8 + file]
    }

    fun findKingSquare(color: PieceColor): Square? {
        for (i in 0 until 64) {
            val p = pieces[i]
            if (p != null && p.color == color && p.type == PieceType.KING) {
                return Square.fromIndex(i)
            }
        }
        return null
    }

    fun corePositionSignature(): String {
        val sb = StringBuilder()
        for (rank in 7 downTo 0) {
            var emptyCount = 0
            for (file in 0..7) {
                val p = pieceAt(file, rank)
                if (p == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    sb.append(p.toFenChar())
                }
            }
            if (emptyCount > 0) sb.append(emptyCount)
            if (rank > 0) sb.append('/')
        }
        sb.append(' ').append(activeColor.fenChar)
        sb.append(' ').append(castlingRights.toFenString())
        sb.append(' ').append(enPassantTarget?.toAlgebraic() ?: "-")
        return sb.toString()
    }

    fun toFen(): String {
        return "${corePositionSignature()} $halfMoveClock $fullMoveNumber"
    }

    fun capturedPiecesBy(capturingColor: PieceColor): List<PieceType> {
        val targetColor = capturingColor.opposite()
        val startingCounts = mapOf(
            PieceType.QUEEN to 1,
            PieceType.ROOK to 2,
            PieceType.BISHOP to 2,
            PieceType.KNIGHT to 2,
            PieceType.PAWN to 8
        )
        val currentCounts = mutableMapOf<PieceType, Int>()
        for (p in pieces) {
            if (p != null && p.color == targetColor && p.type != PieceType.KING) {
                currentCounts[p.type] = (currentCounts[p.type] ?: 0) + 1
            }
        }
        val captured = mutableListOf<PieceType>()
        for ((type, startCount) in startingCounts) {
            val rem = currentCounts[type] ?: 0
            val diff = (startCount - rem).coerceAtLeast(0)
            repeat(diff) { captured.add(type) }
        }
        return captured
    }

    fun materialBalanceFor(color: PieceColor): Int {
        var myPoints = 0
        var oppPoints = 0
        for (p in pieces) {
            if (p != null) {
                if (p.color == color) myPoints += p.type.materialValue
                else oppPoints += p.type.materialValue
            }
        }
        return myPoints - oppPoints
    }

    companion object {
        const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}
