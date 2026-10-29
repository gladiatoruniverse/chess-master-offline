package com.example

import com.example.data.OfflineLessonsRepository
import com.example.data.OfflinePuzzlesRepository
import com.example.domain.ChessBoardState
import com.example.domain.ChessRulesEngine
import com.example.domain.GameStatus
import com.example.domain.LocalUciChessEngine
import com.example.domain.PieceColor
import com.example.domain.PieceType
import com.example.domain.Square
import com.example.ui.screens.formatClockMillis
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun startingBoard_and_turnHandling() {
        val state = ChessRulesEngine.createInitialState()
        assertEquals(PieceColor.WHITE, state.activeColor)
        assertEquals(ChessBoardState.STARTING_FEN, state.toFen())
        assertEquals(GameStatus.ONGOING, state.status)

        val whiteLegalMoves = ChessRulesEngine.generateLegalMoves(state)
        assertEquals(20, whiteLegalMoves.size)

        val e2e4 = ChessRulesEngine.parseUciMove(state, "e2e4")
        assertNotNull(e2e4)
        val afterE4 = ChessRulesEngine.applyLegalMove(state, e2e4!!)
        assertEquals(PieceColor.BLACK, afterE4.activeColor)
        assertEquals(Square.fromAlgebraic("e3"), afterE4.enPassantTarget)
        assertEquals("e4", afterE4.moveHistory.last().san)
    }

    @Test
    fun castling_kingside_and_queenside() {
        val fen = "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1"
        val state = ChessRulesEngine.fromFen(fen)

        val wkCastle = ChessRulesEngine.parseUciMove(state, "e1g1")
        val wqCastle = ChessRulesEngine.parseUciMove(state, "e1c1")
        assertNotNull(wkCastle)
        assertNotNull(wqCastle)

        val afterKingside = ChessRulesEngine.applyLegalMove(state, wkCastle!!)
        assertEquals("O-O", afterKingside.moveHistory.last().san)
        assertEquals(PieceType.KING, afterKingside.pieceAt(Square.fromAlgebraic("g1")!!)?.type)
        assertEquals(PieceType.ROOK, afterKingside.pieceAt(Square.fromAlgebraic("f1")!!)?.type)
        assertFalse(afterKingside.castlingRights.whiteKingside)
        assertFalse(afterKingside.castlingRights.whiteQueenside)
    }

    @Test
    fun enPassant_capture_isLegal_andRemovesCapturedPawn() {
        val fen = "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3"
        val state = ChessRulesEngine.fromFen(fen)
        val epMove = ChessRulesEngine.parseUciMove(state, "e5d6")
        assertNotNull(epMove)
        assertTrue(epMove!!.isEnPassant)

        val afterEp = ChessRulesEngine.applyLegalMove(state, epMove)
        assertNull(afterEp.pieceAt(Square.fromAlgebraic("d5")!!))
        assertEquals(PieceType.PAWN, afterEp.pieceAt(Square.fromAlgebraic("d6")!!)?.type)
        assertEquals("exd6", afterEp.moveHistory.last().san)
    }

    @Test
    fun allFourPromotionChoices_areGenerated_andApplied() {
        val fen = "8/4P1k1/8/8/8/8/6PP/6K1 w - - 0 1"
        val state = ChessRulesEngine.fromFen(fen)
        val movesFromE7 = ChessRulesEngine.generateLegalMovesForSquare(state, Square.fromAlgebraic("e7")!!)
        val promotedTypes = movesFromE7.mapNotNull { it.promotion }.toSet()
        assertEquals(
            setOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT),
            promotedTypes
        )

        val underPromoteKnight = ChessRulesEngine.parseUciMove(state, "e7e8n")
        assertNotNull(underPromoteKnight)
        val afterKnightPromo = ChessRulesEngine.applyLegalMove(state, underPromoteKnight!!)
        assertEquals(PieceType.KNIGHT, afterKnightPromo.pieceAt(Square.fromAlgebraic("e8")!!)?.type)
        assertTrue(afterKnightPromo.moveHistory.last().san.startsWith("e8=N"))
    }

    @Test
    fun checkmate_stalemate_and_insufficientMaterial_detection() {
        // Fool's Mate
        var state = ChessRulesEngine.createInitialState()
        for (uci in listOf("f2f3", "e7e5", "g2g4", "d8h4")) {
            val move = ChessRulesEngine.parseUciMove(state, uci)!!
            state = ChessRulesEngine.applyLegalMove(state, move)
        }
        assertEquals(GameStatus.CHECKMATE, state.status)
        assertEquals(PieceColor.BLACK, state.winner)
        assertEquals("Qh4#", state.moveHistory.last().san)

        // Stalemate position
        val stalemateState = ChessRulesEngine.fromFen("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1")
        assertEquals(GameStatus.STALEMATE, stalemateState.status)
        assertNull(stalemateState.winner)

        // Insufficient material (King + Bishop vs King)
        val insuffState = ChessRulesEngine.fromFen("8/8/4k3/8/8/3B4/4K3/8 w - - 0 1")
        assertEquals(GameStatus.DRAW_INSUFFICIENT_MATERIAL, insuffState.status)
    }

    @Test
    fun localUciEngine_uciProtocol_and_legalMoveSelection() = runTest {
        val engine = LocalUciChessEngine()
        assertTrue(engine.processUciCommand("uci").contains("uciok"))
        assertEquals("readyok", engine.processUciCommand("isready"))

        // Mate-in-1 position: engine must find Re8#
        val mateInOneState = ChessRulesEngine.fromFen("6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1")
        val searchResult = engine.findBestMove(mateInOneState, difficultyLevel = 6)
        assertEquals("e1e8", searchResult.bestMoveUci)
        assertEquals("Re8#", searchResult.bestMoveSan)
        assertTrue(searchResult.humanExplanation.isNotBlank())
    }

    @Test
    fun allBundledOfflinePuzzles_and_lessons_areStrictlyLegal() {
        assertTrue(OfflinePuzzlesRepository.ALL_PUZZLES.size >= 30)
        assertTrue(OfflinePuzzlesRepository.verifyAllPuzzlesLegal())

        for (lesson in OfflineLessonsRepository.ALL_LESSONS) {
            val board = ChessRulesEngine.fromFen(lesson.demoFen)
            val targetMove = ChessRulesEngine.parseUciMove(board, lesson.targetMoveUci)
            assertNotNull("Lesson ${lesson.id} target move ${lesson.targetMoveUci} must be legal", targetMove)
        }

        for (opening in OfflineLessonsRepository.OPENING_EXPLORER_LINES) {
            var board = ChessRulesEngine.createInitialState()
            for (step in opening.steps) {
                val move = ChessRulesEngine.parseUciMove(board, step.moveUci)
                assertNotNull("Opening ${opening.id} move ${step.moveUci} must be legal", move)
                board = ChessRulesEngine.applyLegalMove(board, move!!)
            }
        }
    }

    @Test
    fun clockFormatting_preventsNegativeTimeDisplay() {
        assertEquals("10:00", formatClockMillis(600_000L))
        assertEquals("0:05.4", formatClockMillis(5_400L))
        assertEquals("0:00.0", formatClockMillis(-500L))
    }
}
