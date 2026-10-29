package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChessDatabase
import com.example.data.ChessRepository
import com.example.data.OfflinePuzzlesRepository
import com.example.domain.LocalUciChessEngine
import com.example.ui.screens.ComputerSetupScreen
import com.example.ui.screens.GameBoardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnChessScreen
import com.example.ui.screens.PuzzlesScreen
import com.example.ui.screens.SavedGamesScreen
import com.example.ui.screens.SettingsAndStatsScreen
import com.example.ui.theme.ChessMasterTheme
import com.example.ui.theme.PieceVisualStyle
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ChessMasterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessMasterRoot()
        }
    }
}

@Composable
fun ChessMasterRoot() {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) {
        val db = ChessDatabase.getInstance(context)
        ChessRepository(db.chessDao())
    }
    val vm: ChessMasterViewModel = viewModel(
        factory = ChessMasterViewModel.provideFactory(repository)
    )

    val settings by vm.settings.collectAsStateWithLifecycle()
    val currentScreen by vm.currentScreen.collectAsStateWithLifecycle()
    val setupState by vm.computerSetupState.collectAsStateWithLifecycle()
    val matchState by vm.matchState.collectAsStateWithLifecycle()
    val puzzleState by vm.puzzleState.collectAsStateWithLifecycle()
    val learnState by vm.learnState.collectAsStateWithLifecycle()
    val savedGames by vm.savedGames.collectAsStateWithLifecycle()
    val difficultyStats by vm.difficultyStats.collectAsStateWithLifecycle()
    val puzzleProgress by vm.puzzleProgress.collectAsStateWithLifecycle()

    val staticEvaluator = remember { LocalUciChessEngine() }
    val pieceStyle = PieceVisualStyle.fromId(settings.pieceStyleId)

    ChessMasterTheme(darkTheme = settings.isDarkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Crossfade(
                targetState = currentScreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> {
                        val dailyPuzzle = remember { OfflinePuzzlesRepository.getDailyPuzzle() }
                        val latestSaved = savedGames.firstOrNull()
                        val totalGames = difficultyStats.sumOf { it.gamesPlayed }
                        val totalWins = difficultyStats.sumOf { it.wins }
                        val solvedCount = puzzleProgress.count { it.isSolved }

                        HomeScreen(
                            dailyPuzzle = dailyPuzzle,
                            latestSavedGame = latestSaved,
                            totalGamesPlayed = totalGames,
                            totalWins = totalWins,
                            puzzlesSolvedCount = solvedCount,
                            pieceStyle = pieceStyle,
                            onNavigate = { vm.navigateTo(it) },
                            onStartTwoPlayer = { vm.startTwoPlayerMatch() },
                            onOpenDailyPuzzle = { vm.openDailyPuzzle() },
                            onResumeGame = { vm.loadAndResumeSavedGame(it) }
                        )
                    }

                    AppScreen.COMPUTER_SETUP -> {
                        ComputerSetupScreen(
                            setupState = setupState,
                            difficultyStats = difficultyStats,
                            pieceStyle = pieceStyle,
                            onSelectColor = { vm.updateSetupColor(it) },
                            onSelectDifficulty = { vm.updateSetupDifficulty(it) },
                            onSelectClock = { mins, inc -> vm.updateSetupClock(mins, inc) },
                            onStartMatch = { vm.startComputerMatchFromSetup() },
                            onBack = { vm.navigateBack() }
                        )
                    }

                    AppScreen.GAME_BOARD -> {
                        val staticEvalCp = remember(matchState.boardState) {
                            staticEvaluator.evaluateStaticWhitePerspective(matchState.boardState)
                        }
                        GameBoardScreen(
                            matchState = matchState,
                            settings = settings,
                            staticEvalCp = staticEvalCp,
                            onSquareClicked = { vm.onBoardSquareClicked(it) },
                            onPromotionSelected = { vm.onPromotionPieceSelected(it) },
                            onDismissPromotion = { vm.dismissPromotionDialog() },
                            onUndo = { vm.undoLastMove() },
                            onRequestHint = { vm.requestHint() },
                            onAnalyzePosition = { vm.analyzeCurrentPosition() },
                            onCancelEngine = { vm.cancelEngineCalculation() },
                            onFlipBoard = { vm.toggleBoardFlip() },
                            onSaveGame = { title -> vm.saveMatchManually(title) },
                            onOfferDraw = { vm.offerOrClaimDraw() },
                            onResign = { vm.resignCurrentGame() },
                            onRestartMatch = { vm.restartCurrentMatch() },
                            onConfigureTwoPlayerClock = { mins, inc ->
                                vm.startTwoPlayerMatch(clockMinutes = mins, incrementSeconds = inc)
                            },
                            onBackToHome = { vm.navigateBack() }
                        )
                    }

                    AppScreen.PUZZLES -> {
                        PuzzlesScreen(
                            puzzleState = puzzleState,
                            puzzleProgress = puzzleProgress,
                            settings = settings,
                            onSelectCategory = { vm.selectPuzzleCategory(it) },
                            onSelectPuzzle = { vm.loadPuzzle(it) },
                            onOpenDailyPuzzle = { vm.openDailyPuzzle() },
                            onSquareClicked = { vm.onPuzzleSquareClicked(it) },
                            onPromotionSelected = { vm.onPuzzlePromotionSelected(it) },
                            onRequestHint = { vm.requestPuzzleHint() },
                            onRevealSolution = { vm.revealPuzzleSolution() },
                            onRetryPuzzle = { vm.retryCurrentPuzzle() },
                            onNextPuzzle = { vm.nextPuzzleInCategory() },
                            onBack = { vm.navigateBack() }
                        )
                    }

                    AppScreen.LEARN_CHESS -> {
                        LearnChessScreen(
                            learnState = learnState,
                            settings = settings,
                            onSelectTab = { vm.selectLearnTab(it) },
                            onSelectLesson = { vm.selectLesson(it) },
                            onLessonSquareClicked = { vm.onLessonBoardSquareClicked(it) },
                            onResetLesson = { vm.resetLessonExercise() },
                            onSelectOpening = { vm.selectOpeningLine(it) },
                            onStepOpeningForward = { vm.stepOpeningForward() },
                            onStepOpeningBackward = { vm.stepOpeningBackward() },
                            onResetOpening = { vm.resetOpeningExplorer() },
                            onBack = { vm.navigateBack() }
                        )
                    }

                    AppScreen.SAVED_GAMES -> {
                        SavedGamesScreen(
                            savedGames = savedGames,
                            onResumeOrViewGame = { vm.loadAndResumeSavedGame(it) },
                            onRenameGame = { id, name -> vm.renameSavedGame(id, name) },
                            onDeleteGame = { id -> vm.deleteSavedGame(id) },
                            onBack = { vm.navigateBack() }
                        )
                    }

                    AppScreen.SETTINGS_STATS -> {
                        SettingsAndStatsScreen(
                            settings = settings,
                            difficultyStats = difficultyStats,
                            puzzleProgress = puzzleProgress,
                            onToggleDarkTheme = { vm.toggleDarkTheme(it) },
                            onSelectBoardTheme = { vm.updateBoardTheme(it) },
                            onSelectPieceStyle = { vm.updatePieceStyle(it) },
                            onToggleShowLegalMoves = { vm.toggleShowLegalMoves(it) },
                            onToggleShowEvalBar = { vm.toggleShowEvaluationBar(it) },
                            onToggleAutoFlipTwoPlayer = { vm.toggleAutoFlipTwoPlayer(it) },
                            onToggleSoundEffects = { vm.toggleSoundEffects(it) },
                            onUpdateDefaultDifficulty = { vm.updateDefaultDifficulty(it) },
                            onUpdateDefaultClock = { mins, inc -> vm.updateDefaultClock(mins, inc) },
                            onUpdateAnalysisDepth = { vm.updateAnalysisDepth(it) },
                            onResetAllStatistics = { vm.resetAllStatistics() },
                            onBack = { vm.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
