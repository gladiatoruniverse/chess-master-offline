# Chess Master — Free, 100% Offline Android Chess Application

Chess Master is a complete, free, open-source Android chess application built natively with **Kotlin**, **Jetpack Compose**, and **Room SQLite Database**. Every feature—including the 10-level Stockfish-compatible UCI engine, two-player pass-and-play mode, 34 verified tactical puzzles, 10 interactive chess lessons, 6 interactive opening explorer lines, live evaluation bar, monotonic chess clocks, and full FEN/PGN game archives—operates **100% offline in airplane mode** with zero network permissions, zero advertisements, and zero cloud API requirements.

---

## 1. Key Features Implemented & Verified

- [x] **Home Hub**: Grandmaster hero banner, quick-resume card for unfinished games, Daily Offline Puzzle card, and 6 game/study modules.
- [x] **Play vs. Computer (10 Explicit UCI Difficulty Levels)**:
  - Choose **White**, **Black**, or **Random**.
  - 10 configurable difficulty levels (*1. Beginner* through *10. Champion*) with explicit depth, node budget, movetime, and quiescence search settings.
  - Historical Win / Draw / Loss / Win-rate statistics displayed per level.
- [x] **Two-Player Pass & Play Mode**:
  - Play on the same phone or tablet with optional automatic board flip after each move or manual flip at any time.
  - Configurable monotonic chess clocks and captured-piece material advantage trays.
- [x] **FIDE-Compliant 8x8 Chessboard**:
  - Tap-to-select and tap-to-move with highlighted legal quiet moves, capture rings, last-move squares, and king-in-check alerts.
  - Full rules support: castling (O-O & O-O-O), en passant (`e.p.`), 4-choice pawn promotion (*Queen*, *Rook*, *Bishop*, *Knight*), check, checkmate, stalemate, 50-move rule, threefold repetition, and insufficient material.
  - Live **Evaluation Bar** alongside the board and scrollable Standard Algebraic Notation (**SAN**) move strip.
- [x] **Local UCI Engine Hints & Position Analysis**:
  - Suggests best moves with numerical centipawn/mate evaluation (`+1.45`, `-M2`), Principal Variation (`PV`) line, and beginner-friendly tactical explanations computed 100% locally.
- [x] **Save & Resume Games (Room SQLite)**:
  - Automatic save after every move plus manual named saves.
  - Full restoration of FEN, move history, castling rights, en passant target, undo stack, and remaining clock milliseconds.
  - Rename, delete (with confirmation), and inspect FEN/PGN.
- [x] **Offline Chess Puzzles (34 Verified Puzzles + Daily Puzzle)**:
  - Categories: *Mate in 1*, *Mate in 2*, *Fork*, *Pin*, *Tactical Capture*, and *Discovered Attack*.
  - Hint (highlights piece to move and tactical motif without immediately spoiling the destination), Retry, Reveal Solution, and local accuracy tracking.
- [x] **Learn Chess (10 Interactive Lessons + 6 Opening Explorer Lines)**:
  - Covers all 6 piece movements, capturing, check/escaping check, checkmate vs. stalemate, castling, en passant, promotion, algebraic notation, opening principles, and tactical patterns—each with a playable board exercise.
  - Interactive Opening Explorer covering *Italian Game*, *Ruy Lopez*, *Sicilian Defense*, *Queen's Gambit*, *French Defense*, and *King's Indian Defense*.

---

## 2. Build, Test & APK Generation Commands

### Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

### Build Debug APK
```bash
gradle :app:assembleDebug
```
The generated debug APK is output to:
`app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK Locally
```bash
gradle :app:assembleRelease
```
The release APK is output to:
`app/build/outputs/apk/release/app-release.apk`

---

## 3. Offline Acceptance Test Checklist (Real Device / Emulator)

1. Install `app-debug.apk` on an Android phone or tablet.
2. Enable **Airplane Mode** (Wi-Fi and Mobile Data off).
3. Launch **Chess Master** and verify instant offline startup.
4. Play a complete **Two-Player** game (test undo, board flip, and clock countdown).
5. Start and finish a match against the **Computer** (Levels 1–10).
6. Tap **Save**, close the app, relaunch, and resume the saved game from **Saved Games**.
7. Solve the **Daily Puzzle** and a categorized tactical puzzle in **Chess Puzzles**.
8. Request a **Hint** and **Analyze** during a match to view the evaluation bar, PV line, and tactical explanation.
9. Switch between **Dark Teal & Gold** / **Light Parchment** themes and board palettes in **Settings & Stats**.
