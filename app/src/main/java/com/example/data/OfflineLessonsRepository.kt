package com.example.data

data class ChessLesson(
    val id: Int,
    val title: String,
    val subtitle: String,
    val summaryPoints: List<String>,
    val detailedText: String,
    val demoFen: String,
    val exercisePrompt: String,
    val targetMoveUci: String,
    val successExplanation: String
)

data class OpeningLineStep(
    val moveUci: String,
    val san: String,
    val commentary: String
)

data class ChessOpeningLine(
    val id: String,
    val name: String,
    val ecoCode: String,
    val category: String,
    val description: String,
    val steps: List<OpeningLineStep>
)

object OfflineLessonsRepository {

    val ALL_LESSONS: List<ChessLesson> = listOf(
        ChessLesson(
            id = 1,
            title = "1. The Six Chess Pieces & Movements",
            subtitle = "King, Queen, Rook, Bishop, Knight & Pawn",
            summaryPoints = listOf(
                "King (K): Moves 1 square in any direction—protect it at all costs.",
                "Queen (Q): Combines Rook and Bishop power along ranks, files, and diagonals.",
                "Rook (R): Slides horizontally and vertically along open files and ranks.",
                "Bishop (B): Slides diagonally on squares of a single color.",
                "Knight (N): Jumps in an 'L' shape (2+1 squares) and can leap over pieces.",
                "Pawn: Moves forward 1 square (or 2 from its starting rank) and captures diagonally."
            ),
            detailedText = "Each chess piece has a unique movement pattern. Knights are the only pieces able to jump over obstacles, making them strong early in the game, while Rooks and Queens dominate open boards.",
            demoFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            exercisePrompt = "Exercise: Develop White's kingside Knight from g1 to its best natural square f3.",
            targetMoveUci = "g1f3",
            successExplanation = "Excellent! Nf3 develops the knight toward the center and controls d4 and e5."
        ),
        ChessLesson(
            id = 2,
            title = "2. Capturing Pieces & Material Value",
            subtitle = "Trading wisely and winning undefended material",
            summaryPoints = listOf(
                "Pawn = 1 point • Knight = 3 points • Bishop = 3 points",
                "Rook = 5 points • Queen = 9 points • King = Priceless",
                "Always check if a target piece is defended before capturing."
            ),
            detailedText = "You capture an enemy piece by moving onto its square and removing it from the board. Understanding piece values helps you decide when an exchange is favorable.",
            demoFen = "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3",
            exercisePrompt = "Exercise: Wait—in this tactical setup, capture Black's undefended bishop on c5 with your knight!",
            targetMoveUci = "d3c5",
            successExplanation = "Great capture! Winning an undefended minor piece gives you a decisive 3-point advantage."
        ).copy(
            demoFen = "r1bqk1nr/pppp1ppp/8/2b1p3/4P3/3N4/PPPP1PPP/RNBQKB1R w KQkq - 0 4"
        ),
        ChessLesson(
            id = 3,
            title = "3. Check & Getting Out of Check",
            subtitle = "The CPR Rule: Capture, Protect (Block), or Run",
            summaryPoints = listOf(
                "When your King is under direct attack, it is in Check.",
                "You must get out of check immediately on your turn.",
                "Three legal ways out: Capture the attacker, Block the line of attack, or Move the King to a safe square."
            ),
            detailedText = "You may never leave your King in check or move into check. Often, capturing the checking piece or interposing a defender is stronger than moving the King.",
            demoFen = "rnb1kbnr/pppp1ppp/8/4p3/4P2q/5P2/PPPP2PP/RNBQKBNR w KQkq - 1 3",
            exercisePrompt = "Exercise: Black's Queen on h4 is checking your King on e1! Block the check with the g2 pawn.",
            targetMoveUci = "g2g3",
            successExplanation = "Well done! Pushing g3 blocks the check and attacks Black's Queen at the same time."
        ),
        ChessLesson(
            id = 4,
            title = "4. Checkmate vs. Stalemate",
            subtitle = "How to win the game—and avoid accidental draws",
            summaryPoints = listOf(
                "Checkmate: The King is in check and has zero legal moves—this wins the game (1-0 or 0-1).",
                "Stalemate: The player to move is NOT in check, but has zero legal moves—this ends the game in an immediate Draw (1/2-1/2)."
            ),
            detailedText = "When you have a big material advantage in the endgame, make sure every move either delivers check or leaves the enemy King at least one safe square until you deliver checkmate.",
            demoFen = "6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1",
            exercisePrompt = "Exercise: Deliver back-rank Checkmate with your Rook on e1!",
            targetMoveUci = "e1e8",
            successExplanation = "Checkmate! Re8# attacks the Black king while its own pawns on f7, g7, and h7 block escape."
        ),
        ChessLesson(
            id = 5,
            title = "5. Castling Kingside & Queenside",
            subtitle = "Tucking the King to safety and activating the Rook",
            summaryPoints = listOf(
                "Move the King two squares toward the Rook, and jump the Rook over to the other side.",
                "Neither the King nor the chosen Rook may have moved previously.",
                "All squares between them must be empty.",
                "You cannot castle out of check, through check, or into check."
            ),
            detailedText = "Castling is the only move where two pieces move at once. Kingside castling (O-O) places the King on g1/g8 and Rook on f1/f8; Queenside castling (O-O-O) places the King on c1/c8 and Rook on d1/d8.",
            demoFen = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4",
            exercisePrompt = "Exercise: Castle kingside (O-O) by moving the White King from e1 to g1.",
            targetMoveUci = "e1g1",
            successExplanation = "O-O! Your King is now safe behind the f2, g2, and h2 pawns while your Rook enters the game on f1."
        ),
        ChessLesson(
            id = 6,
            title = "6. En Passant ('In Passing')",
            subtitle = "The special pawn capture rule",
            summaryPoints = listOf(
                "When an enemy pawn advances 2 squares from its starting rank and lands directly beside your pawn on the 5th (or 4th) rank...",
                "You may capture it diagonally as if it had only moved 1 square forward.",
                "This capture MUST be played immediately on the very next turn, or the right is lost."
            ),
            detailedText = "En passant prevents pawns from using their initial two-square dash to sneak past enemy pawns unchallenged.",
            demoFen = "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3",
            exercisePrompt = "Exercise: Black just played d7-d5. Capture the d5 pawn en passant by moving your e5 pawn to d6!",
            targetMoveUci = "e5d6",
            successExplanation = "Perfect! exd6 e.p. removes Black's d5 pawn and places your pawn on d6."
        ),
        ChessLesson(
            id = 7,
            title = "7. Pawn Promotion",
            subtitle = "Transforming a 1-point pawn into a Queen, Rook, Bishop, or Knight",
            summaryPoints = listOf(
                "When a pawn reaches the far rank (8th rank for White, 1st rank for Black), it immediately promotes.",
                "You can choose a Queen (Q), Rook (R), Bishop (B), or Knight (N).",
                "Promoting to a Queen is most common, though underpromoting to a Knight can sometimes deliver a fork or avoid stalemate."
            ),
            detailedText = "In the endgame, a single passed pawn that cannot be stopped by enemy pawns often decides the entire match.",
            demoFen = "8/4P1k1/8/8/8/8/6PP/6K1 w - - 0 1",
            exercisePrompt = "Exercise: Advance the e7 pawn to e8 and promote it to a Queen!",
            targetMoveUci = "e7e8q",
            successExplanation = "e8=Q+! Your pawn transforms into a powerful Queen with check."
        ),
        ChessLesson(
            id = 8,
            title = "8. Basic Chess Notation (SAN)",
            subtitle = "Reading and writing algebraic chess moves",
            summaryPoints = listOf(
                "Files are lettered a–h from left to right; Ranks are numbered 1–8 from White's side.",
                "Piece letters: K (King), Q (Queen), R (Rook), B (Bishop), N (Knight). Pawns use no capital letter.",
                "Symbols: 'x' = capture (Nxf3), '+' = check (Qh5+), '#' = checkmate (Qxf7#), 'O-O' = kingside castle."
            ),
            detailedText = "Standard Algebraic Notation (SAN) lets you read chess books, review your move history, and export PGN games effortlessly.",
            demoFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            exercisePrompt = "Exercise: Play the move written in algebraic notation as '1. e4' (push the e2 pawn to e4).",
            targetMoveUci = "e2e4",
            successExplanation = "1. e4! You just played the King's Pawn opening in standard algebraic notation."
        ),
        ChessLesson(
            id = 9,
            title = "9. Golden Opening Principles",
            subtitle = "Control the center, develop pieces, and castle early",
            summaryPoints = listOf(
                "1. Control the center squares (e4, d4, e5, d5) with pawns and minor pieces.",
                "2. Develop Knights and Bishops before moving Queens or Rooks.",
                "3. Avoid moving the same piece twice in the opening without a concrete tactical reason.",
                "4. Castle early (usually within the first 8 moves) to connect your Rooks."
            ),
            detailedText = "Strong openings don't require memorizing 20 moves of theory—following these four classical principles guarantees a harmonious position.",
            demoFen = "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3",
            exercisePrompt = "Exercise: Develop your light-squared Bishop from f1 to c4, eyeing the weak f7 square.",
            targetMoveUci = "f1c4",
            successExplanation = "Bc4! The Italian Game setup develops a piece, controls d5, and prepares immediate kingside castling."
        ),
        ChessLesson(
            id = 10,
            title = "10. Essential Tactical Patterns",
            subtitle = "Forks, Pins, Skewers & Discovered Attacks",
            summaryPoints = listOf(
                "Fork: One piece attacks two or more enemy targets simultaneously (Knights and Pawns excel at forks).",
                "Pin: An attacked piece cannot move without exposing a more valuable piece (like the King or Queen) behind it.",
                "Discovered Attack: Moving one piece uncovers a line of attack from a second piece behind it."
            ),
            detailedText = "Most games are decided by tactics! Before every move, scan for Checks, Captures, and Threats (CCT) for both yourself and your opponent.",
            demoFen = "2r3k1/5ppp/8/3N4/8/8/5PPP/6K1 w - - 0 1",
            exercisePrompt = "Exercise: Execute a Knight Fork! Jump from d5 to e7 to check the King and attack the Rook on c8.",
            targetMoveUci = "d5e7",
            successExplanation = "Ne7+! A textbook royal knight fork—Black must move the King out of check, allowing Nxc8."
        )
    )

    val OPENING_EXPLORER_LINES: List<ChessOpeningLine> = listOf(
        ChessOpeningLine(
            id = "italian_game",
            name = "Italian Game (Giuoco Piano)",
            ecoCode = "C50",
            category = "Open Game (1.e4 e5)",
            description = "Rapid minor-piece development aimed at controlling the center and pressuring Black's f7 square.",
            steps = listOf(
                OpeningLineStep("e2e4", "e4", "White claims the center and opens diagonals for the Queen and Bishop."),
                OpeningLineStep("e7e5", "e5", "Black mirrors White's stake in the center."),
                OpeningLineStep("g1f3", "Nf3", "Develops the kingside Knight and attacks the e5 pawn."),
                OpeningLineStep("b8c6", "Nc6", "Defends the e5 pawn while developing a Queenside piece."),
                OpeningLineStep("f1c4", "Bc4", "The signature Italian move—targets f7 and prepares kingside castling."),
                OpeningLineStep("f8c5", "Bc5", "Giuoco Piano ('Quiet Game'): Black develops harmoniously to c5."),
                OpeningLineStep("c2c3", "c3", "Prepares to build an ideal pawn center with d2-d4."),
                OpeningLineStep("g8f6", "Nf6", "Counterattacks White's e4 pawn and prepares castling.")
            )
        ),
        ChessOpeningLine(
            id = "ruy_lopez",
            name = "Ruy Lopez (Spanish Opening)",
            ecoCode = "C60",
            category = "Open Game (1.e4 e5)",
            description = "One of the oldest and most respected openings in chess, pressuring the defender of the e5 pawn.",
            steps = listOf(
                OpeningLineStep("e2e4", "e4", "Controls d5 and f5 while opening lines."),
                OpeningLineStep("e7e5", "e5", "Claims equal space in the center."),
                OpeningLineStep("g1f3", "Nf3", "Attacks e5 and prepares kingside castling."),
                OpeningLineStep("b8c6", "Nc6", "Protects e5."),
                OpeningLineStep("f1b5", "Bb5", "The Ruy Lopez: pressures the c6 Knight that defends e5."),
                OpeningLineStep("a7a6", "a6", "Morphy's Defense: challenges the Bishop immediately."),
                OpeningLineStep("b5a4", "Ba4", "Maintains the pin tension along the a4-e8 diagonal."),
                OpeningLineStep("g8f6", "Nf6", "Develops the Knight and prepares to castle.")
            )
        ),
        ChessOpeningLine(
            id = "sicilian_defense",
            name = "Sicilian Defense (Open Najdorf/Classical)",
            ecoCode = "B50",
            category = "Semi-Open (1.e4 c5)",
            description = "Black's most combative and asymmetrical response to 1.e4, fighting for d4 from the flank.",
            steps = listOf(
                OpeningLineStep("e2e4", "e4", "White opens with the King's Pawn."),
                OpeningLineStep("c7c5", "c5", "The Sicilian Defense! Fights for d4 without symmetry."),
                OpeningLineStep("g1f3", "Nf3", "Prepares the d2-d4 central break."),
                OpeningLineStep("d7d6", "d6", "Controls e5 and opens the c8-h3 diagonal."),
                OpeningLineStep("d2d4", "d4", "Opens the center for White's active pieces."),
                OpeningLineStep("c5d4", "cxd4", "Black trades a flank c-pawn for White's central d-pawn."),
                OpeningLineStep("f3d4", "Nxd4", "White centralizes the Knight on d4."),
                OpeningLineStep("g8f6", "Nf6", "Attacks White's e4 pawn to force development.")
            )
        ),
        ChessOpeningLine(
            id = "queens_gambit",
            name = "Queen's Gambit Declined",
            ecoCode = "D30",
            category = "Closed Game (1.d4 d5)",
            description = "A classical strategic battle where White offers a wing pawn to gain central dominance.",
            steps = listOf(
                OpeningLineStep("d2d4", "d4", "Controls e5 and c5 with the protected d-pawn."),
                OpeningLineStep("d7d5", "d5", "Prevents White from playing e2-e4 unchallenged."),
                OpeningLineStep("c2c4", "c4", "The Queen's Gambit: challenges Black's d5 anchor."),
                OpeningLineStep("e7e6", "e6", "Queen's Gambit Declined: reinforces d5 solidly."),
                OpeningLineStep("b1c3", "Nc3", "Adds further pressure to d5 and e4."),
                OpeningLineStep("g8f6", "Nf6", "Reinforces d5 and prepares kingside development.")
            )
        ),
        ChessOpeningLine(
            id = "french_defense",
            name = "French Defense",
            ecoCode = "C00",
            category = "Semi-Open (1.e4 e6)",
            description = "A resilient counter-attacking defense where Black invites White's center in order to undermine it with d5 and c5.",
            steps = listOf(
                OpeningLineStep("e2e4", "e4", "White occupies the center."),
                OpeningLineStep("e7e6", "e6", "Prepares the d7-d5 counter-strike."),
                OpeningLineStep("d2d4", "d4", "White establishes the full classical pawn duo."),
                OpeningLineStep("d7d5", "d5", "Challenges White's e4 pawn immediately."),
                OpeningLineStep("b1c3", "Nc3", "Defends e4 and develops a piece."),
                OpeningLineStep("g8f6", "Nf6", "Increases pressure on e4.")
            )
        ),
        ChessOpeningLine(
            id = "kings_indian",
            name = "King's Indian Defense",
            ecoCode = "E60",
            category = "Indian Defense (1.d4 Nf6)",
            description = "A dynamic hypermodern system where Black fianchettoes the kingside Bishop and strikes back in the center.",
            steps = listOf(
                OpeningLineStep("d2d4", "d4", "White claims central space."),
                OpeningLineStep("g8f6", "Nf6", "Controls e4 without committing central pawns yet."),
                OpeningLineStep("c2c4", "c4", "Expands on the queenside and center."),
                OpeningLineStep("g7g6", "g6", "Prepares to fianchetto the dark-squared Bishop on g7."),
                OpeningLineStep("b1c3", "Nc3", "Prepares e2-e4."),
                OpeningLineStep("f8g7", "Bg7", "The King's Indian Bishop commands the long a1-h8 diagonal.")
            )
        )
    )
}
