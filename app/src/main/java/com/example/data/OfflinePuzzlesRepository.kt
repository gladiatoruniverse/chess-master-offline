package com.example.data

import com.example.domain.ChessRulesEngine
import java.util.Calendar

enum class PuzzleCategory(val displayName: String) {
    ALL("All"),
    MATE_IN_ONE("Mate in 1"),
    MATE_IN_TWO("Mate in 2"),
    FORK("Fork"),
    PIN("Pin"),
    TACTICAL_CAPTURE("Tactical Capture"),
    DISCOVERED_ATTACK("Discovered Attack")
}

data class ChessPuzzle(
    val id: Int,
    val title: String,
    val category: PuzzleCategory,
    val difficulty: String,
    val initialFen: String,
    val solutionUciMoves: List<String>,
    val hintText: String,
    val explanation: String
)

object OfflinePuzzlesRepository {

    val ALL_PUZZLES: List<ChessPuzzle> = listOf(
        // --- MATE IN ONE (9 Puzzles) ---
        ChessPuzzle(
            id = 1,
            title = "Scholar's Strike",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Beginner",
            initialFen = "r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 4 4",
            solutionUciMoves = listOf("h5f7"),
            hintText = "Look at the f7 square, which is only guarded by the Black king.",
            explanation = "Qxf7# delivers immediate checkmate because the bishop on c4 protects the queen."
        ),
        ChessPuzzle(
            id = 2,
            title = "Back-Rank Corridor",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Beginner",
            initialFen = "6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1",
            solutionUciMoves = listOf("e1e8"),
            hintText = "Black's own pawns on f7, g7, and h7 trap the king on the 8th rank.",
            explanation = "Re8# is a classic back-rank checkmate—Black has no pieces to block and no flight squares."
        ),
        ChessPuzzle(
            id = 3,
            title = "Central Queen Snare",
            category = PuzzleCategory.TACTICAL_CAPTURE,
            difficulty = "Beginner",
            initialFen = "r4rk1/ppp2ppp/2n5/3q4/8/2PB1Q2/PP3PPP/R4RK1 w - - 0 12",
            solutionUciMoves = listOf("f3d5"),
            hintText = "Look for the undefended Black queen in the center of the board.",
            explanation = "Qxd5 wins Black's unguarded queen cleanly in the center."
        ),
        ChessPuzzle(
            id = 4,
            title = "Long Diagonal Battery Mate",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Beginner",
            initialFen = "r4rk1/ppp2p1p/2n3p1/8/8/5Q2/PBPP1PPP/R4RK1 w - - 0 1",
            solutionUciMoves = listOf("f3f6"),
            hintText = "Your bishop on b2 and queen on f3 eye the weakened dark squares around Black's king.",
            explanation = "Qf6 threatens unstoppable checkmate on g7 supported by the b2 bishop."
        ),
        ChessPuzzle(
            id = 5,
            title = "Smothered Corner Mate",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Intermediate",
            initialFen = "6rk/6pp/8/4N3/8/8/6PP/6K1 w - - 0 1",
            solutionUciMoves = listOf("e5f7"),
            hintText = "Look for the knight jump that attacks h8 while Black's own pieces block every exit.",
            explanation = "Nf7# delivers Philidor's smothered checkmate because the Black king is boxed in by its own rook and pawns."
        ),
        ChessPuzzle(
            id = 6,
            title = "Arabian Knight & Rook Mate",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Beginner",
            initialFen = "7k/R7/5N2/8/8/8/6PP/6K1 w - - 0 1",
            solutionUciMoves = listOf("a7h7"),
            hintText = "Your knight on f6 both guards h7 and takes away the g8 escape square.",
            explanation = "Rh7# is the classic Arabian Mate—the knight and rook cooperate to trap the king in the corner."
        ),
        ChessPuzzle(
            id = 7,
            title = "Fool's Diagonal Strike",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Beginner",
            initialFen = "rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2",
            solutionUciMoves = listOf("d8h4"),
            hintText = "White has weakened the e1-h4 diagonal around the king. Activate your queen.",
            explanation = "Qh4# exploits the weakened f2 and g3 squares to deliver Fool's Mate."
        ),
        ChessPuzzle(
            id = 8,
            title = "Epaulette Royal Finale",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Intermediate",
            initialFen = "3rkr2/8/2Q5/8/8/8/6PP/6K1 w - - 0 1",
            solutionUciMoves = listOf("c6e6"),
            hintText = "Look at how Black's rooks on d8 and f8 block the king's side escape squares.",
            explanation = "Qe6# is Epaulette Mate: Black's own rooks on d8 and f8 act like shoulder ornaments blocking escape."
        ),
        ChessPuzzle(
            id = 9,
            title = "Support Mate on h7",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Beginner",
            initialFen = "r1bq1rk1/ppp2ppp/2n5/3np1N1/2B5/7Q/PPPP1PPP/RNB1K2R w KQ - 0 8",
            solutionUciMoves = listOf("h3h7"),
            hintText = "Your knight on g5 and queen on h3 both converge on the h7 square.",
            explanation = "Qxh7# finishes the game immediately with the knight on g5 protecting the queen."
        ),
        ChessPuzzle(
            id = 10,
            title = "Doubled Rooks Overload",
            category = PuzzleCategory.MATE_IN_TWO,
            difficulty = "Intermediate",
            initialFen = "3r2k1/5ppp/8/8/8/8/4RPPP/4R1K1 w - - 0 1",
            solutionUciMoves = listOf("e2e8", "d8e8", "e1e8"),
            hintText = "Your doubled rooks on the e-file overpower Black's lone back-rank defender.",
            explanation = "1. Re8+ Rxe8 2. Rxe8# uses a battery of doubled rooks to overload Black's back rank."
        ),
        ChessPuzzle(
            id = 11,
            title = "Queen & Rook Corridor",
            category = PuzzleCategory.MATE_IN_TWO,
            difficulty = "Intermediate",
            initialFen = "5rk1/5ppp/8/8/8/8/4QPPP/4R1K1 w - - 0 1",
            solutionUciMoves = listOf("e2e8", "f8e8", "e1e8"),
            hintText = "Invade the 8th rank with your queen, backed up by the rook on e1.",
            explanation = "1. Qe8 forces 1...Rxe8, and 2. Rxe8# delivers back-rank checkmate."
        ),
        ChessPuzzle(
            id = 12,
            title = "Two-Rook Lawnmower",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Beginner",
            initialFen = "6k1/R7/8/8/8/8/8/1R4K1 w - - 0 1",
            solutionUciMoves = listOf("b1b8"),
            hintText = "Use your two rooks on the 7th and 8th ranks to trap the Black king.",
            explanation = "Rb8# completes the two-rook lawnmower mate on the 8th rank."
        ),
        ChessPuzzle(
            id = 13,
            title = "Open D-File Invasion",
            category = PuzzleCategory.MATE_IN_ONE,
            difficulty = "Beginner",
            initialFen = "6k1/5ppp/8/8/8/8/5PPP/3R2K1 w - - 0 1",
            solutionUciMoves = listOf("d1d8"),
            hintText = "Send your rook down the open d-file to the 8th rank.",
            explanation = "Rd8# traps the Black king behind its own kingside pawn shield."
        ),
        ChessPuzzle(
            id = 14,
            title = "Deflection Back-Rank Strike",
            category = PuzzleCategory.MATE_IN_TWO,
            difficulty = "Advanced",
            initialFen = "4r1k1/5ppp/8/8/8/8/4RPPP/3QR1K1 w - - 0 1",
            solutionUciMoves = listOf("e2e8", "e8e8").let { listOf("e2e8") },
            hintText = "Capture the lone defender on e8 with check!",
            explanation = "Rxe8# captures Black's defending rook and delivers back-rank checkmate."
        ),
        ChessPuzzle(
            id = 15,
            title = "Morphy's Opera House Finale",
            category = PuzzleCategory.MATE_IN_TWO,
            difficulty = "Advanced",
            initialFen = "4kb1r/p2n1ppp/4q3/4p1B1/4P3/1Q6/PPP2PPP/2KR4 w k - 1 16",
            solutionUciMoves = listOf("b3b8", "d7b8", "d1d8"),
            hintText = "Morphy's legendary sacrifice! Deflect the knight on d7 away from guarding d8.",
            explanation = "1. Qb8+! Nxb8 2. Rd8# is Paul Morphy's famous 1858 Opera Game checkmate!"
        ),

        // --- FORK (6 Puzzles) ---
        ChessPuzzle(
            id = 16,
            title = "Royal Family Fork on c7",
            category = PuzzleCategory.FORK,
            difficulty = "Beginner",
            initialFen = "r1bqkb1r/pppp1ppp/2n5/1N2p3/4P3/8/PPPP1PPP/R1BQKBNR w KQkq - 1 4",
            solutionUciMoves = listOf("b5c7"),
            hintText = "Your knight on b5 can jump to a square that checks the king and attacks the a8 rook.",
            explanation = "Nxc7+ forks the Black king on e8 and the rook on a8, winning the exchange."
        ),
        ChessPuzzle(
            id = 17,
            title = "Knight Fork on f7",
            category = PuzzleCategory.FORK,
            difficulty = "Beginner",
            initialFen = "r1bqk2r/pppp1ppp/2n2n2/4N3/4P3/8/PPPP1PPP/RNBQKB1R w KQkq - 0 5",
            solutionUciMoves = listOf("e5f7"),
            hintText = "Look at how the knight on e5 can simultaneously attack the Black queen and rook.",
            explanation = "Nxf7 forks the Black queen on d8 and the rook on h8."
        ),
        ChessPuzzle(
            id = 18,
            title = "Central Knight Capture & Fork Threat",
            category = PuzzleCategory.FORK,
            difficulty = "Beginner",
            initialFen = "r1bqkbnr/pppp1ppp/8/2n1p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 2 4",
            solutionUciMoves = listOf("f3e5"),
            hintText = "The e5 pawn is undefended—capture it with your knight to threaten f7.",
            explanation = "Nxe5 wins a central pawn and creates an immediate fork threat against f7."
        ),
        ChessPuzzle(
            id = 19,
            title = "Bounding Bishop Check",
            category = PuzzleCategory.FORK,
            difficulty = "Intermediate",
            initialFen = "r1bqkbnr/ppp2ppp/8/3pp3/n3P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 0 4",
            solutionUciMoves = listOf("f1b5"),
            hintText = "Develop your bishop with check to attack the king and pressure Black's queenside.",
            explanation = "Bb5+ delivers check and forces Black to make a concession in the center."
        ),
        ChessPuzzle(
            id = 20,
            title = "Central Outpost Elimination",
            category = PuzzleCategory.FORK,
            difficulty = "Intermediate",
            initialFen = "r2q1rk1/ppp2ppp/8/3p4/4n3/3B1N2/PPP2PPP/R2Q1RK1 w - - 0 10",
            solutionUciMoves = listOf("d3e4"),
            hintText = "Capture the centralized Black knight on e4 with your bishop.",
            explanation = "Bxe4 wins a full knight in the center of the board."
        ),
        ChessPuzzle(
            id = 21,
            title = "Endgame King & Rook Fork",
            category = PuzzleCategory.FORK,
            difficulty = "Intermediate",
            initialFen = "2r3k1/5ppp/8/3N4/8/8/5PPP/6K1 w - - 0 1",
            solutionUciMoves = listOf("d5e7"),
            hintText = "Your knight on d5 can jump to a square that checks the king on g8 and attacks the c8 rook.",
            explanation = "Ne7+ forks the Black king on g8 and the rook on c8, winning a full rook!"
        ),

        // --- PIN (4 Puzzles) ---
        ChessPuzzle(
            id = 22,
            title = "Open E-File Strike",
            category = PuzzleCategory.PIN,
            difficulty = "Beginner",
            initialFen = "4k3/8/8/4n3/8/8/5PPP/4R1K1 w - - 0 1",
            solutionUciMoves = listOf("e1e5"),
            hintText = "The Black knight on e5 is aligned with the Black king on e8.",
            explanation = "Rxe5+ captures the knight with check along the open e-file."
        ),
        ChessPuzzle(
            id = 23,
            title = "Diagonal Queen Capture",
            category = PuzzleCategory.PIN,
            difficulty = "Intermediate",
            initialFen = "r2qk2r/ppp2ppp/2np4/4p1B1/2B1P1b1/2NP1N2/PPP2PPP/R2Q1RK1 w kq - 0 7",
            solutionUciMoves = listOf("g5d8"),
            hintText = "Look directly down the g5-d8 diagonal at Black's unguarded queen.",
            explanation = "Bxd8 captures the Black queen directly."
        ),
        ChessPuzzle(
            id = 24,
            title = "Central Pawn Sweep",
            category = PuzzleCategory.PIN,
            difficulty = "Intermediate",
            initialFen = "r1bq1rk1/pppp1ppp/2n2n2/4p1Q1/2B1P3/8/PPPP1PPP/RNB1K1NR w KQ - 4 5",
            solutionUciMoves = listOf("g5e5"),
            hintText = "Black's e5 pawn is undefended—capture it with your queen.",
            explanation = "Qxe5 wins a key central pawn while maintaining active pressure."
        ),
        ChessPuzzle(
            id = 25,
            title = "Rook Pin on the 8th Rank",
            category = PuzzleCategory.PIN,
            difficulty = "Advanced",
            initialFen = "3q2k1/5ppp/8/8/8/1B6/5PPP/3R2K1 w - - 0 1",
            solutionUciMoves = listOf("d1d8"),
            hintText = "Black's queen on d8 is undefended on the open d-file.",
            explanation = "Rxd8+ wins the Black queen and pins the back rank."
        ),

        // --- TACTICAL CAPTURE (5 Puzzles) ---
        ChessPuzzle(
            id = 26,
            title = "Free Central Pawn",
            category = PuzzleCategory.TACTICAL_CAPTURE,
            difficulty = "Beginner",
            initialFen = "r1bqk1nr/pppp1ppp/8/2b1p3/4P3/3P1N2/PPP1QPPP/RNB1KB1R w KQkq - 1 4",
            solutionUciMoves = listOf("f3e5"),
            hintText = "Black's e5 pawn has no defender—capture it with your knight.",
            explanation = "Nxe5 cleanly wins a central pawn for free."
        ),
        ChessPuzzle(
            id = 27,
            title = "Back-Rank Rook Capture",
            category = PuzzleCategory.TACTICAL_CAPTURE,
            difficulty = "Intermediate",
            initialFen = "2r3k1/5ppp/8/8/8/8/5PPP/2R3K1 w - - 0 1",
            solutionUciMoves = listOf("c1c8"),
            hintText = "Control the open c-file and deliver back-rank mate!",
            explanation = "Rxc8# captures the Black rook and delivers immediate back-rank checkmate."
        ),
        ChessPuzzle(
            id = 28,
            title = "Winning the Exchange on f8",
            category = PuzzleCategory.TACTICAL_CAPTURE,
            difficulty = "Intermediate",
            initialFen = "r1bq1rk1/ppp2p1p/2n1p1pB/3n4/3P4/2PB1N2/PP3PPP/R2Q1RK1 w - - 0 10",
            solutionUciMoves = listOf("h6f8"),
            hintText = "Your bishop on h6 is attacking the Black rook on f8.",
            explanation = "Bxf8 wins the exchange by capturing the rook on f8."
        ),
        ChessPuzzle(
            id = 29,
            title = "En Passant Tactical Strike",
            category = PuzzleCategory.TACTICAL_CAPTURE,
            difficulty = "Intermediate",
            initialFen = "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3",
            solutionUciMoves = listOf("e5d6"),
            hintText = "Black just pushed d7-d5 two squares past your e5 pawn. Use the special pawn capture!",
            explanation = "exd6 e.p. captures Black's d5 pawn en passant just as it tries to bypass your advanced e5 pawn."
        ),
        ChessPuzzle(
            id = 30,
            title = "Promotion Capture Checkmate",
            category = PuzzleCategory.TACTICAL_CAPTURE,
            difficulty = "Advanced",
            initialFen = "4r1k1/5Ppp/8/8/8/8/6PP/6K1 w - - 0 1",
            solutionUciMoves = listOf("f7e8q"),
            hintText = "Your pawn on f7 can capture the rook on e8 and promote to a new queen!",
            explanation = "fxe8=Q# captures the rook, promotes to a Queen, and delivers back-rank checkmate!"
        ),

        // --- DISCOVERED ATTACK (4 Puzzles) ---
        ChessPuzzle(
            id = 31,
            title = "Discovered Check on the E-File",
            category = PuzzleCategory.DISCOVERED_ATTACK,
            difficulty = "Intermediate",
            initialFen = "4k3/4q3/8/8/4N3/8/5PPP/4R1K1 w - - 0 1",
            solutionUciMoves = listOf("e4f6"),
            hintText = "Move your knight on e4 with check to uncover your rook on e1 against the Black queen on e7!",
            explanation = "Nf6+ delivers a double check (from both the knight and the rook on e1) and wins the queen on e7!"
        ),
        ChessPuzzle(
            id = 32,
            title = "Seventh-Rank Rook Sweep",
            category = PuzzleCategory.DISCOVERED_ATTACK,
            difficulty = "Intermediate",
            initialFen = "6k1/5Rpp/8/8/8/8/1r4PP/6K1 w - - 0 1",
            solutionUciMoves = listOf("f7b7"),
            hintText = "Slide your rook along the 7th rank to attack the undefended Black rook on b2.",
            explanation = "Rb7 attacks and wins the loose rook on b2 while cutting off the Black king."
        ),
        ChessPuzzle(
            id = 33,
            title = "Bishop Uncovers the Queen",
            category = PuzzleCategory.DISCOVERED_ATTACK,
            difficulty = "Advanced",
            initialFen = "3q2k1/5ppp/8/3B4/8/8/5PPP/3Q2K1 w - - 0 1",
            solutionUciMoves = listOf("d5f7"),
            hintText = "Sacrifice your bishop on f7 with check to uncover your queen on d1 against Black's undefended queen on d8!",
            explanation = "Bxf7+! forces the Black king to respond to check while uncovering a deadly attack on the d8 queen."
        ),
        ChessPuzzle(
            id = 34,
            title = "Central Discovered Battery",
            category = PuzzleCategory.DISCOVERED_ATTACK,
            difficulty = "Advanced",
            initialFen = "r3k2r/ppp2ppp/8/3N4/8/8/PPP2PPP/R3R1K1 w kq - 0 1",
            solutionUciMoves = listOf("d5c7"),
            hintText = "Jump with your knight from d5 to c7 to deliver check and fork the rook on a8!",
            explanation = "Nxc7+ checks the king, uncovers the e-file pressure, and forks the rook on a8."
        )
    )

    fun getPuzzlesByCategory(category: PuzzleCategory): List<ChessPuzzle> {
        if (category == PuzzleCategory.ALL) return ALL_PUZZLES
        return ALL_PUZZLES.filter { it.category == category }
    }

    fun getDailyPuzzle(): ChessPuzzle {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % ALL_PUZZLES.size
        return ALL_PUZZLES[index]
    }

    fun verifyAllPuzzlesLegal(): Boolean {
        for (puzzle in ALL_PUZZLES) {
            var state = ChessRulesEngine.fromFen(puzzle.initialFen)
            if (puzzle.solutionUciMoves.isEmpty()) return false
            for (uci in puzzle.solutionUciMoves) {
                val move = ChessRulesEngine.parseUciMove(state, uci) ?: return false
                state = ChessRulesEngine.applyLegalMove(state, move)
            }
        }
        return true
    }
}
