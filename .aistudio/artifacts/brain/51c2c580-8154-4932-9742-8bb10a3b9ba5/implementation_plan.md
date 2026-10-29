# Chess Master — Free, Offline Android Chess Application

Chess Master is a complete, 100% offline Android chess application built natively with Kotlin, Jetpack Compose, and Room Database. It delivers authentic FIDE-compliant gameplay against a 10-level local chess engine or a local friend, paired with real-time position analysis, an evaluation bar, 32+ curated tactical puzzles, interactive chess lessons with an opening explorer, monotonic chess clocks, and persistent game archives—all working seamlessly in airplane mode without external dependencies or cloud APIs.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and product choices have been confirmed from your answers and aligned with the native Android build environment:

- **Confirmed Decision 1 — Native Kotlin & Jetpack Compose Architecture**: Rather than wrapping a web app in Capacitor, the application is built natively in Kotlin with Jetpack Compose and Room SQLite persistence for zero-latency touch responsiveness, native back-navigation handling, and direct Gradle APK generation.
- **Confirmed Decision 2 — Strictly 100% Offline Execution**: Zero network permissions, zero cloud AI dependencies, and zero external CDNs. All engine search, tactical evaluation, puzzle validation, lessons, fonts, and vector piece artwork are bundled locally inside the APK.
- **Confirmed Decision 3 — Enhanced Gameplay & Visual Suite**:
  - **Live Evaluation Bar**: Vertical centipawn/mate advantage gauge rendered alongside the chessboard during gameplay and analysis.
  - **Custom Board Themes**: Four selectable board palettes (*Warm Royal Wood*, *Tournament Green*, *Dark Teal & Gold*, and *Classic Marble*) plus light/dark application themes.
  - **Interactive Opening Explorer**: Step-by-step interactive opening lines integrated into the *Learn Chess* module.
  - **Pass-and-Play Auto Board Flip**: Optional automatic board rotation after each move in Two-Player mode, plus manual flip at any time.

---

## 1. Overview & Core Concept

- **What It Does**: Provides a comprehensive offline chess studio featuring **Play vs. Computer** (10 explicit difficulty levels), **Two-Player Pass & Play**, **Chess Puzzles** (32+ verified tactical puzzles across 6 categories plus a Daily Puzzle), **Learn Chess** (10 interactive lessons + Opening Explorer), **Saved Games** (auto-save and manual slots with full FEN/PGN/clock restoration), and **Statistics & Settings**.
- **Target Audience / Persona**: Chess enthusiasts, casual learners, and tournament players seeking a distraction-free, ad-free, offline mobile chess companion that works anywhere—on flights, commutes, or offline tablets.
- **Key Value**: Full chess rule fidelity (castling, en passant, 4-choice promotion, 50-move rule, threefold repetition, insufficient material, check/checkmate/stalemate) combined with an embedded search engine that provides both realistic opponents and human-readable move explanations without ever needing an internet connection.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Home Hub Flow**: Launching the app presents an atmospheric dark-teal and gold header with a Daily Puzzle quick-card, quick-resume banner for any active unfinished game, and six tactile navigation cards (*Play vs Computer*, *Two Players*, *Chess Puzzles*, *Learn Chess*, *Saved Games*, *Settings & Stats*).
2. **Computer Game Setup & Match Flow**: Users select their color (*White*, *Black*, or *Random*), pick from 10 explicitly mapped difficulty tiers (*Level 1: Beginner* to *Level 10: Champion*) while viewing their historical Win/Draw/Loss record against that tier, choose a clock control (*Untimed*, *1m*, *3m*, *5m*, *10m*, *15m* with optional increment *0s*, *2s*, *5s*, *10s*), and enter the match.
3. **Interactive Chessboard & Analysis Flow**:
   - Tap-to-select and tap-to-move (plus smooth drag-and-drop) with distinct visual indicators for selected squares, legal quiet moves (subtle dots), legal captures (corner rings), last move highlights, and crimson king-in-check glow.
   - Reaching the 8th/1st rank opens an immediate modal promotion selector (*Queen*, *Rook*, *Bishop*, *Knight*).
   - Tapping **Hint** or **Analyze** runs the local engine on a background coroutine to highlight the recommended move, update the live **Evaluation Bar**, display the centipawn/mate score and principal variation (PV) line, and generate a clear tactical explanation (e.g., *"Develops a minor piece toward the center"*, *"Captures undefended material"*, *"Delivers check and restricts king mobility"*).
4. **Two-Player Pass & Play Flow**: Dual-clock layout with top and bottom player banners, captured piece trays with material advantage badges (`+3`), manual flip button, and optional auto-flip after each legal move.
5. **Tactical Puzzles & Daily Puzzle Flow**: Filter puzzles by category (*All*, *Mate in 1*, *Mate in 2*, *Fork*, *Pin*, *Tactical Capture*, *Discovered Attack*) or launch the deterministic offline *Daily Puzzle*. Users play moves on the board, receive immediate feedback (*Correct — Keep going!*, *Puzzle Solved!*, or *Not the best move — Try again*), request a piece-highlight hint without spoiling the destination square, or reveal the full solution with explanation.
6. **Learn Chess & Opening Explorer Flow**: Browse 10 structured interactive lessons (from piece movement and special rules to opening principles and tactical motifs) where each lesson includes a pre-configured demonstration board and an interactive mini-challenge to complete, alongside an interactive **Opening Explorer** covering classic openings (*Italian Game*, *Sicilian Defense*, *Queen's Gambit*, *Ruy Lopez*, *French Defense*, *King's Indian*).

### Visual Identity & Theme

- **Aesthetic Direction**: *Grandmaster Club & Warm Artisanal Wood* — rich dark teal surfaces paired with warm walnut/maple board tones and brushed gold accents.
- **Color Palette & Mood**:
  - **Dark Theme Posture (Default)**:
    - Background Surface: Deep Slate Teal (`#0B1D21`)
    - Elevated Card / Panel Surface: Rich Cypress Teal (`#132A30`)
    - Primary Accent: Brushed Championship Gold (`#D4AF37`)
    - Secondary Accent: Warm Amber Wood (`#C88A4B`)
    - Text Hierarchy: Ivory Cream (`#F5F2EB`) primary text, Muted Sage (`#9FB3B8`) secondary labels
    - Semantic Accents: Emerald Check/Success (`#2EBD85`), Crimson Check/Alert (`#E05252`), Royal Gold Hint Highlight (`#F0C441`)
  - **Light Theme Posture**:
    - Background Surface: Warm Parchment (`#F6F3EC`)
    - Elevated Card Surface: Ivory Wood (`#FFFFFF`) with subtle warm border (`#E2D9C8`)
    - Primary Accent: Deep Forest Teal (`#0F3D3E`) with Gold highlights (`#B8860B`)
  - **Selectable Chessboard Palettes**:
    - *Warm Wood (Default)*: Maple Light (`#EDD6B0`) / Walnut Dark (`#8B5A2B`)
    - *Tournament Green*: Cream (`#EEEED2`) / Classic Green (`#769656`)
    - *Dark Teal & Gold*: Frosted Mint (`#D8E6E7`) / Deep Teal (`#24525A`)
    - *Classic Marble*: Alabaster (`#F0F0F0`) / Charcoal Slate (`#68737A`)
- **Typography & Hierarchy**:
  - **Display / Headers**: Locally bundled *Playfair Display* (`.ttf` in `res/font`) for grandmaster titles, lesson headers, and game status banners.
  - **Body & UI Controls**: Locally bundled *Plus Jakarta Sans* for crisp legibility across buttons, explanations, and statistics.
  - **Move Notation & Clocks**: Locally bundled *JetBrains Mono* for tabular-aligned algebraic move lists (`1. e4 e5`) and jitter-free digital chess clocks.
- **Component Styling & Layout**:
  - Custom `Canvas` and vector-rendered Staunton 2D chess pieces with crisp outlines, inner shading, and high contrast on both light and dark squares.
  - Adaptive layouts supporting compact phones in portrait mode and expanded two-column layouts when space permits.
  - All interactive buttons and board squares respect minimum touch target standards and safe-area window insets.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Pure Kotlin FIDE Rules Engine + Multi-Depth Alpha-Beta/UCI Engine on Coroutines**
  - *Chosen Approach*: Implement a self-contained, bitboard/mailbox hybrid chess rules and search engine in Kotlin speaking standard UCI-style search commands (`position fen`, `go depth/movetime`, `stop`, `bestmove`, `info score cp/mate pv`) executing on `Dispatchers.Default` with full cancellation support.
  - *Why*: Guarantees 100% compatibility across all Android CPU architectures (ARM64, ARMv7, x86_64 emulators, and JVM unit tests) with zero WebView overhead, instant startup, deterministic difficulty scaling across 10 levels (combining search depth 1–7, quiescence search, MVV-LVA move ordering, transposition tables, positional piece-square tables, and controlled tactical noise on lower levels), and genuine multi-PV evaluation for hints and analysis.
  - *Alternatives Considered*: Loading a WebAssembly Stockfish worker inside a hidden Android WebView was rejected because WebView WASM workers suffer from memory throttling on low-end Android devices, cannot run in fast JVM/Robolectric unit tests, and add unnecessary UI latency compared to native Compose.
- **Decision 2: Room SQLite Database for All Offline Persistence**
  - *Chosen Approach*: Use Jetpack Room with reactive `Flow` queries to persist saved games (including complete move history, FEN snapshots, PGN export text, and remaining clock milliseconds), puzzle completion/attempt history, per-difficulty match statistics, lesson completion flags, and user preferences.
  - *Why*: Room provides ACID transactions, instant offline startup, compile-time SQL verification via KSP, and seamless integration with `StateFlow` in the MVVM layer.
- **Decision 3: Monotonic Elapsed-Realtime Chess Clocks**
  - *Chosen Approach*: Drive player clocks using `SystemClock.elapsedRealtime()` deltas rather than naive tick counters.
  - *Why*: Prevents clock drift caused by UI frame drops, backgrounding, or screen locks, and cleanly prevents negative time displays or duplicate timeout triggers.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        UI LAYER (Jetpack Compose)                           │
│                                                                             │
│  ┌──────────────┐  ┌────────────────────┐  ┌─────────────────────────────┐  │
│  │ HomeScreen   │  │ ComputerSetupModal │  │ GameScreen (1P & 2P Modes)  │  │
│  │ - DailyCard  │  │ - Color & 10 Lvls  │  │ - Top/Bottom Player Clocks  │  │
│  │ - QuickResume│  │ - Clock & Incr.    │  │ - Captured Material Trays   │  │
│  │ - Mode Grid  │  │ - Tier W/D/L Stats │  │ - Live Eval Bar + 8x8 Board │  │
│  └──────┬───────┘  └─────────┬──────────┘  │ - MoveSAN Strip & Analysis  │  │
│         │                    │             │ - Promotion & Save Dialogs  │  │
│         ▼                    ▼             └──────────────┬──────────────┘  │
│  ┌────────────────────┐  ┌────────────────────┐           │                 │
│  │ PuzzlesScreen      │  │ LearnChessScreen   │  ┌────────▼──────────────┐  │
│  │ - Category Filter  │  │ - 10 Interactive   │  │ SavedGames & Settings │  │
│  │ - Daily Puzzle     │  │   Lessons          │  │ - Rename / Delete /   │  │
│  │ - Hint / Solution  │  │ - Opening Explorer │  │   Resume              │  │
│  │ - Accuracy Tracker │  │ - Practice Board   │  │ - Board & App Themes  │  │
│  └─────────┬──────────┘  └─────────┬──────────┘  └────────┬──────────────┘  │
└────────────┼───────────────────────┼──────────────────────┼─────────────────┘
             │                       │                      │
             ▼                       ▼                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                   STATE & VIEWMODEL LAYER (StateFlow)                       │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ ChessMasterViewModel                                                  │  │
│  │ - activeGameState: StateFlow<ActiveGameUiState>                       │  │
│  │ - engineState: StateFlow<EngineAnalysisState>                         │  │
│  │ - puzzleState: StateFlow<PuzzleUiState>                               │  │
│  │ - learnState: StateFlow<LearnUiState>                                 │  │
│  │ - savedGames / stats / settings: StateFlow<...>                       │  │
│  └───────────────┬───────────────────────────┬───────────────────────────┘  │
└──────────────────┼───────────────────────────┼──────────────────────────────┘
                   │                           │
                   ▼                           ▼
┌───────────────────────────────────┐ ┌───────────────────────────────────────┐
│   DOMAIN & CHESS ENGINE LAYER     │ │     OFFLINE DATA & ROOM LAYER         │
│                                   │ │                                       │
│ ┌───────────────────────────────┐ │ │ ┌───────────────────────────────────┐ │
│ │ ChessRulesEngine              │ │ │ │ Room AppDatabase (SQLite)         │ │
│ │ - FEN / PGN Parser & Builder  │ │ │ │ - SavedGameEntity (FEN history,   │ │
│ │ - Legal Move Generator        │ │ │ │   PGN, clocks, mode, status)      │ │
│ │ - Check / Checkmate / Draw /  │ │ │ │ -DifficultyStatsEntity (Lv 1..10) │ │
│ │   Castling / En Passant /     │ │ │ │ - PuzzleProgressEntity            │ │
│ │   Promotion / SAN Notation    │ │ │ │ - AppSettingsEntity               │ │
│ └───────────────┬───────────────┘ │ │ └─────────────────┬─────────────────┘ │
│                 │                 │ │                   │                   │
│ ┌───────────────▼───────────────┐ │ │ ┌─────────────────▼─────────────────┐ │
│ │ LocalUciChessEngine           │ │ │ │ Bundled Offline Repositories      │ │
│ │ - 10 Explicit Difficulty Tiers│ │ │ │ - 32+ Validated Tactical Puzzles  │ │
│ │ - Alpha-Beta + Quiescence +   │ │ │ │ - 10 Interactive Chess Lessons    │ │
│ │   TT + Piece-Square Eval      │ │ │ │ - 6 Interactive Opening Lines     │ │
│ │ - BestMove, Eval CP, PV Line  │ │ │ │ - ToneGenerator Sound Synthesizer │ │
│ │   & Tactical Move Explainer   │ │ │ └───────────────────────────────────┘ │
│ └───────────────────────────────┘ │ └───────────────────────────────────────┘
└───────────────────────────────────┘
```

### Data Model & State

1. **Core Chess Domain Models**:
   - `Piece` (`type`: King, Queen, Rook, Bishop, Knight, Pawn; `color`: White, Black)
   - `Square` (`file`: 0..7, `rank`: 0..7, algebraic conversion `a1`..`h8`)
   - `ChessMove` (`from`, `to`, `piece`, `capturedPiece`, `promotionPiece`, `isCastlingKingside`, `isCastlingQueenside`, `isEnPassant`, `san`: Standard Algebraic Notation string, `resultingFen`)
   - `BoardState` (64-square array, active turn, castling rights `KQkq`, en passant target square, halfmove clock, fullmove number, position hash history for threefold repetition, terminal status: `ONGOING`, `CHECK`, `CHECKMATE`, `STALEMATE`, `DRAW_FIFTY_MOVE`, `DRAW_REPETITION`, `DRAW_INSUFFICIENT_MATERIAL`, `DRAW_AGREEMENT`, `RESIGNED`, `TIMEOUT`)
2. **Room Database Entities**:
   - `SavedGameEntity`: `id`, `title`, `gameMode` (`VS_COMPUTER` or `TWO_PLAYER`), `difficultyLevel` (1..10), `playerColor`, `currentFen`, `initialFen`, `moveHistoryJson` (serialized list of moves & FENs for full undo restoration), `pgnText`, `whiteTimeRemainingMs`, `blackTimeRemainingMs`, `incrementMs`, `isTimed`, `status`, `resultSummary`, `isAutoSave`, `updatedAt`.
   - `DifficultyStatsEntity`: `difficultyLevel` (1..10), `gamesPlayed`, `wins`, `draws`, `losses`.
   - `PuzzleProgressEntity`: `puzzleId`, `isSolved`, `attempts`, `hintsUsed`, `lastAttemptAt`.
   - `AppSettingsEntity`: `id = 1`, `isDarkTheme`, `boardTheme`, `pieceStyle`, `showLegalMoves`, `showEvalBar`, `autoFlipTwoPlayer`, `soundEnabled`, `defaultDifficulty`, `defaultClockMinutes`, `defaultIncrementSeconds`, `analysisDepth`, `completedLessonIdsCsv`.

### Interactive Component & State Mapping

- **Square Selection & Legal Move Execution**:
  - Tapping a friendly piece selects it and computes all strictly legal moves (filtering out any move that leaves the friendly king in check).
  - Tapping a highlighted legal destination executes the move: if the move is a pawn reaching rank 8 or 1, the UI pauses and opens `PromotionPickerDialog` (*Q, R, B, N*) before committing.
  - On move commit: `BoardState` advances, SAN notation is appended, captured pieces and material diff update, the active player's monotonic clock adds `incrementMs` and hands off to the opponent, the game auto-saves to Room, and if playing against the computer, `LocalUciChessEngine` launches a cancellable search job.
- **Engine Search & Difficulty Mapping (Levels 1–10)**:
  - Each difficulty tier has an explicit configuration of search depth, node budget, time limit, and evaluation randomness margin.
  - While the engine is thinking, a non-blocking status pill shows `"Stockfish Local Engine thinking (Lv X)..."` with a cancel/restart option; board move inputs are locked until the engine's move is validated against legal moves and applied.
  - Starting a new game, undoing a move, or exiting immediately cancels any in-flight engine coroutine via `Job.cancelAndJoin()`.
- **Undo & Restart Integrity**:
  - In Two-Player mode, **Undo** pops 1 ply (returning to the previous player's turn). In vs. Computer mode, **Undo** pops 2 plies (undoing both the computer's reply and the user's prior move so it remains the user's turn) and restores exact FEN, castling rights, en passant state, move history, and game status.
- **Hints & Position Analysis**:
  - Tapping **Hint** or **Analyze Position** runs a deeper evaluation pass on the current FEN, updates the vertical **Evaluation Bar** (`+1.4`, `-0.8`, `M3`), highlights the engine's recommended `from -> to` squares in gold, displays the Principal Variation sequence in SAN, and generates a plain-English tactical summary explaining why the move works.
- **Offline Sound Effects**:
  - Uses Android's built-in local audio synthesis (`AudioTrack` / `ToneGenerator`) to produce crisp, zero-asset acoustic clicks for quiet moves, captures, check alerts, castling, and victory chimes when sound is enabled in Settings.
