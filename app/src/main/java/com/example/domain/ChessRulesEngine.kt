package com.example.domain

import kotlin.math.abs

object ChessRulesEngine {

    fun createInitialState(): ChessBoardState = fromFen(ChessBoardState.STARTING_FEN)

    fun fromFen(fen: String): ChessBoardState {
        val parts = fen.trim().split(Regex("\\s+"))
        require(parts.isNotEmpty() && parts[0].isNotBlank()) { "Invalid FEN: empty board" }

        val ranks = parts[0].split("/")
        require(ranks.size == 8) { "Invalid FEN: expected 8 ranks, found ${ranks.size}" }

        val pieces = MutableList<Piece?>(64) { null }
        for (rIdx in 0..7) {
            val rank = 7 - rIdx
            val rowStr = ranks[rIdx]
            var file = 0
            for (ch in rowStr) {
                if (ch.isDigit()) {
                    file += ch.digitToInt()
                } else {
                    val type = PieceType.fromFenChar(ch)
                        ?: throw IllegalArgumentException("Invalid FEN piece char: $ch")
                    val color = if (ch.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                    if (file in 0..7) {
                        pieces[rank * 8 + file] = Piece(type, color)
                    }
                    file++
                }
            }
            require(file == 8) { "Invalid FEN rank width in rank ${rank + 1}" }
        }

        val activeColor = if (parts.getOrNull(1) == "b") PieceColor.BLACK else PieceColor.WHITE
        val castling = CastlingRights.fromFenString(parts.getOrNull(2) ?: "-")
        val epTarget = parts.getOrNull(3)?.let { if (it == "-") null else Square.fromAlgebraic(it) }
        val halfMove = parts.getOrNull(4)?.toIntOrNull() ?: 0
        val fullMove = (parts.getOrNull(5)?.toIntOrNull() ?: 1).coerceAtLeast(1)

        val baseState = ChessBoardState(
            pieces = pieces,
            activeColor = activeColor,
            castlingRights = castling,
            enPassantTarget = epTarget,
            halfMoveClock = halfMove,
            fullMoveNumber = fullMove
        )
        val coreSig = baseState.corePositionSignature()
        val stateWithHistory = baseState.copy(positionHistory = listOf(coreSig))
        return evaluateTerminalStatus(stateWithHistory)
    }

    fun isSquareAttacked(state: ChessBoardState, square: Square, byColor: PieceColor): Boolean {
        // 1. Pawn attacks
        val pawnAttackRank = if (byColor == PieceColor.WHITE) square.rank - 1 else square.rank + 1
        for (df in intArrayOf(-1, 1)) {
            val p = state.pieceAt(square.file + df, pawnAttackRank)
            if (p != null && p.color == byColor && p.type == PieceType.PAWN) return true
        }

        // 2. Knight attacks
        val knightOffsets = arrayOf(
            intArrayOf(-2, -1), intArrayOf(-2, 1), intArrayOf(-1, -2), intArrayOf(-1, 2),
            intArrayOf(1, -2), intArrayOf(1, 2), intArrayOf(2, -1), intArrayOf(2, 1)
        )
        for (offset in knightOffsets) {
            val p = state.pieceAt(square.file + offset[0], square.rank + offset[1])
            if (p != null && p.color == byColor && p.type == PieceType.KNIGHT) return true
        }

        // 3. King attacks
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val p = state.pieceAt(square.file + df, square.rank + dr)
                if (p != null && p.color == byColor && p.type == PieceType.KING) return true
            }
        }

        // 4. Orthogonal sliding (Rook / Queen)
        val orthoDirs = arrayOf(intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1))
        for (dir in orthoDirs) {
            var f = square.file + dir[0]
            var r = square.rank + dir[1]
            while (f in 0..7 && r in 0..7) {
                val p = state.pieceAt(f, r)
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                f += dir[0]
                r += dir[1]
            }
        }

        // 5. Diagonal sliding (Bishop / Queen)
        val diagDirs = arrayOf(intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1))
        for (dir in diagDirs) {
            var f = square.file + dir[0]
            var r = square.rank + dir[1]
            while (f in 0..7 && r in 0..7) {
                val p = state.pieceAt(f, r)
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                f += dir[0]
                r += dir[1]
            }
        }

        return false
    }

    fun isInCheck(state: ChessBoardState, color: PieceColor): Boolean {
        val kingSq = state.findKingSquare(color) ?: return false
        return isSquareAttacked(state, kingSq, color.opposite())
    }

    fun generateLegalMoves(state: ChessBoardState): List<ChessMove> {
        val pseudoMoves = generatePseudoLegalMoves(state, state.activeColor)
        val legalMoves = ArrayList<ChessMove>(pseudoMoves.size)
        for (move in pseudoMoves) {
            val nextBoard = applyMoveRaw(state, move)
            if (!isInCheck(nextBoard, state.activeColor)) {
                legalMoves.add(move)
            }
        }
        return legalMoves
    }

    fun generateLegalMovesForSquare(state: ChessBoardState, square: Square): List<ChessMove> {
        val piece = state.pieceAt(square) ?: return emptyList()
        if (piece.color != state.activeColor) return emptyList()
        return generateLegalMoves(state).filter { it.from == square }
    }

    private fun generatePseudoLegalMoves(state: ChessBoardState, color: PieceColor): List<ChessMove> {
        val moves = ArrayList<ChessMove>(48)
        for (rank in 0..7) {
            for (file in 0..7) {
                val from = Square(file, rank)
                val piece = state.pieceAt(from) ?: continue
                if (piece.color != color) continue
                when (piece.type) {
                    PieceType.PAWN -> generatePawnMoves(state, from, piece, moves)
                    PieceType.KNIGHT -> generateKnightMoves(state, from, piece, moves)
                    PieceType.BISHOP -> generateSlidingMoves(state, from, piece, moves, diagOnly = true, orthoOnly = false)
                    PieceType.ROOK -> generateSlidingMoves(state, from, piece, moves, diagOnly = false, orthoOnly = true)
                    PieceType.QUEEN -> generateSlidingMoves(state, from, piece, moves, diagOnly = true, orthoOnly = true)
                    PieceType.KING -> generateKingMoves(state, from, piece, moves)
                }
            }
        }
        return moves
    }

    private fun generatePawnMoves(
        state: ChessBoardState,
        from: Square,
        piece: Piece,
        out: MutableList<ChessMove>
    ) {
        val dir = if (piece.color == PieceColor.WHITE) 1 else -1
        val startRank = if (piece.color == PieceColor.WHITE) 1 else 6
        val promoRank = if (piece.color == PieceColor.WHITE) 7 else 0
        val promoTypes = arrayOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

        // 1. Single step forward
        val oneForward = Square(from.file, from.rank + dir)
        if (oneForward.isValid && state.pieceAt(oneForward) == null) {
            if (oneForward.rank == promoRank) {
                for (pt in promoTypes) {
                    out.add(ChessMove(from = from, to = oneForward, movedPiece = piece, promotion = pt))
                }
            } else {
                out.add(ChessMove(from = from, to = oneForward, movedPiece = piece))
                // 2. Double step from starting rank
                if (from.rank == startRank) {
                    val twoForward = Square(from.file, from.rank + dir * 2)
                    if (twoForward.isValid && state.pieceAt(twoForward) == null) {
                        out.add(ChessMove(from = from, to = twoForward, movedPiece = piece))
                    }
                }
            }
        }

        // 3. Diagonal captures & En Passant
        for (df in intArrayOf(-1, 1)) {
            val capSq = Square(from.file + df, from.rank + dir)
            if (!capSq.isValid) continue
            val targetPiece = state.pieceAt(capSq)
            if (targetPiece != null && targetPiece.color != piece.color) {
                if (capSq.rank == promoRank) {
                    for (pt in promoTypes) {
                        out.add(
                            ChessMove(
                                from = from,
                                to = capSq,
                                movedPiece = piece,
                                capturedPiece = targetPiece,
                                promotion = pt
                            )
                        )
                    }
                } else {
                    out.add(
                        ChessMove(
                            from = from,
                            to = capSq,
                            movedPiece = piece,
                            capturedPiece = targetPiece
                        )
                    )
                }
            } else if (state.enPassantTarget == capSq) {
                val epCapturedSq = Square(capSq.file, from.rank)
                val epPawn = state.pieceAt(epCapturedSq)
                if (epPawn != null && epPawn.color != piece.color && epPawn.type == PieceType.PAWN) {
                    out.add(
                        ChessMove(
                            from = from,
                            to = capSq,
                            movedPiece = piece,
                            capturedPiece = epPawn,
                            isEnPassant = true
                        )
                    )
                }
            }
        }
    }

    private fun generateKnightMoves(
        state: ChessBoardState,
        from: Square,
        piece: Piece,
        out: MutableList<ChessMove>
    ) {
        val offsets = arrayOf(
            intArrayOf(-2, -1), intArrayOf(-2, 1), intArrayOf(-1, -2), intArrayOf(-1, 2),
            intArrayOf(1, -2), intArrayOf(1, 2), intArrayOf(2, -1), intArrayOf(2, 1)
        )
        for (off in offsets) {
            val to = Square(from.file + off[0], from.rank + off[1])
            if (!to.isValid) continue
            val target = state.pieceAt(to)
            if (target == null || target.color != piece.color) {
                out.add(ChessMove(from = from, to = to, movedPiece = piece, capturedPiece = target))
            }
        }
    }

    private fun generateSlidingMoves(
        state: ChessBoardState,
        from: Square,
        piece: Piece,
        out: MutableList<ChessMove>,
        diagOnly: Boolean,
        orthoOnly: Boolean
    ) {
        val dirs = mutableListOf<IntArray>()
        if (orthoOnly) {
            dirs.add(intArrayOf(1, 0))
            dirs.add(intArrayOf(-1, 0))
            dirs.add(intArrayOf(0, 1))
            dirs.add(intArrayOf(0, -1))
        }
        if (diagOnly) {
            dirs.add(intArrayOf(1, 1))
            dirs.add(intArrayOf(1, -1))
            dirs.add(intArrayOf(-1, 1))
            dirs.add(intArrayOf(-1, -1))
        }
        for (dir in dirs) {
            var f = from.file + dir[0]
            var r = from.rank + dir[1]
            while (f in 0..7 && r in 0..7) {
                val to = Square(f, r)
                val target = state.pieceAt(to)
                if (target == null) {
                    out.add(ChessMove(from = from, to = to, movedPiece = piece))
                } else {
                    if (target.color != piece.color) {
                        out.add(ChessMove(from = from, to = to, movedPiece = piece, capturedPiece = target))
                    }
                    break
                }
                f += dir[0]
                r += dir[1]
            }
        }
    }

    private fun generateKingMoves(
        state: ChessBoardState,
        from: Square,
        piece: Piece,
        out: MutableList<ChessMove>
    ) {
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val to = Square(from.file + df, from.rank + dr)
                if (!to.isValid) continue
                val target = state.pieceAt(to)
                if (target == null || target.color != piece.color) {
                    out.add(ChessMove(from = from, to = to, movedPiece = piece, capturedPiece = target))
                }
            }
        }

        // Castling checks
        val opp = piece.color.opposite()
        val homeRank = if (piece.color == PieceColor.WHITE) 0 else 7
        if (from.file == 4 && from.rank == homeRank && !isSquareAttacked(state, from, opp)) {
            val canKingside = if (piece.color == PieceColor.WHITE) {
                state.castlingRights.whiteKingside
            } else {
                state.castlingRights.blackKingside
            }
            if (canKingside) {
                val fSq = Square(5, homeRank)
                val gSq = Square(6, homeRank)
                val hSq = Square(7, homeRank)
                val rook = state.pieceAt(hSq)
                if (rook != null && rook.color == piece.color && rook.type == PieceType.ROOK &&
                    state.pieceAt(fSq) == null && state.pieceAt(gSq) == null &&
                    !isSquareAttacked(state, fSq, opp) && !isSquareAttacked(state, gSq, opp)
                ) {
                    out.add(
                        ChessMove(
                            from = from,
                            to = gSq,
                            movedPiece = piece,
                            isKingsideCastle = true
                        )
                    )
                }
            }

            val canQueenside = if (piece.color == PieceColor.WHITE) {
                state.castlingRights.whiteQueenside
            } else {
                state.castlingRights.blackQueenside
            }
            if (canQueenside) {
                val dSq = Square(3, homeRank)
                val cSq = Square(2, homeRank)
                val bSq = Square(1, homeRank)
                val aSq = Square(0, homeRank)
                val rook = state.pieceAt(aSq)
                if (rook != null && rook.color == piece.color && rook.type == PieceType.ROOK &&
                    state.pieceAt(dSq) == null && state.pieceAt(cSq) == null && state.pieceAt(bSq) == null &&
                    !isSquareAttacked(state, dSq, opp) && !isSquareAttacked(state, cSq, opp)
                ) {
                    out.add(
                        ChessMove(
                            from = from,
                            to = cSq,
                            movedPiece = piece,
                            isQueensideCastle = true
                        )
                    )
                }
            }
        }
    }

    private fun applyMoveRaw(state: ChessBoardState, move: ChessMove): ChessBoardState {
        val nextPieces = state.pieces.toMutableList()
        nextPieces[move.from.index] = null

        if (move.isEnPassant) {
            val epCapturedSq = Square(move.to.file, move.from.rank)
            nextPieces[epCapturedSq.index] = null
        }

        val placedPiece = if (move.promotion != null) {
            Piece(move.promotion, move.movedPiece.color)
        } else {
            move.movedPiece
        }
        nextPieces[move.to.index] = placedPiece

        if (move.isKingsideCastle) {
            val rank = move.from.rank
            val rook = nextPieces[Square(7, rank).index]
            nextPieces[Square(7, rank).index] = null
            nextPieces[Square(5, rank).index] = rook
        } else if (move.isQueensideCastle) {
            val rank = move.from.rank
            val rook = nextPieces[Square(0, rank).index]
            nextPieces[Square(0, rank).index] = null
            nextPieces[Square(3, rank).index] = rook
        }

        // Update castling rights
        var wk = state.castlingRights.whiteKingside
        var wq = state.castlingRights.whiteQueenside
        var bk = state.castlingRights.blackKingside
        var bq = state.castlingRights.blackQueenside

        if (move.movedPiece.type == PieceType.KING) {
            if (move.movedPiece.color == PieceColor.WHITE) {
                wk = false; wq = false
            } else {
                bk = false; bq = false
            }
        }
        if (move.from == Square(0, 0) || move.to == Square(0, 0)) wq = false
        if (move.from == Square(7, 0) || move.to == Square(7, 0)) wk = false
        if (move.from == Square(0, 7) || move.to == Square(0, 7)) bq = false
        if (move.from == Square(7, 7) || move.to == Square(7, 7)) bk = false

        // Update en passant square
        val newEpTarget = if (move.movedPiece.type == PieceType.PAWN && abs(move.to.rank - move.from.rank) == 2) {
            Square(move.from.file, (move.from.rank + move.to.rank) / 2)
        } else {
            null
        }

        val nextHalfMove = if (move.movedPiece.type == PieceType.PAWN || move.capturedPiece != null) {
            0
        } else {
            state.halfMoveClock + 1
        }
        val nextFullMove = if (state.activeColor == PieceColor.BLACK) {
            state.fullMoveNumber + 1
        } else {
            state.fullMoveNumber
        }

        return state.copy(
            pieces = nextPieces,
            activeColor = state.activeColor.opposite(),
            castlingRights = CastlingRights(wk, wq, bk, bq),
            enPassantTarget = newEpTarget,
            halfMoveClock = nextHalfMove,
            fullMoveNumber = nextFullMove
        )
    }

    fun applyLegalMove(state: ChessBoardState, move: ChessMove): ChessBoardState {
        val allLegal = generateLegalMoves(state)
        val matched = allLegal.firstOrNull {
            it.from == move.from && it.to == move.to && it.promotion == move.promotion
        } ?: throw IllegalArgumentException("Illegal move attempted: ${move.toUci()} in FEN ${state.toFen()}")

        val prevFen = state.toFen()
        val afterRaw = applyMoveRaw(state, matched)
        val coreSig = afterRaw.corePositionSignature()
        val updatedPosHistory = state.positionHistory + coreSig
        val evaluated = evaluateTerminalStatus(afterRaw.copy(positionHistory = updatedPosHistory))
        val sanText = formatSan(state, matched, allLegal, evaluated)
        val recordedMove = matched.copy(
            san = sanText,
            previousFen = prevFen,
            resultingFen = evaluated.toFen()
        )
        return evaluated.copy(moveHistory = state.moveHistory + recordedMove)
    }

    private fun formatSan(
        beforeState: ChessBoardState,
        move: ChessMove,
        allLegalMoves: List<ChessMove>,
        afterState: ChessBoardState
    ): String {
        val base = when {
            move.isKingsideCastle -> "O-O"
            move.isQueensideCastle -> "O-O-O"
            move.movedPiece.type == PieceType.PAWN -> {
                val sb = StringBuilder()
                if (move.capturedPiece != null || move.isEnPassant) {
                    sb.append(('a'.code + move.from.file).toChar())
                    sb.append('x')
                }
                sb.append(move.to.toAlgebraic())
                if (move.promotion != null) {
                    sb.append('=').append(move.promotion.sanChar)
                }
                sb.toString()
            }
            else -> {
                val sb = StringBuilder()
                sb.append(move.movedPiece.type.sanChar)
                val ambiguous = allLegalMoves.filter {
                    it.from != move.from &&
                        it.to == move.to &&
                        it.movedPiece.type == move.movedPiece.type
                }
                if (ambiguous.isNotEmpty()) {
                    val sameFile = ambiguous.any { it.from.file == move.from.file }
                    val sameRank = ambiguous.any { it.from.rank == move.from.rank }
                    when {
                        !sameFile -> sb.append(('a'.code + move.from.file).toChar())
                        !sameRank -> sb.append(('1'.code + move.from.rank).toChar())
                        else -> sb.append(move.from.toAlgebraic())
                    }
                }
                if (move.capturedPiece != null) {
                    sb.append('x')
                }
                sb.append(move.to.toAlgebraic())
                sb.toString()
            }
        }
        return when (afterState.status) {
            GameStatus.CHECKMATE -> "$base#"
            GameStatus.CHECK -> "$base+"
            else -> base
        }
    }

    fun evaluateTerminalStatus(state: ChessBoardState): ChessBoardState {
        val legalMoves = generateLegalMoves(state)
        val inCheck = isInCheck(state, state.activeColor)

        if (legalMoves.isEmpty()) {
            return if (inCheck) {
                state.copy(status = GameStatus.CHECKMATE, winner = state.activeColor.opposite())
            } else {
                state.copy(status = GameStatus.STALEMATE, winner = null)
            }
        }

        // Check draw conditions
        if (state.halfMoveClock >= 100) {
            return state.copy(status = GameStatus.DRAW_FIFTY_MOVE, winner = null)
        }

        val currentSig = state.corePositionSignature()
        val occurrences = state.positionHistory.count { it == currentSig }
        if (occurrences >= 3) {
            return state.copy(status = GameStatus.DRAW_REPETITION, winner = null)
        }

        if (isInsufficientMaterial(state)) {
            return state.copy(status = GameStatus.DRAW_INSUFFICIENT_MATERIAL, winner = null)
        }

        return state.copy(
            status = if (inCheck) GameStatus.CHECK else GameStatus.ONGOING,
            winner = null
        )
    }

    fun isInsufficientMaterial(state: ChessBoardState): Boolean {
        val nonKingPieces = mutableListOf<Pair<Square, Piece>>()
        for (i in 0 until 64) {
            val p = state.pieces[i]
            if (p != null && p.type != PieceType.KING) {
                nonKingPieces.add(Square.fromIndex(i) to p)
            }
        }
        if (nonKingPieces.isEmpty()) return true // K vs K
        if (nonKingPieces.size == 1) {
            val t = nonKingPieces[0].second.type
            if (t == PieceType.KNIGHT || t == PieceType.BISHOP) return true
        }
        if (nonKingPieces.size == 2) {
            val (sq1, p1) = nonKingPieces[0]
            val (sq2, p2) = nonKingPieces[1]
            if (p1.type == PieceType.BISHOP && p2.type == PieceType.BISHOP &&
                p1.color != p2.color && sq1.isLightSquare == sq2.isLightSquare
            ) {
                return true
            }
        }
        return false
    }

    fun parseUciMove(state: ChessBoardState, uci: String): ChessMove? {
        val clean = uci.trim().lowercase()
        if (clean.length < 4) return null
        val from = Square.fromAlgebraic(clean.substring(0, 2)) ?: return null
        val to = Square.fromAlgebraic(clean.substring(2, 4)) ?: return null
        val promo = if (clean.length >= 5) PieceType.fromFenChar(clean[4]) else null
        return generateLegalMoves(state).firstOrNull {
            it.from == from && it.to == to && (it.promotion == promo || (promo == null && it.promotion == PieceType.QUEEN))
        }
    }

    fun exportPgn(
        state: ChessBoardState,
        whiteName: String = "White",
        blackName: String = "Black",
        event: String = "Chess Master Offline Match"
    ): String {
        val resultTag = when (state.status) {
            GameStatus.CHECKMATE, GameStatus.RESIGNED, GameStatus.TIMEOUT -> {
                if (state.winner == PieceColor.WHITE) "1-0" else "0-1"
            }
            GameStatus.STALEMATE,
            GameStatus.DRAW_FIFTY_MOVE,
            GameStatus.DRAW_REPETITION,
            GameStatus.DRAW_INSUFFICIENT_MATERIAL,
            GameStatus.DRAW_AGREEMENT -> "1/2-1/2"
            else -> "*"
        }
        val sb = StringBuilder()
        sb.appendLine("[Event \"$event\"]")
        sb.appendLine("[Site \"Offline Android Studio\"]")
        sb.appendLine("[White \"$whiteName\"]")
        sb.appendLine("[Black \"$blackName\"]")
        sb.appendLine("[Result \"$resultTag\"]")
        sb.appendLine()

        val moves = state.moveHistory
        for (i in moves.indices) {
            if (i % 2 == 0) {
                sb.append("${(i / 2) + 1}. ")
            }
            sb.append(moves[i].san).append(' ')
        }
        sb.append(resultTag)
        return sb.toString().trim()
    }
}
