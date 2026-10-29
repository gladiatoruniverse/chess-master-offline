package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AppSettingsEntity
import com.example.domain.ClockPresets
import com.example.domain.EngineDifficultyLevels
import com.example.domain.GameMode
import com.example.domain.GameStatus
import com.example.domain.Piece
import com.example.domain.PieceColor
import com.example.domain.PieceType
import com.example.domain.Square
import com.example.ui.components.ChessPieceIcon
import com.example.ui.components.InteractiveChessBoardWithEval
import com.example.ui.components.PromotionPickerDialog
import com.example.ui.theme.BoardThemePalette
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrimsonCheckAlert
import com.example.ui.theme.DeepSlateTeal
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PieceVisualStyle
import com.example.viewmodel.ActiveMatchUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameBoardScreen(
    matchState: ActiveMatchUiState,
    settings: AppSettingsEntity,
    staticEvalCp: Int,
    onSquareClicked: (Square) -> Unit,
    onPromotionSelected: (PieceType) -> Unit,
    onDismissPromotion: () -> Unit,
    onUndo: () -> Unit,
    onRequestHint: () -> Unit,
    onAnalyzePosition: () -> Unit,
    onCancelEngine: () -> Unit,
    onFlipBoard: () -> Unit,
    onSaveGame: (String) -> Unit,
    onOfferDraw: () -> Unit,
    onResign: () -> Unit,
    onRestartMatch: () -> Unit,
    onConfigureTwoPlayerClock: (Int, Int) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToHome() }

    var showRestartConfirm by remember { mutableStateOf(false) }
    var showResignConfirm by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showTwoPlayerClockDialog by remember { mutableStateOf(false) }
    var saveTitleInput by remember(matchState.title) { mutableStateOf(matchState.title) }

    val boardTheme = BoardThemePalette.fromId(settings.boardThemeId)
    val pieceStyle = PieceVisualStyle.fromId(settings.pieceStyleId)

    val topPlayerColor = if (matchState.isBoardFlipped) PieceColor.WHITE else PieceColor.BLACK
    val bottomPlayerColor = topPlayerColor.opposite()

    val evalCp = matchState.engineAnalysis?.scoreCentipawns ?: staticEvalCp
    val evalLabel = matchState.engineAnalysis?.formattedEvaluation()
        ?: if (evalCp >= 0) "+%.1f".format(evalCp / 100.0) else "%.1f".format(evalCp / 100.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("game_board_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button)
                    )
                }
                Column {
                    Text(
                        text = matchState.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val modeSub = if (matchState.gameMode == GameMode.VS_COMPUTER) {
                        val tier = EngineDifficultyLevels.getByLevel(matchState.difficultyLevel)
                        "Playing as ${matchState.humanColor.displayName} • Lv.${tier.level} ${tier.title}"
                    } else {
                        "Two Players on Device • ${ClockPresets.formatLabel(matchState.initialClockMinutes, matchState.incrementSeconds)}"
                    }
                    Text(
                        text = modeSub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (matchState.gameMode == GameMode.TWO_PLAYER) {
                OutlinedButton(
                    onClick = { showTwoPlayerClockDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("two_player_clock_button")
                ) {
                    Text(
                        text = ClockPresets.formatLabel(matchState.initialClockMinutes, matchState.incrementSeconds),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // 2. Top Player Banner (Clock + Captured Pieces)
        PlayerClockAndCapturesBar(
            playerColor = topPlayerColor,
            playerName = getPlayerLabel(matchState, topPlayerColor),
            isTurn = matchState.boardState.activeColor == topPlayerColor && !matchState.boardState.status.isTerminal,
            isTimed = matchState.isTimed,
            timeRemainingMs = if (topPlayerColor == PieceColor.WHITE) matchState.whiteTimeRemainingMs else matchState.blackTimeRemainingMs,
            capturedPieces = matchState.boardState.capturedPiecesBy(topPlayerColor),
            materialAdvantage = matchState.boardState.materialBalanceFor(topPlayerColor),
            pieceStyle = pieceStyle,
            testTagPrefix = "top_player"
        )

        // 3. Status / Check / Terminal Banner
        GameStatusAlertStrip(
            matchState = matchState,
            onCancelEngine = onCancelEngine,
            onStartRematch = onRestartMatch
        )

        // 4. Interactive 8x8 Chessboard + Live Evaluation Bar
        InteractiveChessBoardWithEval(
            boardState = matchState.boardState,
            selectedSquare = matchState.selectedSquare,
            legalMovesForSelected = matchState.legalDestinations,
            hintSquares = matchState.hintSquares,
            isFlipped = matchState.isBoardFlipped,
            showLegalMoveHighlights = settings.showLegalMoveHighlights,
            showEvalBar = settings.showEvaluationBar,
            evalScoreCentipawns = evalCp,
            evalFormattedLabel = evalLabel,
            boardTheme = boardTheme,
            pieceStyle = pieceStyle,
            onSquareClicked = onSquareClicked
        )

        // 5. Bottom Player Banner (Clock + Captured Pieces)
        PlayerClockAndCapturesBar(
            playerColor = bottomPlayerColor,
            playerName = getPlayerLabel(matchState, bottomPlayerColor),
            isTurn = matchState.boardState.activeColor == bottomPlayerColor && !matchState.boardState.status.isTerminal,
            isTimed = matchState.isTimed,
            timeRemainingMs = if (bottomPlayerColor == PieceColor.WHITE) matchState.whiteTimeRemainingMs else matchState.blackTimeRemainingMs,
            capturedPieces = matchState.boardState.capturedPiecesBy(bottomPlayerColor),
            materialAdvantage = matchState.boardState.materialBalanceFor(bottomPlayerColor),
            pieceStyle = pieceStyle,
            testTagPrefix = "bottom_player"
        )

        // 6. Standard Algebraic Notation (SAN) Move History Strip
        MoveHistorySanStrip(moves = matchState.boardState.moveHistory.map { it.san })

        // 7. Engine Hint & Position Analysis Card (when requested or available)
        if (matchState.isAnalyzingPosition || matchState.engineAnalysis != null || matchState.engineErrorMessage != null) {
            EngineAnalysisPanel(
                matchState = matchState,
                onCancel = onCancelEngine
            )
        }

        // 8. Primary Game Control Bar
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameControlChipButton(
                label = "Undo",
                icon = Icons.AutoMirrored.Filled.Undo,
                enabled = matchState.undoSnapshots.isNotEmpty() && !matchState.isEngineThinking,
                testTag = "btn_undo_move",
                onClick = onUndo
            )
            GameControlChipButton(
                label = "Hint",
                icon = Icons.Default.Lightbulb,
                enabled = !matchState.boardState.status.isTerminal && !matchState.isEngineThinking,
                testTag = "btn_hint",
                onClick = onRequestHint
            )
            GameControlChipButton(
                label = "Analyze",
                icon = Icons.Default.Analytics,
                enabled = !matchState.boardState.status.isTerminal && !matchState.isEngineThinking,
                testTag = "btn_analyze",
                onClick = onAnalyzePosition
            )
            GameControlChipButton(
                label = "Flip",
                icon = Icons.Default.SwapVert,
                enabled = true,
                testTag = "btn_flip_board",
                onClick = onFlipBoard
            )
            GameControlChipButton(
                label = "Save",
                icon = Icons.Default.BookmarkAdd,
                enabled = true,
                testTag = "btn_save_game",
                onClick = {
                    saveTitleInput = matchState.title
                    showSaveDialog = true
                }
            )
            GameControlChipButton(
                label = "Draw",
                icon = Icons.Default.Handshake,
                enabled = !matchState.boardState.status.isTerminal,
                testTag = "btn_offer_draw",
                onClick = onOfferDraw
            )
            GameControlChipButton(
                label = "Resign",
                icon = Icons.Default.Flag,
                enabled = !matchState.boardState.status.isTerminal,
                testTag = "btn_resign",
                onClick = { showResignConfirm = true }
            )
            GameControlChipButton(
                label = "Restart",
                icon = Icons.Default.Refresh,
                enabled = true,
                testTag = "btn_restart_game",
                onClick = { showRestartConfirm = true }
            )
        }
    }

    // Pawn Promotion Picker Modal
    if (matchState.pendingPromotionFromTo != null) {
        PromotionPickerDialog(
            color = matchState.boardState.activeColor,
            pieceStyle = pieceStyle,
            onPieceChosen = onPromotionSelected,
            onDismiss = onDismissPromotion
        )
    }

    // Restart Confirmation Dialog
    if (showRestartConfirm) {
        AlertDialog(
            onDismissRequest = { showRestartConfirm = false },
            title = { Text("Restart Match?") },
            text = { Text("Start a fresh board with the current settings? Your current progress will be reset.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestartConfirm = false
                        onRestartMatch()
                    },
                    modifier = Modifier.testTag("confirm_restart_button")
                ) {
                    Text("Restart")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Resign Confirmation Dialog
    if (showResignConfirm) {
        AlertDialog(
            onDismissRequest = { showResignConfirm = false },
            title = { Text("Resign Match?") },
            text = { Text("Are you sure you want to resign this game?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResignConfirm = false
                        onResign()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonCheckAlert),
                    modifier = Modifier.testTag("confirm_resign_button")
                ) {
                    Text("Resign")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResignConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Manual Save Game Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Chess Game") },
            text = {
                Column {
                    Text(
                        text = "Save current board position, move history, PGN, and clock timers locally:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = saveTitleInput,
                        onValueChange = { saveTitleInput = it },
                        label = { Text("Match Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_game_title_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        onSaveGame(saveTitleInput)
                    },
                    modifier = Modifier.testTag("confirm_save_game_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Two-Player Clock Selection Dialog
    if (showTwoPlayerClockDialog) {
        AlertDialog(
            onDismissRequest = { showTwoPlayerClockDialog = false },
            title = { Text("Two-Player Clock Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a time control to start a new Two-Player game:")
                    for (mins in ClockPresets.MINUTE_OPTIONS) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showTwoPlayerClockDialog = false
                                    onConfigureTwoPlayerClock(mins, 0)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (mins == 0) "Untimed" else "$mins min")
                            }
                            if (mins > 0) {
                                OutlinedButton(
                                    onClick = {
                                        showTwoPlayerClockDialog = false
                                        onConfigureTwoPlayerClock(mins, 3)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${mins}m + 3s")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTwoPlayerClockDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun getPlayerLabel(matchState: ActiveMatchUiState, color: PieceColor): String {
    return if (matchState.gameMode == GameMode.VS_COMPUTER) {
        if (color == matchState.humanColor) {
            "You (${color.displayName})"
        } else {
            val tier = EngineDifficultyLevels.getByLevel(matchState.difficultyLevel)
            "Stockfish Lv.${tier.level} (${color.displayName})"
        }
    } else {
        "Player ${color.displayName}"
    }
}

@Composable
private fun PlayerClockAndCapturesBar(
    playerColor: PieceColor,
    playerName: String,
    isTurn: Boolean,
    isTimed: Boolean,
    timeRemainingMs: Long,
    capturedPieces: List<PieceType>,
    materialAdvantage: Int,
    pieceStyle: PieceVisualStyle,
    testTagPrefix: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isTurn) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isTurn) 1.5.dp else 1.dp,
                color = if (isTurn) ChampionshipGold else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("${testTagPrefix}_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChessPieceIcon(
                        piece = Piece(PieceType.KING, playerColor),
                        pieceStyle = pieceStyle,
                        size = 20.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = playerName,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isTurn) ChampionshipGold else MaterialTheme.colorScheme.onSurface
                    )
                    if (materialAdvantage > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "+$materialAdvantage",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                if (capturedPieces.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val capturedColor = playerColor.opposite()
                        for (type in capturedPieces) {
                            ChessPieceIcon(
                                piece = Piece(type, capturedColor),
                                pieceStyle = pieceStyle,
                                size = 15.dp
                            )
                        }
                    }
                }
            }

            // Digital Monotonic Chess Clock
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isTurn) ChampionshipGold else DeepSlateTeal.copy(alpha = 0.7f),
                modifier = Modifier.testTag("${testTagPrefix}_clock")
            ) {
                Text(
                    text = if (isTimed) formatClockMillis(timeRemainingMs) else "∞",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isTurn) DeepSlateTeal else Color(0xFFF5F2EB),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

fun formatClockMillis(ms: Long): String {
    val safeMs = ms.coerceAtLeast(0L)
    val totalSeconds = safeMs / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (minutes == 0L && safeMs < 10_000L) {
        val tenths = (safeMs % 1000L) / 100L
        "%d:%02d.%d".format(minutes, seconds, tenths)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun GameStatusAlertStrip(
    matchState: ActiveMatchUiState,
    onCancelEngine: () -> Unit,
    onStartRematch: () -> Unit
) {
    val board = matchState.boardState
    when {
        board.status.isTerminal -> {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ChampionshipGold.copy(alpha = 0.18f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ChampionshipGold, RoundedCornerShape(12.dp))
                    .testTag("terminal_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = board.status.label.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = ChampionshipGold
                        )
                        val detail = when {
                            board.winner != null -> "${board.winner.displayName} wins the game!"
                            else -> "The game ended in a draw (1/2 - 1/2)."
                        }
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(
                        onClick = onStartRematch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampionshipGold,
                            contentColor = DeepSlateTeal
                        ),
                        modifier = Modifier.testTag("new_game_after_terminal_button")
                    ) {
                        Text("New Game")
                    }
                }
            }
        }
        matchState.isEngineThinking -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("engine_thinking_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ChampionshipGold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Stockfish Local UCI Engine calculating move...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = onCancelEngine) {
                        Text("Cancel", color = ChampionshipGold)
                    }
                }
            }
        }
        board.status == GameStatus.CHECK -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CrimsonCheckAlert.copy(alpha = 0.22f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CrimsonCheckAlert, RoundedCornerShape(10.dp))
                    .testTag("check_warning_banner")
            ) {
                Text(
                    text = "CHECK! ${board.activeColor.displayName}'s King is under attack.",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrimsonCheckAlert,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        matchState.statusBannerMessage != null -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = matchState.statusBannerMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = ChampionshipGold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MoveHistorySanStrip(moves: List<String>) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .testTag("move_history_strip")
    ) {
        if (moves.isEmpty()) {
            Text(
                text = "Move History (SAN): Make a move on the board to begin.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        } else {
            val pairedMoves = moves.chunked(2)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(pairedMoves) { idx, pair ->
                    val whiteMove = pair.getOrElse(0) { "" }
                    val blackMove = pair.getOrElse(1) { "" }
                    Text(
                        text = "${idx + 1}. $whiteMove ${if (blackMove.isNotEmpty()) blackMove else ""}".trim(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineAnalysisPanel(
    matchState: ActiveMatchUiState,
    onCancel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ChampionshipGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .testTag("engine_analysis_panel")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (matchState.isAnalyzingPosition) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ChampionshipGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing position with local UCI engine...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            } else if (matchState.engineErrorMessage != null) {
                Text(
                    text = matchState.engineErrorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonCheckAlert
                )
            } else if (matchState.engineAnalysis != null) {
                val analysis = matchState.engineAnalysis
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENGINE HINT & ANALYSIS • Best: ${analysis.bestMoveSan}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ChampionshipGold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DeepSlateTeal
                    ) {
                        Text(
                            text = "Eval ${analysis.formattedEvaluation()} (d=${analysis.depthReached})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChampionshipGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = analysis.humanExplanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (analysis.principalVariationSan.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PV Line: ${analysis.principalVariationSan.joinToString(" → ")} (${analysis.nodesSearched} nodes)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GameControlChipButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier
            .height(48.dp)
            .testTag(testTag)
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}
