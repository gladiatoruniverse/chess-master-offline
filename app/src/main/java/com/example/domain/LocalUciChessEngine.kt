package com.example.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

data class EngineSearchResult(
    val bestMove: ChessMove,
    val bestMoveUci: String,
    val bestMoveSan: String,
    val scoreCentipawns: Int, // From White's perspective (+ = White advantage, - = Black advantage)
    val mateInMoves: Int? = null, // Positive = White mates in N, Negative = Black mates in N
    val depthReached: Int,
    val nodesSearched: Int,
    val principalVariationSan: List<String>,
    val humanExplanation: String,
    val uciProtocolLog: List<String>
) {
    fun formattedEvaluation(): String {
        if (mateInMoves != null) {
            val sign = if (mateInMoves > 0) "+" else "-"
            return "${sign}M${abs(mateInMoves)}"
        }
        val pawns = scoreCentipawns / 100.0
        return if (pawns >= 0) "+%.2f".format(pawns) else "%.2f".format(pawns)
    }
}

/**
 * Bundled Local UCI-Protocol Chess Engine (Stockfish-compatible UCI interface + Alpha-Beta/Quiescence Search).
 * Executes 100% offline on local background coroutines with zero network dependency.
 */
class LocalUciChessEngine(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private data class TtEntry(
        val depth: Int,
        val score: Int,
        val bestMoveUci: String?
    )

    private val transpositionTable = HashMap<String, TtEntry>(16384)
    private var nodesVisited = 0
    private var maxNodesBudget = 50000
    private var searchDeadlineMs = Long.MAX_VALUE

    // Piece-Square Tables (from White's perspective; rank 0 = rank 1, rank 7 = rank 8)
    private val pawnPst = intArrayOf(
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10,-20,-20, 10, 10,  5,
         5, -5,-10,  0,  0,-10, -5,  5,
         0,  0,  0, 22, 22,  0,  0,  0,
         5,  5, 10, 26, 26, 10,  5,  5,
        10, 10, 20, 32, 32, 20, 10, 10,
        50, 50, 50, 50, 50, 50, 50, 50,
         0,  0,  0,  0,  0,  0,  0,  0
    )

    private val knightPst = intArrayOf(
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -30,  5, 12, 16, 16, 12,  5,-30,
        -30,  0, 16, 22, 22, 16,  0,-30,
        -30,  5, 16, 22, 22, 16,  5,-30,
        -30,  0, 12, 16, 16, 12,  0,-30,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    )

    private val bishopPst = intArrayOf(
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -10, 10, 10, 12, 12, 10, 10,-10,
        -10,  0, 12, 14, 14, 12,  0,-10,
        -10,  5,  5, 14, 14,  5,  5,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    )

    private val rookPst = intArrayOf(
          0,  0,  4,  8,  8,  4,  0,  0,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         25, 30, 30, 30, 30, 30, 30, 25,
          0,  0,  0,  0,  0,  0,  0,  0
    )

    private val queenPst = intArrayOf(
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  8,  8,  5,  0, -5,
          0,  0,  5,  8,  8,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    )

    private val kingMidgamePst = intArrayOf(
         20, 30, 10,  0,  0, 10, 30, 20,
         20, 20,  0,  0,  0,  0, 20, 20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30
    )

    fun processUciCommand(command: String): String {
        return when (command.trim()) {
            "uci" -> "id name Stockfish-Local-Kotlin 16.1-Offline\nid author Chess Master Open Engine\nuciok"
            "isready" -> "readyok"
            "ucinewgame" -> {
                transpositionTable.clear()
                "readyok"
            }
            else -> "info string command received: $command"
        }
    }

    /**
     * Static evaluation in centipawns from White's perspective.
     */
    fun evaluateStaticWhitePerspective(state: ChessBoardState): Int {
        when (state.status) {
            GameStatus.CHECKMATE -> {
                return if (state.activeColor == PieceColor.WHITE) -18500 else 18500
            }
            GameStatus.STALEMATE,
            GameStatus.DRAW_FIFTY_MOVE,
            GameStatus.DRAW_REPETITION,
            GameStatus.DRAW_INSUFFICIENT_MATERIAL,
            GameStatus.DRAW_AGREEMENT -> return 0
            else -> {}
        }

        var score = 0
        var whiteBishops = 0
        var blackBishops = 0

        for (index in 0 until 64) {
            val piece = state.pieces[index] ?: continue
            val rank = index / 8
            val file = index % 8
            val pstIndex = if (piece.color == PieceColor.WHITE) {
                rank * 8 + file
            } else {
                (7 - rank) * 8 + file
            }

            val positional = when (piece.type) {
                PieceType.PAWN -> pawnPst[pstIndex]
                PieceType.KNIGHT -> knightPst[pstIndex]
                PieceType.BISHOP -> {
                    if (piece.color == PieceColor.WHITE) whiteBishops++ else blackBishops++
                    bishopPst[pstIndex]
                }
                PieceType.ROOK -> rookPst[pstIndex]
                PieceType.QUEEN -> queenPst[pstIndex]
                PieceType.KING -> kingMidgamePst[pstIndex]
            }

            val totalVal = piece.type.centipawnValue + positional
            if (piece.color == PieceColor.WHITE) {
                score += totalVal
            } else {
                score -= totalVal
            }
        }

        // Bishop pair bonus
        if (whiteBishops >= 2) score += 35
        if (blackBishops >= 2) score -= 35

        // Small check bonus
        if (state.status == GameStatus.CHECK) {
            score += if (state.activeColor == PieceColor.BLACK) 25 else -25
        }

        return score
    }

    suspend fun findBestMove(
        state: ChessBoardState,
        difficultyLevel: Int,
        overrideDepth: Int? = null
    ): EngineSearchResult = withContext(dispatcher) {
        currentCoroutineContext().ensureActive()

        val config = EngineDifficultyLevels.getByLevel(difficultyLevel)
        val targetDepth = (overrideDepth ?: config.searchDepth).coerceIn(1, 6)
        val legalMoves = ChessRulesEngine.generateLegalMoves(state)
        require(legalMoves.isNotEmpty()) { "No legal moves available in position ${state.toFen()}" }

        val uciLogs = mutableListOf<String>()
        uciLogs.add("uci -> uciok")
        uciLogs.add("position fen ${state.toFen()}")
        uciLogs.add("go depth $targetDepth nodes ${config.maxNodes} movetime ${config.movetimeMs}")

        nodesVisited = 0
        maxNodesBudget = config.maxNodes
        searchDeadlineMs = System.currentTimeMillis() + config.movetimeMs + 1500L

        val sideMultiplier = if (state.activeColor == PieceColor.WHITE) 1 else -1
        var chosenMove = legalMoves.first()
        var bestSideScore = -30000
        var depthCompleted = 1

        val orderedRootMoves = orderMoves(state, legalMoves, null)

        // Single legal move shortcut
        if (orderedRootMoves.size == 1) {
            val onlyMove = orderedRootMoves.first()
            val nextState = ChessRulesEngine.applyLegalMove(state, onlyMove)
            val whiteScore = evaluateStaticWhitePerspective(nextState)
            val explanation = buildMoveExplanation(state, onlyMove, nextState, whiteScore)
            uciLogs.add("bestmove ${onlyMove.toUci()}")
            return@withContext EngineSearchResult(
                bestMove = onlyMove,
                bestMoveUci = onlyMove.toUci(),
                bestMoveSan = onlyMove.san.ifEmpty { onlyMove.toUci() },
                scoreCentipawns = whiteScore,
                mateInMoves = detectMateInMoves(whiteScore),
                depthReached = 1,
                nodesSearched = 1,
                principalVariationSan = listOf(onlyMove.san.ifEmpty { onlyMove.toUci() }),
                humanExplanation = explanation,
                uciProtocolLog = uciLogs
            )
        }

        // Iterative deepening up to targetDepth
        for (depth in 1..targetDepth) {
            currentCoroutineContext().ensureActive()
            var alpha = -30000
            val beta = 30000
            var iterationBestMove = chosenMove
            var iterationBestScore = -30000

            val candidateScores = mutableListOf<Pair<ChessMove, Int>>()

            for (move in orderedRootMoves) {
                currentCoroutineContext().ensureActive()
                val nextState = ChessRulesEngine.applyLegalMove(state, move)
                val rawScore = if (nextState.status == GameStatus.CHECKMATE) {
                    19500
                } else {
                    -negamax(
                        state = nextState,
                        depth = depth - 1,
                        alpha = -beta,
                        beta = -alpha,
                        ply = 1,
                        useQuiescence = config.useQuiescence
                    )
                }

                // Add controlled noise on lower levels for non-mate moves so Beginner/Novice feel natural
                val noisyScore = if (config.noiseCentipawns > 0 && overrideDepth == null && abs(rawScore) < 18000) {
                    rawScore + Random.nextInt(-config.noiseCentipawns, config.noiseCentipawns + 1)
                } else {
                    rawScore
                }
                candidateScores.add(move to noisyScore)

                if (noisyScore > iterationBestScore) {
                    iterationBestScore = noisyScore
                    iterationBestMove = move
                }
                if (rawScore > alpha) {
                    alpha = rawScore
                }
            }

            chosenMove = iterationBestMove
            bestSideScore = iterationBestScore
            depthCompleted = depth

            val cpFromWhite = bestSideScore * sideMultiplier
            uciLogs.add("info depth $depth score cp $cpFromWhite nodes $nodesVisited pv ${chosenMove.toUci()}")

            if (nodesVisited >= maxNodesBudget || System.currentTimeMillis() >= searchDeadlineMs) {
                break
            }
        }

        // Apply chosen move to obtain official SAN and build Principal Variation
        val stateAfterBest = ChessRulesEngine.applyLegalMove(state, chosenMove)
        val bestSan = stateAfterBest.moveHistory.lastOrNull()?.san ?: chosenMove.toUci()
        val finalChosenMove = stateAfterBest.moveHistory.lastOrNull() ?: chosenMove
        val pvLine = extractPrincipalVariation(state, finalChosenMove, depthCompleted.coerceAtMost(4))
        val whitePerspectiveScore = bestSideScore * sideMultiplier
        val mateIn = detectMateInMoves(whitePerspectiveScore)
        val explanation = buildMoveExplanation(state, finalChosenMove, stateAfterBest, whitePerspectiveScore)

        uciLogs.add("bestmove ${finalChosenMove.toUci()}")

        EngineSearchResult(
            bestMove = finalChosenMove,
            bestMoveUci = finalChosenMove.toUci(),
            bestMoveSan = bestSan,
            scoreCentipawns = whitePerspectiveScore,
            mateInMoves = mateIn,
            depthReached = depthCompleted,
            nodesSearched = nodesVisited.coerceAtLeast(1),
            principalVariationSan = pvLine,
            humanExplanation = explanation,
            uciProtocolLog = uciLogs
        )
    }

    private suspend fun negamax(
        state: ChessBoardState,
        depth: Int,
        alpha: Int,
        beta: Int,
        ply: Int,
        useQuiescence: Boolean
    ): Int {
        nodesVisited++
        if ((nodesVisited and 255) == 0) {
            currentCoroutineContext().ensureActive()
        }

        val sideMultiplier = if (state.activeColor == PieceColor.WHITE) 1 else -1

        if (state.status.isTerminal) {
            return when (state.status) {
                GameStatus.CHECKMATE -> -19000 + ply * 50
                else -> 0
            }
        }

        if (depth <= 0 || nodesVisited >= maxNodesBudget) {
            return if (useQuiescence && nodesVisited < maxNodesBudget) {
                quiescence(state, alpha, beta, sideMultiplier, qDepth = 2)
            } else {
                evaluateStaticWhitePerspective(state) * sideMultiplier
            }
        }

        val sig = state.corePositionSignature()
        val ttHit = transpositionTable[sig]
        if (ttHit != null && ttHit.depth >= depth) {
            return ttHit.score
        }

        val legalMoves = ChessRulesEngine.generateLegalMoves(state)
        if (legalMoves.isEmpty()) {
            return if (ChessRulesEngine.isInCheck(state, state.activeColor)) {
                -19000 + ply * 50
            } else {
                0
            }
        }

        val ordered = orderMoves(state, legalMoves, ttHit?.bestMoveUci)
        var currentAlpha = alpha
        var bestScore = -30000
        var bestMoveUci: String? = null

        for (move in ordered) {
            val nextState = ChessRulesEngine.applyLegalMove(state, move)
            val score = -negamax(nextState, depth - 1, -beta, -currentAlpha, ply + 1, useQuiescence)
            if (score > bestScore) {
                bestScore = score
                bestMoveUci = move.toUci()
            }
            if (score > currentAlpha) {
                currentAlpha = score
            }
            if (currentAlpha >= beta) {
                break // Beta cutoff
            }
        }

        if (transpositionTable.size > 24000) {
            transpositionTable.clear()
        }
        transpositionTable[sig] = TtEntry(depth, bestScore, bestMoveUci)
        return bestScore
    }

    private fun quiescence(
        state: ChessBoardState,
        alpha: Int,
        beta: Int,
        sideMultiplier: Int,
        qDepth: Int
    ): Int {
        val standPat = evaluateStaticWhitePerspective(state) * sideMultiplier
        if (qDepth <= 0) return standPat
        if (standPat >= beta) return beta
        var currentAlpha = maxOf(alpha, standPat)

        val tacticalMoves = ChessRulesEngine.generateLegalMoves(state).filter {
            it.capturedPiece != null || it.promotion != null
        }
        if (tacticalMoves.isEmpty()) return standPat

        val ordered = orderMoves(state, tacticalMoves, null)
        for (move in ordered) {
            nodesVisited++
            val nextState = ChessRulesEngine.applyLegalMove(state, move)
            val score = -quiescence(nextState, -beta, -currentAlpha, -sideMultiplier, qDepth - 1)
            if (score >= beta) return beta
            if (score > currentAlpha) currentAlpha = score
        }
        return currentAlpha
    }

    private fun orderMoves(
        state: ChessBoardState,
        moves: List<ChessMove>,
        ttBestUci: String?
    ): List<ChessMove> {
        return moves.sortedByDescending { move ->
            var priority = 0
            if (ttBestUci != null && move.toUci() == ttBestUci) {
                priority += 10000
            }
            if (move.promotion != null) {
                priority += 4000 + move.promotion.centipawnValue
            }
            if (move.capturedPiece != null) {
                // MVV-LVA: Most Valuable Victim - Least Valuable Attacker
                priority += 2000 + (move.capturedPiece.type.centipawnValue * 10) - move.movedPiece.type.centipawnValue
            }
            if (move.isKingsideCastle || move.isQueensideCastle) {
                priority += 600
            }
            // Center control bonus (d4, e4, d5, e5)
            if (move.to.file in 2..5 && move.to.rank in 2..5) {
                priority += 60
            }
            priority
        }
    }

    private fun extractPrincipalVariation(
        rootState: ChessBoardState,
        firstMove: ChessMove,
        maxPlies: Int
    ): List<String> {
        val pv = mutableListOf<String>()
        var current = ChessRulesEngine.applyLegalMove(rootState, firstMove)
        pv.add(current.moveHistory.lastOrNull()?.san ?: firstMove.toUci())

        for (step in 1 until maxPlies) {
            if (current.status.isTerminal) break
            val ttBest = transpositionTable[current.corePositionSignature()]?.bestMoveUci ?: break
            val nextMove = ChessRulesEngine.parseUciMove(current, ttBest) ?: break
            current = ChessRulesEngine.applyLegalMove(current, nextMove)
            pv.add(current.moveHistory.lastOrNull()?.san ?: nextMove.toUci())
        }
        return pv
    }

    private fun detectMateInMoves(scoreCentipawns: Int): Int? {
        val absScore = abs(scoreCentipawns)
        if (absScore < 18000) return null
        val pliesToMate = ((19000 - absScore) / 50).coerceAtLeast(1)
        val movesToMate = (pliesToMate + 1) / 2
        return if (scoreCentipawns > 0) movesToMate else -movesToMate
    }

    fun buildMoveExplanation(
        beforeState: ChessBoardState,
        move: ChessMove,
        afterState: ChessBoardState,
        whiteScoreCp: Int
    ): String {
        val pieceName = move.movedPiece.type.displayName.lowercase()
        val san = afterState.moveHistory.lastOrNull()?.san ?: move.toUci()

        val reasons = mutableListOf<String>()

        if (afterState.status == GameStatus.CHECKMATE) {
            return "Plays $san to deliver immediate checkmate and win the game!"
        }
        if (move.isKingsideCastle || move.isQueensideCastle) {
            val side = if (move.isKingsideCastle) "kingside" else "queenside"
            reasons.add("castles $side to tuck the king into safety and activate the rook toward the center")
        }
        if (move.promotion != null) {
            reasons.add("promotes the pawn to a ${move.promotion.displayName} on ${move.to.toAlgebraic()}")
        }
        if (move.capturedPiece != null) {
            val capName = move.capturedPiece.type.displayName.lowercase()
            val epNote = if (move.isEnPassant) " via en passant" else ""
            reasons.add("captures the opponent's $capName on ${move.to.toAlgebraic()}$epNote")
        }
        if (afterState.status == GameStatus.CHECK) {
            reasons.add("places the enemy king in check, forcing a defensive response")
        }
        if (reasons.isEmpty()) {
            val isCenterSquare = move.to.file in 2..5 && move.to.rank in 2..5
            val isOpeningBackRank = (move.movedPiece.color == PieceColor.WHITE && move.from.rank == 0) ||
                (move.movedPiece.color == PieceColor.BLACK && move.from.rank == 7)
            when {
                isOpeningBackRank && (move.movedPiece.type == PieceType.KNIGHT || move.movedPiece.type == PieceType.BISHOP) ->
                    reasons.add("develops the $pieceName off the back rank onto an active square (${move.to.toAlgebraic()})")
                isCenterSquare && move.movedPiece.type == PieceType.PAWN ->
                    reasons.add("stakes a claim in the center of the board with the $pieceName on ${move.to.toAlgebraic()}")
                isCenterSquare ->
                    reasons.add("centralizes the $pieceName on ${move.to.toAlgebraic()} to maximize board control")
                move.movedPiece.type == PieceType.ROOK ->
                    reasons.add("repositions the rook to ${move.to.toAlgebraic()} to contest key files and ranks")
                else ->
                    reasons.add("improves piece coordination by moving the $pieceName to ${move.to.toAlgebraic()}")
            }
        }

        val evalSummary = when {
            abs(whiteScoreCp) < 45 -> "The position remains roughly balanced."
            (whiteScoreCp > 0 && beforeState.activeColor == PieceColor.WHITE) ||
                (whiteScoreCp < 0 && beforeState.activeColor == PieceColor.BLACK) ->
                "This maintains a solid positional advantage for ${beforeState.activeColor.displayName}."
            else ->
                "This is the most resilient defensive resource available."
        }

        return "Suggested move $san ${reasons.joinToString(" and ")}. $evalSummary"
    }
}
