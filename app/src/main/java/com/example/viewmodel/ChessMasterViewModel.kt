package com.example.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppSettingsEntity
import com.example.data.ChessLesson
import com.example.data.ChessOpeningLine
import com.example.data.ChessPuzzle
import com.example.data.ChessRepository
import com.example.data.DifficultyStatsEntity
import com.example.data.OfflineLessonsRepository
import com.example.data.OfflinePuzzlesRepository
import com.example.data.PuzzleCategory
import com.example.data.PuzzleProgressEntity
import com.example.data.SavedGameEntity
import com.example.domain.ChessBoardState
import com.example.domain.ChessMove
import com.example.domain.ChessRulesEngine
import com.example.domain.EngineDifficultyLevels
import com.example.domain.EngineSearchResult
import com.example.domain.GameMode
import com.example.domain.GameStatus
import com.example.domain.LocalUciChessEngine
import com.example.domain.PieceColor
import com.example.domain.PieceType
import com.example.domain.PlayerColorChoice
import com.example.domain.Square
import com.example.util.ChessSoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    HOME,
    COMPUTER_SETUP,
    GAME_BOARD,
    PUZZLES,
    LEARN_CHESS,
    SAVED_GAMES,
    SETTINGS_STATS
}

data class MatchSnapshot(
    val boardState: ChessBoardState,
    val whiteTimeMs: Long,
    val blackTimeMs: Long
)

data class ActiveMatchUiState(
    val savedGameId: Long? = null,
    val title: String = "New Match",
    val gameMode: GameMode = GameMode.VS_COMPUTER,
    val difficultyLevel: Int = 4,
    val humanColor: PieceColor = PieceColor.WHITE,
    val boardState: ChessBoardState = ChessRulesEngine.createInitialState(),
    val undoSnapshots: List<MatchSnapshot> = emptyList(),
    val selectedSquare: Square? = null,
    val legalDestinations: List<ChessMove> = emptyList(),
    val pendingPromotionFromTo: Pair<Square, Square>? = null,
    val isBoardFlipped: Boolean = false,
    val initialClockMinutes: Int = 10,
    val incrementSeconds: Int = 0,
    val whiteTimeRemainingMs: Long = 600_000L,
    val blackTimeRemainingMs: Long = 600_000L,
    val isEngineThinking: Boolean = false,
    val isAnalyzingPosition: Boolean = false,
    val engineAnalysis: EngineSearchResult? = null,
    val hintSquares: Pair<Square, Square>? = null,
    val engineErrorMessage: String? = null,
    val statusBannerMessage: String? = null,
    val statsRecordedForMatch: Boolean = false
) {
    val isTimed: Boolean get() = initialClockMinutes > 0
}

data class ComputerSetupUiState(
    val colorChoice: PlayerColorChoice = PlayerColorChoice.WHITE,
    val difficultyLevel: Int = 4,
    val clockMinutes: Int = 10,
    val incrementSeconds: Int = 0
)

data class PuzzleUiState(
    val selectedCategory: PuzzleCategory = PuzzleCategory.ALL,
    val currentPuzzle: ChessPuzzle = OfflinePuzzlesRepository.getDailyPuzzle(),
    val isDailyPuzzleMode: Boolean = true,
    val boardState: ChessBoardState = ChessRulesEngine.fromFen(OfflinePuzzlesRepository.getDailyPuzzle().initialFen),
    val solutionStepIndex: Int = 0,
    val selectedSquare: Square? = null,
    val legalDestinations: List<ChessMove> = emptyList(),
    val pendingPromotionFromTo: Pair<Square, Square>? = null,
    val isSolved: Boolean = false,
    val isFailedAttempt: Boolean = false,
    val hintRevealed: Boolean = false,
    val solutionRevealed: Boolean = false,
    val feedbackBanner: String = "Find the best tactical move for ${ChessRulesEngine.fromFen(OfflinePuzzlesRepository.getDailyPuzzle().initialFen).activeColor.displayName}."
)

enum class LearnTab {
    LESSONS,
    OPENING_EXPLORER
}

data class LearnUiState(
    val selectedTab: LearnTab = LearnTab.LESSONS,
    val currentLesson: ChessLesson = OfflineLessonsRepository.ALL_LESSONS.first(),
    val lessonBoardState: ChessBoardState = ChessRulesEngine.fromFen(OfflineLessonsRepository.ALL_LESSONS.first().demoFen),
    val selectedSquare: Square? = null,
    val legalDestinations: List<ChessMove> = emptyList(),
    val exerciseCompleted: Boolean = false,
    val exerciseFeedback: String = OfflineLessonsRepository.ALL_LESSONS.first().exercisePrompt,
    val currentOpening: ChessOpeningLine = OfflineLessonsRepository.OPENING_EXPLORER_LINES.first(),
    val openingBoardState: ChessBoardState = ChessRulesEngine.createInitialState(),
    val openingStepIndex: Int = 0
)

class ChessMasterViewModel(
    private val repository: ChessRepository,
    private val engine: LocalUciChessEngine = LocalUciChessEngine()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _computerSetupState = MutableStateFlow(ComputerSetupUiState())
    val computerSetupState: StateFlow<ComputerSetupUiState> = _computerSetupState.asStateFlow()

    private val _matchState = MutableStateFlow(ActiveMatchUiState())
    val matchState: StateFlow<ActiveMatchUiState> = _matchState.asStateFlow()

    private val _puzzleState = MutableStateFlow(PuzzleUiState())
    val puzzleState: StateFlow<PuzzleUiState> = _puzzleState.asStateFlow()

    private val _learnState = MutableStateFlow(LearnUiState())
    val learnState: StateFlow<LearnUiState> = _learnState.asStateFlow()

    val savedGames: StateFlow<List<SavedGameEntity>> = repository.savedGamesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val difficultyStats: StateFlow<List<DifficultyStatsEntity>> = repository.difficultyStatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val puzzleProgress: StateFlow<List<PuzzleProgressEntity>> = repository.puzzleProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettingsEntity> = repository.settingsFlow
        .map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    private var engineJob: Job? = null
    private var analysisJob: Job? = null
    private var clockJob: Job? = null
    private var lastClockTickRealtimeMs: Long = 0L

    init {
        viewModelScope.launch {
            repository.ensureDefaultsInitialized()
            val savedSettings = settings.value
            _computerSetupState.update {
                it.copy(
                    difficultyLevel = savedSettings.defaultDifficulty,
                    clockMinutes = savedSettings.defaultClockMinutes,
                    incrementSeconds = savedSettings.defaultIncrementSeconds
                )
            }
        }
    }

    // --- Navigation ---
    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.GAME_BOARD) {
            stopClockLoop()
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        return when (_currentScreen.value) {
            AppScreen.HOME -> false
            AppScreen.GAME_BOARD -> {
                cancelEngineJobs()
                stopClockLoop()
                _currentScreen.value = AppScreen.HOME
                true
            }
            else -> {
                _currentScreen.value = AppScreen.HOME
                true
            }
        }
    }

    // --- Computer Setup Configuration ---
    fun updateSetupColor(choice: PlayerColorChoice) {
        _computerSetupState.update { it.copy(colorChoice = choice) }
    }

    fun updateSetupDifficulty(level: Int) {
        _computerSetupState.update { it.copy(difficultyLevel = level.coerceIn(1, 10)) }
    }

    fun updateSetupClock(minutes: Int, incrementSeconds: Int) {
        _computerSetupState.update {
            it.copy(clockMinutes = minutes, incrementSeconds = incrementSeconds)
        }
    }

    fun startComputerMatchFromSetup() {
        val setup = _computerSetupState.value
        val resolvedColor = when (setup.colorChoice) {
            PlayerColorChoice.WHITE -> PieceColor.WHITE
            PlayerColorChoice.BLACK -> PieceColor.BLACK
            PlayerColorChoice.RANDOM -> if (Random.nextBoolean()) PieceColor.WHITE else PieceColor.BLACK
        }
        startNewMatch(
            gameMode = GameMode.VS_COMPUTER,
            difficultyLevel = setup.difficultyLevel,
            humanColor = resolvedColor,
            clockMinutes = setup.clockMinutes,
            incrementSeconds = setup.incrementSeconds
        )
    }

    fun startTwoPlayerMatch(
        clockMinutes: Int = settings.value.defaultClockMinutes,
        incrementSeconds: Int = settings.value.defaultIncrementSeconds
    ) {
        startNewMatch(
            gameMode = GameMode.TWO_PLAYER,
            difficultyLevel = settings.value.defaultDifficulty,
            humanColor = PieceColor.WHITE,
            clockMinutes = clockMinutes,
            incrementSeconds = incrementSeconds
        )
    }

    fun startNewMatch(
        gameMode: GameMode,
        difficultyLevel: Int,
        humanColor: PieceColor,
        clockMinutes: Int,
        incrementSeconds: Int
    ) {
        cancelEngineJobs()
        stopClockLoop()
        engine.processUciCommand("ucinewgame")

        val initialBoard = ChessRulesEngine.createInitialState()
        val initialTimeMs = clockMinutes * 60_000L
        val title = if (gameMode == GameMode.VS_COMPUTER) {
            val tier = EngineDifficultyLevels.getByLevel(difficultyLevel)
            "vs Stockfish Lv.${tier.level} (${tier.title})"
        } else {
            "Two-Player Match"
        }

        _matchState.value = ActiveMatchUiState(
            savedGameId = null,
            title = title,
            gameMode = gameMode,
            difficultyLevel = difficultyLevel,
            humanColor = humanColor,
            boardState = initialBoard,
            undoSnapshots = emptyList(),
            selectedSquare = null,
            legalDestinations = emptyList(),
            pendingPromotionFromTo = null,
            isBoardFlipped = (gameMode == GameMode.VS_COMPUTER && humanColor == PieceColor.BLACK),
            initialClockMinutes = clockMinutes,
            incrementSeconds = incrementSeconds,
            whiteTimeRemainingMs = initialTimeMs,
            blackTimeRemainingMs = initialTimeMs,
            isEngineThinking = false,
            isAnalyzingPosition = false,
            engineAnalysis = null,
            hintSquares = null,
            engineErrorMessage = null,
            statusBannerMessage = null,
            statsRecordedForMatch = false
        )

        _currentScreen.value = AppScreen.GAME_BOARD
        startClockLoopIfNeeded()

        if (gameMode == GameMode.VS_COMPUTER && humanColor == PieceColor.BLACK) {
            triggerComputerReplyIfNeeded()
        }
    }

    // --- Board Interaction (Gameplay) ---
    fun onBoardSquareClicked(square: Square) {
        val current = _matchState.value
        if (current.boardState.status.isTerminal || current.isEngineThinking) return

        // In vs Computer mode, only allow input on human's turn
        if (current.gameMode == GameMode.VS_COMPUTER &&
            current.boardState.activeColor != current.humanColor
        ) {
            return
        }

        // Check if tapped square is a legal destination for currently selected square
        val matchingMoves = current.legalDestinations.filter { it.to == square }
        if (current.selectedSquare != null && matchingMoves.isNotEmpty()) {
            if (matchingMoves.any { it.promotion != null }) {
                _matchState.update {
                    it.copy(pendingPromotionFromTo = current.selectedSquare to square)
                }
            } else {
                executeMoveOnBoard(matchingMoves.first())
            }
            return
        }

        // Otherwise select friendly piece
        val tappedPiece = current.boardState.pieceAt(square)
        if (tappedPiece != null && tappedPiece.color == current.boardState.activeColor) {
            val legalForSq = ChessRulesEngine.generateLegalMovesForSquare(current.boardState, square)
            _matchState.update {
                it.copy(
                    selectedSquare = square,
                    legalDestinations = legalForSq,
                    pendingPromotionFromTo = null
                )
            }
        } else {
            _matchState.update {
                it.copy(
                    selectedSquare = null,
                    legalDestinations = emptyList(),
                    pendingPromotionFromTo = null
                )
            }
        }
    }

    fun onPromotionPieceSelected(promotionType: PieceType) {
        val current = _matchState.value
        val (from, to) = current.pendingPromotionFromTo ?: return
        val move = current.legalDestinations.firstOrNull {
            it.from == from && it.to == to && it.promotion == promotionType
        } ?: return
        executeMoveOnBoard(move)
    }

    fun dismissPromotionDialog() {
        _matchState.update { it.copy(pendingPromotionFromTo = null) }
    }

    private fun executeMoveOnBoard(move: ChessMove) {
        val current = _matchState.value
        val snapshot = MatchSnapshot(
            boardState = current.boardState,
            whiteTimeMs = current.whiteTimeRemainingMs,
            blackTimeMs = current.blackTimeRemainingMs
        )

        val movingColor = current.boardState.activeColor
        val nextBoard = ChessRulesEngine.applyLegalMove(current.boardState, move)

        // Apply increment if timed and game continues
        val incMs = if (current.isTimed) current.incrementSeconds * 1_000L else 0L
        val updatedWhiteMs = if (movingColor == PieceColor.WHITE && current.isTimed) {
            current.whiteTimeRemainingMs + incMs
        } else {
            current.whiteTimeRemainingMs
        }
        val updatedBlackMs = if (movingColor == PieceColor.BLACK && current.isTimed) {
            current.blackTimeRemainingMs + incMs
        } else {
            current.blackTimeRemainingMs
        }

        val shouldFlipInTwoPlayer = current.gameMode == GameMode.TWO_PLAYER &&
            settings.value.autoFlipTwoPlayer &&
            !nextBoard.status.isTerminal

        val nextFlipped = if (shouldFlipInTwoPlayer) {
            nextBoard.activeColor == PieceColor.BLACK
        } else {
            current.isBoardFlipped
        }

        // Play offline sound effect if enabled
        val soundEnabled = settings.value.soundEnabled
        when {
            nextBoard.status.isTerminal -> ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.GAME_END, soundEnabled)
            nextBoard.status == GameStatus.CHECK -> ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.CHECK, soundEnabled)
            move.isKingsideCastle || move.isQueensideCastle -> ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.CASTLE, soundEnabled)
            move.capturedPiece != null -> ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.CAPTURE, soundEnabled)
            else -> ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.MOVE, soundEnabled)
        }

        lastClockTickRealtimeMs = SystemClock.elapsedRealtime()

        _matchState.update {
            it.copy(
                boardState = nextBoard,
                undoSnapshots = it.undoSnapshots + snapshot,
                selectedSquare = null,
                legalDestinations = emptyList(),
                pendingPromotionFromTo = null,
                isBoardFlipped = nextFlipped,
                whiteTimeRemainingMs = updatedWhiteMs,
                blackTimeRemainingMs = updatedBlackMs,
                hintSquares = null,
                engineErrorMessage = null
            )
        }

        autoSaveActiveMatch()

        if (nextBoard.status.isTerminal) {
            stopClockLoop()
            recordStatsIfTerminal()
        } else {
            startClockLoopIfNeeded()
            triggerComputerReplyIfNeeded()
        }
    }

    private fun triggerComputerReplyIfNeeded() {
        val current = _matchState.value
        if (current.gameMode != GameMode.VS_COMPUTER) return
        if (current.boardState.status.isTerminal) return
        if (current.boardState.activeColor == current.humanColor) return

        engineJob?.cancel()
        engineJob = viewModelScope.launch {
            _matchState.update {
                it.copy(
                    isEngineThinking = true,
                    engineErrorMessage = null
                )
            }
            try {
                // Brief natural visual pacing so user sees their move land
                delay(160L)
                val boardBefore = _matchState.value.boardState
                val result = engine.findBestMove(
                    state = boardBefore,
                    difficultyLevel = _matchState.value.difficultyLevel
                )
                if (!isActive) return@launch

                // Validate engine move against current chess position before applying
                val validatedMove = ChessRulesEngine.generateLegalMoves(boardBefore).firstOrNull {
                    it.from == result.bestMove.from &&
                        it.to == result.bestMove.to &&
                        it.promotion == result.bestMove.promotion
                }
                if (validatedMove == null) {
                    _matchState.update {
                        it.copy(
                            isEngineThinking = false,
                            engineErrorMessage = "Engine returned an out-of-sync move; please retry or undo."
                        )
                    }
                    return@launch
                }

                _matchState.update {
                    it.copy(
                        isEngineThinking = false,
                        engineAnalysis = result
                    )
                }
                executeMoveOnBoard(validatedMove)
            } catch (e: Exception) {
                if (isActive) {
                    _matchState.update {
                        it.copy(
                            isEngineThinking = false,
                            engineErrorMessage = "Engine calculation interrupted: ${e.message ?: "Unknown error"}"
                        )
                    }
                }
            }
        }
    }

    fun cancelEngineCalculation() {
        engineJob?.cancel()
        analysisJob?.cancel()
        _matchState.update {
            it.copy(
                isEngineThinking = false,
                isAnalyzingPosition = false,
                statusBannerMessage = "Engine calculation cancelled."
            )
        }
    }

    private fun cancelEngineJobs() {
        engineJob?.cancel()
        analysisJob?.cancel()
        engineJob = null
        analysisJob = null
    }

    // --- Hints & Position Analysis ---
    fun requestHint() {
        val current = _matchState.value
        if (current.boardState.status.isTerminal || current.isEngineThinking) return

        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _matchState.update {
                it.copy(
                    isAnalyzingPosition = true,
                    engineErrorMessage = null
                )
            }
            try {
                val depth = settings.value.analysisDepth.coerceIn(2, 5)
                val result = engine.findBestMove(
                    state = current.boardState,
                    difficultyLevel = 10,
                    overrideDepth = depth
                )
                if (!isActive) return@launch
                _matchState.update {
                    it.copy(
                        isAnalyzingPosition = false,
                        engineAnalysis = result,
                        hintSquares = result.bestMove.from to result.bestMove.to,
                        selectedSquare = result.bestMove.from,
                        legalDestinations = ChessRulesEngine.generateLegalMovesForSquare(
                            current.boardState,
                            result.bestMove.from
                        )
                    )
                }
            } catch (e: Exception) {
                if (isActive) {
                    _matchState.update {
                        it.copy(
                            isAnalyzingPosition = false,
                            engineErrorMessage = "Failed to compute hint: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    fun analyzeCurrentPosition() {
        requestHint()
    }

    // --- Game Controls: Undo, Flip, Resign, Draw, Restart ---
    fun undoLastMove() {
        cancelEngineJobs()
        val current = _matchState.value
        val snapshots = current.undoSnapshots
        if (snapshots.isEmpty()) return

        // In vs Computer mode, if it is currently the human's turn and there are >= 2 snapshots,
        // pop 2 plies so it returns to the human's previous turn.
        val popCount = if (current.gameMode == GameMode.VS_COMPUTER &&
            current.boardState.activeColor == current.humanColor &&
            snapshots.size >= 2
        ) {
            2
        } else {
            1
        }

        val targetIndex = (snapshots.size - popCount).coerceAtLeast(0)
        val restored = snapshots[targetIndex]
        val remainingSnapshots = snapshots.take(targetIndex)

        lastClockTickRealtimeMs = SystemClock.elapsedRealtime()
        _matchState.update {
            it.copy(
                boardState = restored.boardState,
                undoSnapshots = remainingSnapshots,
                selectedSquare = null,
                legalDestinations = emptyList(),
                pendingPromotionFromTo = null,
                whiteTimeRemainingMs = restored.whiteTimeMs,
                blackTimeRemainingMs = restored.blackTimeMs,
                isEngineThinking = false,
                hintSquares = null,
                engineErrorMessage = null,
                statusBannerMessage = "Move undone."
            )
        }
        startClockLoopIfNeeded()
        autoSaveActiveMatch()
    }

    fun toggleBoardFlip() {
        _matchState.update { it.copy(isBoardFlipped = !it.isBoardFlipped) }
    }

    fun resignCurrentGame() {
        cancelEngineJobs()
        stopClockLoop()
        val current = _matchState.value
        if (current.boardState.status.isTerminal) return

        val resigningColor = if (current.gameMode == GameMode.VS_COMPUTER) {
            current.humanColor
        } else {
            current.boardState.activeColor
        }
        val winner = resigningColor.opposite()
        val updatedBoard = current.boardState.copy(
            status = GameStatus.RESIGNED,
            winner = winner
        )
        _matchState.update {
            it.copy(
                boardState = updatedBoard,
                selectedSquare = null,
                legalDestinations = emptyList(),
                statusBannerMessage = "${resigningColor.displayName} resigned. ${winner.displayName} wins!"
            )
        }
        ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.GAME_END, settings.value.soundEnabled)
        autoSaveActiveMatch()
        recordStatsIfTerminal()
    }

    fun offerOrClaimDraw() {
        val current = _matchState.value
        if (current.boardState.status.isTerminal) return

        val board = current.boardState
        val canClaimRuleDraw = board.halfMoveClock >= 100 ||
            board.positionHistory.count { it == board.corePositionSignature() } >= 3 ||
            ChessRulesEngine.isInsufficientMaterial(board)

        val acceptDraw = if (current.gameMode == GameMode.TWO_PLAYER || canClaimRuleDraw) {
            true
        } else {
            // In vs Computer mode, engine accepts draw if position evaluation is close to equal (within 0.6 pawns) after at least 10 plies
            val evalCp = engine.evaluateStaticWhitePerspective(board)
            board.moveHistory.size >= 10 && kotlin.math.abs(evalCp) <= 65
        }

        if (acceptDraw) {
            cancelEngineJobs()
            stopClockLoop()
            val updatedBoard = board.copy(
                status = GameStatus.DRAW_AGREEMENT,
                winner = null
            )
            _matchState.update {
                it.copy(
                    boardState = updatedBoard,
                    selectedSquare = null,
                    legalDestinations = emptyList(),
                    statusBannerMessage = "Game drawn by agreement."
                )
            }
            ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.GAME_END, settings.value.soundEnabled)
            autoSaveActiveMatch()
            recordStatsIfTerminal()
        } else {
            _matchState.update {
                it.copy(
                    statusBannerMessage = "Computer declines the draw offer and plays on!"
                )
            }
        }
    }

    fun restartCurrentMatch() {
        val current = _matchState.value
        startNewMatch(
            gameMode = current.gameMode,
            difficultyLevel = current.difficultyLevel,
            humanColor = current.humanColor,
            clockMinutes = current.initialClockMinutes,
            incrementSeconds = current.incrementSeconds
        )
    }

    // --- Monotonic Chess Clock ---
    private fun startClockLoopIfNeeded() {
        stopClockLoop()
        val current = _matchState.value
        if (!current.isTimed || current.boardState.status.isTerminal) return

        lastClockTickRealtimeMs = SystemClock.elapsedRealtime()
        clockJob = viewModelScope.launch {
            while (isActive) {
                delay(100L)
                val now = SystemClock.elapsedRealtime()
                val elapsed = (now - lastClockTickRealtimeMs).coerceAtLeast(0L)
                lastClockTickRealtimeMs = now

                val state = _matchState.value
                if (!state.isTimed || state.boardState.status.isTerminal) break

                val activeColor = state.boardState.activeColor
                if (activeColor == PieceColor.WHITE) {
                    val rem = (state.whiteTimeRemainingMs - elapsed).coerceAtLeast(0L)
                    if (rem == 0L) {
                        handleClockTimeout(PieceColor.WHITE)
                        break
                    } else {
                        _matchState.update { it.copy(whiteTimeRemainingMs = rem) }
                    }
                } else {
                    val rem = (state.blackTimeRemainingMs - elapsed).coerceAtLeast(0L)
                    if (rem == 0L) {
                        handleClockTimeout(PieceColor.BLACK)
                        break
                    } else {
                        _matchState.update { it.copy(blackTimeRemainingMs = rem) }
                    }
                }
            }
        }
    }

    private fun stopClockLoop() {
        clockJob?.cancel()
        clockJob = null
    }

    private fun handleClockTimeout(flaggedColor: PieceColor) {
        cancelEngineJobs()
        stopClockLoop()
        val current = _matchState.value
        if (current.boardState.status.isTerminal) return

        val winner = flaggedColor.opposite()
        val updatedBoard = current.boardState.copy(
            status = GameStatus.TIMEOUT,
            winner = winner
        )
        _matchState.update {
            it.copy(
                boardState = updatedBoard,
                whiteTimeRemainingMs = if (flaggedColor == PieceColor.WHITE) 0L else it.whiteTimeRemainingMs,
                blackTimeRemainingMs = if (flaggedColor == PieceColor.BLACK) 0L else it.blackTimeRemainingMs,
                selectedSquare = null,
                legalDestinations = emptyList(),
                statusBannerMessage = "${flaggedColor.displayName} ran out of time! ${winner.displayName} wins."
            )
        }
        ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.GAME_END, settings.value.soundEnabled)
        autoSaveActiveMatch()
        recordStatsIfTerminal()
    }

    // --- Save & Resume Games (Room Persistence) ---
    private fun autoSaveActiveMatch() {
        val current = _matchState.value
        if (current.boardState.moveHistory.isEmpty()) return
        viewModelScope.launch {
            val entity = buildSavedGameEntity(current, customTitle = null, isAutoSave = true)
            repository.saveGame(entity)
        }
    }

    fun saveMatchManually(customName: String) {
        val current = _matchState.value
        viewModelScope.launch {
            val title = customName.trim().ifEmpty { current.title }
            val entity = buildSavedGameEntity(current, customTitle = title, isAutoSave = false)
            val newId = repository.saveGame(entity)
            _matchState.update {
                it.copy(
                    savedGameId = newId,
                    title = title,
                    statusBannerMessage = "Game saved as \"$title\"."
                )
            }
        }
    }

    private fun buildSavedGameEntity(
        state: ActiveMatchUiState,
        customTitle: String?,
        isAutoSave: Boolean
    ): SavedGameEntity {
        val board = state.boardState
        val encodedMoves = board.moveHistory.joinToString(";;") { m ->
            "${m.from.toAlgebraic()}|${m.to.toAlgebraic()}|${m.promotion?.fenLower ?: ""}|${m.san}|${m.previousFen}|${m.resultingFen}"
        }
        val encodedPositions = board.positionHistory.joinToString(";;")
        val pgn = ChessRulesEngine.exportPgn(
            state = board,
            whiteName = if (state.gameMode == GameMode.VS_COMPUTER && state.humanColor == PieceColor.BLACK) "Stockfish Local" else "White",
            blackName = if (state.gameMode == GameMode.VS_COMPUTER && state.humanColor == PieceColor.WHITE) "Stockfish Local" else "Black",
            event = customTitle ?: state.title
        )
        val summary = when {
            board.status == GameStatus.CHECKMATE -> "Checkmate • ${board.winner?.displayName} won"
            board.status == GameStatus.RESIGNED -> "Resignation • ${board.winner?.displayName} won"
            board.status == GameStatus.TIMEOUT -> "Time Out • ${board.winner?.displayName} won"
            board.status.isTerminal -> board.status.label
            else -> "${board.activeColor.displayName} to move • Move ${board.fullMoveNumber}"
        }
        return SavedGameEntity(
            id = if (!isAutoSave && state.savedGameId != null) state.savedGameId else 0L,
            title = customTitle ?: if (isAutoSave) "Auto-Save: ${state.title}" else state.title,
            gameMode = state.gameMode.name,
            difficultyLevel = state.difficultyLevel,
            playerColor = state.humanColor.name,
            initialFen = ChessBoardState.STARTING_FEN,
            currentFen = board.toFen(),
            moveHistoryEncoded = encodedMoves,
            positionHistoryEncoded = encodedPositions,
            pgnText = pgn,
            whiteTimeRemainingMs = state.whiteTimeRemainingMs,
            blackTimeRemainingMs = state.blackTimeRemainingMs,
            initialClockMinutes = state.initialClockMinutes,
            incrementSeconds = state.incrementSeconds,
            status = board.status.name,
            winnerColor = board.winner?.name,
            resultSummary = summary,
            isAutoSave = isAutoSave,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun loadAndResumeSavedGame(entity: SavedGameEntity) {
        cancelEngineJobs()
        stopClockLoop()

        // Replay moves from initialFen to rebuild full undoSnapshots and exact moveHistory
        var runningBoard = ChessRulesEngine.fromFen(entity.initialFen)
        val snapshots = mutableListOf<MatchSnapshot>()
        if (entity.moveHistoryEncoded.isNotBlank()) {
            val moveTokens = entity.moveHistoryEncoded.split(";;")
            for (token in moveTokens) {
                val parts = token.split("|")
                if (parts.size >= 2) {
                    val uci = parts[0] + parts[1] + parts.getOrNull(2).orEmpty()
                    val parsed = ChessRulesEngine.parseUciMove(runningBoard, uci)
                    if (parsed != null) {
                        snapshots.add(
                            MatchSnapshot(
                                boardState = runningBoard,
                                whiteTimeMs = entity.whiteTimeRemainingMs,
                                blackTimeMs = entity.blackTimeRemainingMs
                            )
                        )
                        runningBoard = ChessRulesEngine.applyLegalMove(runningBoard, parsed)
                    }
                }
            }
        } else {
            runningBoard = ChessRulesEngine.fromFen(entity.currentFen)
        }

        val savedStatus = runCatching { GameStatus.valueOf(entity.status) }.getOrDefault(runningBoard.status)
        val savedWinner = entity.winnerColor?.let { runCatching { PieceColor.valueOf(it) }.getOrNull() }
        val finalBoard = runningBoard.copy(status = savedStatus, winner = savedWinner)

        val mode = runCatching { GameMode.valueOf(entity.gameMode) }.getOrDefault(GameMode.VS_COMPUTER)
        val humanColor = runCatching { PieceColor.valueOf(entity.playerColor) }.getOrDefault(PieceColor.WHITE)

        _matchState.value = ActiveMatchUiState(
            savedGameId = if (entity.isAutoSave) null else entity.id,
            title = entity.title.removePrefix("Auto-Save: "),
            gameMode = mode,
            difficultyLevel = entity.difficultyLevel,
            humanColor = humanColor,
            boardState = finalBoard,
            undoSnapshots = snapshots,
            selectedSquare = null,
            legalDestinations = emptyList(),
            pendingPromotionFromTo = null,
            isBoardFlipped = (mode == GameMode.VS_COMPUTER && humanColor == PieceColor.BLACK),
            initialClockMinutes = entity.initialClockMinutes,
            incrementSeconds = entity.incrementSeconds,
            whiteTimeRemainingMs = entity.whiteTimeRemainingMs,
            blackTimeRemainingMs = entity.blackTimeRemainingMs,
            statsRecordedForMatch = finalBoard.status.isTerminal
        )

        _currentScreen.value = AppScreen.GAME_BOARD
        if (!finalBoard.status.isTerminal) {
            startClockLoopIfNeeded()
            triggerComputerReplyIfNeeded()
        }
    }

    fun renameSavedGame(id: Long, newTitle: String) {
        viewModelScope.launch {
            repository.renameSavedGame(id, newTitle)
        }
    }

    fun deleteSavedGame(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedGame(id)
        }
    }

    private fun recordStatsIfTerminal() {
        val current = _matchState.value
        if (current.statsRecordedForMatch || !current.boardState.status.isTerminal) return
        if (current.gameMode != GameMode.VS_COMPUTER) return

        val outcome = when (current.boardState.winner) {
            current.humanColor -> "WIN"
            null -> "DRAW"
            else -> "LOSS"
        }
        _matchState.update { it.copy(statsRecordedForMatch = true) }
        viewModelScope.launch {
            repository.recordMatchOutcome(current.difficultyLevel, outcome)
        }
    }

    // --- Puzzles Logic ---
    fun selectPuzzleCategory(category: PuzzleCategory) {
        val list = OfflinePuzzlesRepository.getPuzzlesByCategory(category)
        val first = list.firstOrNull() ?: OfflinePuzzlesRepository.ALL_PUZZLES.first()
        loadPuzzle(first, category = category, isDaily = false)
    }

    fun openDailyPuzzle() {
        val daily = OfflinePuzzlesRepository.getDailyPuzzle()
        loadPuzzle(daily, category = PuzzleCategory.ALL, isDaily = true)
        _currentScreen.value = AppScreen.PUZZLES
    }

    fun loadPuzzle(
        puzzle: ChessPuzzle,
        category: PuzzleCategory = _puzzleState.value.selectedCategory,
        isDaily: Boolean = false
    ) {
        val initialBoard = ChessRulesEngine.fromFen(puzzle.initialFen)
        _puzzleState.value = PuzzleUiState(
            selectedCategory = category,
            currentPuzzle = puzzle,
            isDailyPuzzleMode = isDaily,
            boardState = initialBoard,
            solutionStepIndex = 0,
            selectedSquare = null,
            legalDestinations = emptyList(),
            pendingPromotionFromTo = null,
            isSolved = false,
            isFailedAttempt = false,
            hintRevealed = false,
            solutionRevealed = false,
            feedbackBanner = "Find the best move for ${initialBoard.activeColor.displayName}."
        )
    }

    fun onPuzzleSquareClicked(square: Square) {
        val current = _puzzleState.value
        if (current.isSolved || current.solutionRevealed) return

        val matchingMoves = current.legalDestinations.filter { it.to == square }
        if (current.selectedSquare != null && matchingMoves.isNotEmpty()) {
            if (matchingMoves.any { it.promotion != null }) {
                _puzzleState.update { it.copy(pendingPromotionFromTo = current.selectedSquare to square) }
            } else {
                submitPuzzleMove(matchingMoves.first())
            }
            return
        }

        val tappedPiece = current.boardState.pieceAt(square)
        if (tappedPiece != null && tappedPiece.color == current.boardState.activeColor) {
            val legal = ChessRulesEngine.generateLegalMovesForSquare(current.boardState, square)
            _puzzleState.update {
                it.copy(
                    selectedSquare = square,
                    legalDestinations = legal,
                    pendingPromotionFromTo = null
                )
            }
        } else {
            _puzzleState.update {
                it.copy(selectedSquare = null, legalDestinations = emptyList())
            }
        }
    }

    fun onPuzzlePromotionSelected(promotionType: PieceType) {
        val current = _puzzleState.value
        val (from, to) = current.pendingPromotionFromTo ?: return
        val move = current.legalDestinations.firstOrNull {
            it.from == from && it.to == to && it.promotion == promotionType
        } ?: return
        submitPuzzleMove(move)
    }

    private fun submitPuzzleMove(move: ChessMove) {
        val current = _puzzleState.value
        val expectedUci = current.currentPuzzle.solutionUciMoves.getOrNull(current.solutionStepIndex) ?: return

        if (move.toUci().equals(expectedUci, ignoreCase = true)) {
            var nextBoard = ChessRulesEngine.applyLegalMove(current.boardState, move)
            var nextStep = current.solutionStepIndex + 1

            // If there is an automatic opponent response in a multi-move puzzle, apply it
            if (nextStep < current.currentPuzzle.solutionUciMoves.size) {
                val oppUci = current.currentPuzzle.solutionUciMoves[nextStep]
                val oppMove = ChessRulesEngine.parseUciMove(nextBoard, oppUci)
                if (oppMove != null) {
                    nextBoard = ChessRulesEngine.applyLegalMove(nextBoard, oppMove)
                    nextStep++
                }
            }

            val solvedNow = nextStep >= current.currentPuzzle.solutionUciMoves.size
            if (solvedNow) {
                ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.PUZZLE_SUCCESS, settings.value.soundEnabled)
                viewModelScope.launch {
                    repository.recordPuzzleAttempt(
                        puzzleId = current.currentPuzzle.id,
                        solved = true,
                        usedHint = current.hintRevealed
                    )
                }
            } else {
                ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.MOVE, settings.value.soundEnabled)
            }

            _puzzleState.update {
                it.copy(
                    boardState = nextBoard,
                    solutionStepIndex = nextStep,
                    selectedSquare = null,
                    legalDestinations = emptyList(),
                    pendingPromotionFromTo = null,
                    isSolved = solvedNow,
                    isFailedAttempt = false,
                    feedbackBanner = if (solvedNow) {
                        "Puzzle Solved! ${current.currentPuzzle.explanation}"
                    } else {
                        "Correct move! Keep going—find the finishing continuation."
                    }
                )
            }
        } else {
            ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.ERROR, settings.value.soundEnabled)
            viewModelScope.launch {
                repository.recordPuzzleAttempt(
                    puzzleId = current.currentPuzzle.id,
                    solved = false,
                    usedHint = current.hintRevealed
                )
            }
            _puzzleState.update {
                it.copy(
                    selectedSquare = null,
                    legalDestinations = emptyList(),
                    pendingPromotionFromTo = null,
                    isFailedAttempt = true,
                    feedbackBanner = "Not the best tactical move (${move.toUci()}). Try again or request a hint!"
                )
            }
        }
    }

    fun requestPuzzleHint() {
        val current = _puzzleState.value
        val expectedUci = current.currentPuzzle.solutionUciMoves.getOrNull(current.solutionStepIndex)
        val hintFromSq = expectedUci?.take(2)?.let { Square.fromAlgebraic(it) }
        val legal = if (hintFromSq != null) {
            ChessRulesEngine.generateLegalMovesForSquare(current.boardState, hintFromSq)
        } else {
            emptyList()
        }
        _puzzleState.update {
            it.copy(
                hintRevealed = true,
                selectedSquare = hintFromSq,
                legalDestinations = legal,
                feedbackBanner = "Hint: ${current.currentPuzzle.hintText}"
            )
        }
    }

    fun revealPuzzleSolution() {
        val current = _puzzleState.value
        var board = ChessRulesEngine.fromFen(current.currentPuzzle.initialFen)
        for (uci in current.currentPuzzle.solutionUciMoves) {
            val m = ChessRulesEngine.parseUciMove(board, uci) ?: break
            board = ChessRulesEngine.applyLegalMove(board, m)
        }
        _puzzleState.update {
            it.copy(
                boardState = board,
                solutionRevealed = true,
                selectedSquare = null,
                legalDestinations = emptyList(),
                feedbackBanner = "Solution: ${current.currentPuzzle.explanation}"
            )
        }
    }

    fun retryCurrentPuzzle() {
        val current = _puzzleState.value
        loadPuzzle(current.currentPuzzle, current.selectedCategory, current.isDailyPuzzleMode)
    }

    fun nextPuzzleInCategory() {
        val current = _puzzleState.value
        val list = OfflinePuzzlesRepository.getPuzzlesByCategory(current.selectedCategory)
        if (list.isEmpty()) return
        val idx = list.indexOfFirst { it.id == current.currentPuzzle.id }
        val next = list[(idx + 1) % list.size]
        loadPuzzle(next, current.selectedCategory, isDaily = false)
    }

    // --- Learn Chess & Opening Explorer ---
    fun selectLearnTab(tab: LearnTab) {
        _learnState.update { it.copy(selectedTab = tab) }
    }

    fun selectLesson(lesson: ChessLesson) {
        val board = ChessRulesEngine.fromFen(lesson.demoFen)
        _learnState.update {
            it.copy(
                currentLesson = lesson,
                lessonBoardState = board,
                selectedSquare = null,
                legalDestinations = emptyList(),
                exerciseCompleted = false,
                exerciseFeedback = lesson.exercisePrompt
            )
        }
    }

    fun onLessonBoardSquareClicked(square: Square) {
        val current = _learnState.value
        if (current.exerciseCompleted) return

        val matchingMoves = current.legalDestinations.filter { it.to == square }
        if (current.selectedSquare != null && matchingMoves.isNotEmpty()) {
            val chosen = matchingMoves.firstOrNull {
                it.toUci().equals(current.currentLesson.targetMoveUci, ignoreCase = true)
            } ?: matchingMoves.first()

            if (chosen.toUci().equals(current.currentLesson.targetMoveUci, ignoreCase = true)) {
                val nextBoard = ChessRulesEngine.applyLegalMove(current.lessonBoardState, chosen)
                ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.PUZZLE_SUCCESS, settings.value.soundEnabled)
                markLessonCompleted(current.currentLesson.id)
                _learnState.update {
                    it.copy(
                        lessonBoardState = nextBoard,
                        selectedSquare = null,
                        legalDestinations = emptyList(),
                        exerciseCompleted = true,
                        exerciseFeedback = current.currentLesson.successExplanation
                    )
                }
            } else {
                ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.ERROR, settings.value.soundEnabled)
                _learnState.update {
                    it.copy(
                        selectedSquare = null,
                        legalDestinations = emptyList(),
                        exerciseFeedback = "Try again! ${current.currentLesson.exercisePrompt}"
                    )
                }
            }
            return
        }

        val piece = current.lessonBoardState.pieceAt(square)
        if (piece != null && piece.color == current.lessonBoardState.activeColor) {
            val legal = ChessRulesEngine.generateLegalMovesForSquare(current.lessonBoardState, square)
            _learnState.update {
                it.copy(selectedSquare = square, legalDestinations = legal)
            }
        } else {
            _learnState.update {
                it.copy(selectedSquare = null, legalDestinations = emptyList())
            }
        }
    }

    fun resetLessonExercise() {
        selectLesson(_learnState.value.currentLesson)
    }

    private fun markLessonCompleted(lessonId: Int) {
        viewModelScope.launch {
            repository.updateSettings { current ->
                val existing = current.completedLessonIdsCsv
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toMutableSet()
                existing.add(lessonId)
                current.copy(completedLessonIdsCsv = existing.sorted().joinToString(","))
            }
        }
    }

    fun selectOpeningLine(opening: ChessOpeningLine) {
        _learnState.update {
            it.copy(
                currentOpening = opening,
                openingBoardState = ChessRulesEngine.createInitialState(),
                openingStepIndex = 0
            )
        }
    }

    fun stepOpeningForward() {
        val current = _learnState.value
        val step = current.currentOpening.steps.getOrNull(current.openingStepIndex) ?: return
        val move = ChessRulesEngine.parseUciMove(current.openingBoardState, step.moveUci) ?: return
        val nextBoard = ChessRulesEngine.applyLegalMove(current.openingBoardState, move)
        ChessSoundPlayer.play(ChessSoundPlayer.SoundEffect.MOVE, settings.value.soundEnabled)
        _learnState.update {
            it.copy(
                openingBoardState = nextBoard,
                openingStepIndex = current.openingStepIndex + 1
            )
        }
    }

    fun stepOpeningBackward() {
        val current = _learnState.value
        if (current.openingStepIndex <= 0) return
        val targetSteps = current.openingStepIndex - 1
        var board = ChessRulesEngine.createInitialState()
        for (i in 0 until targetSteps) {
            val step = current.currentOpening.steps[i]
            val move = ChessRulesEngine.parseUciMove(board, step.moveUci) ?: break
            board = ChessRulesEngine.applyLegalMove(board, move)
        }
        _learnState.update {
            it.copy(
                openingBoardState = board,
                openingStepIndex = targetSteps
            )
        }
    }

    fun resetOpeningExplorer() {
        selectOpeningLine(_learnState.value.currentOpening)
    }

    // --- Settings & Stats Actions ---
    fun toggleDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(isDarkTheme = isDark) }
        }
    }

    fun updateBoardTheme(themeId: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(boardThemeId = themeId) }
        }
    }

    fun updatePieceStyle(styleId: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(pieceStyleId = styleId) }
        }
    }

    fun toggleShowLegalMoves(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(showLegalMoveHighlights = enabled) }
        }
    }

    fun toggleShowEvaluationBar(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(showEvaluationBar = enabled) }
        }
    }

    fun toggleAutoFlipTwoPlayer(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(autoFlipTwoPlayer = enabled) }
        }
    }

    fun toggleSoundEffects(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(soundEnabled = enabled) }
        }
    }

    fun updateDefaultDifficulty(level: Int) {
        val coerced = level.coerceIn(1, 10)
        _computerSetupState.update { it.copy(difficultyLevel = coerced) }
        viewModelScope.launch {
            repository.updateSettings { it.copy(defaultDifficulty = coerced) }
        }
    }

    fun updateDefaultClock(minutes: Int, incrementSeconds: Int) {
        _computerSetupState.update {
            it.copy(clockMinutes = minutes, incrementSeconds = incrementSeconds)
        }
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(defaultClockMinutes = minutes, defaultIncrementSeconds = incrementSeconds)
            }
        }
    }

    fun updateAnalysisDepth(depth: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(analysisDepth = depth.coerceIn(2, 5)) }
        }
    }

    fun resetAllStatistics() {
        viewModelScope.launch {
            repository.resetAllStatistics()
        }
    }

    fun dismissStatusBanner() {
        _matchState.update { it.copy(statusBannerMessage = null) }
    }

    companion object {
        fun provideFactory(repository: ChessRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChessMasterViewModel(repository) as T
                }
            }
    }
}
