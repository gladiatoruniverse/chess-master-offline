package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AppSettingsEntity
import com.example.data.DifficultyStatsEntity
import com.example.data.PuzzleProgressEntity
import com.example.data.SavedGameEntity
import com.example.domain.ClockPresets
import com.example.domain.EngineDifficultyLevels
import com.example.domain.GameStatus
import com.example.ui.theme.BoardThemePalette
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrimsonCheckAlert
import com.example.ui.theme.DeepSlateTeal
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PieceVisualStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedGamesScreen(
    savedGames: List<SavedGameEntity>,
    onResumeOrViewGame: (SavedGameEntity) -> Unit,
    onRenameGame: (Long, String) -> Unit,
    onDeleteGame: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var gameToRename by remember { mutableStateOf<SavedGameEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var gameToDelete by remember { mutableStateOf<SavedGameEntity?>(null) }
    var gameForPgnInspect by remember { mutableStateOf<SavedGameEntity?>(null) }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("saved_games_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("saved_games_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button)
                    )
                }
                Column {
                    Text(
                        text = "Saved Games Archive",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Stored locally offline with full FEN, PGN, move history & clocks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (savedGames.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .testTag("empty_saved_games_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = ChampionshipGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Saved Games Yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Active matches are automatically saved after every move, or you can tap 'Save' during any game.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(savedGames, key = { it.id }) { game ->
                val isTerminal = runCatching { GameStatus.valueOf(game.status).isTerminal }.getOrDefault(false)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (game.isAutoSave) ChampionshipGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("saved_game_card_${game.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (game.isAutoSave) {
                                    Text(
                                        text = "AUTO-SAVED ACTIVE SLOT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ChampionshipGold
                                    )
                                }
                                Text(
                                    text = game.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${game.resultSummary} • ${dateFormatter.format(Date(game.updatedAt))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { onResumeOrViewGame(game) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChampionshipGold,
                                    contentColor = DeepSlateTeal
                                ),
                                modifier = Modifier.testTag("btn_open_saved_${game.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isTerminal) "Review" else "Resume")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { gameForPgnInspect = game },
                                modifier = Modifier.testTag("btn_pgn_saved_${game.id}")
                            ) {
                                Icon(Icons.Default.Description, contentDescription = "View PGN", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FEN / PGN")
                            }
                            TextButton(
                                onClick = {
                                    renameText = game.title
                                    gameToRename = game
                                },
                                modifier = Modifier.testTag("btn_rename_saved_${game.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rename")
                            }
                            TextButton(
                                onClick = { gameToDelete = game },
                                modifier = Modifier.testTag("btn_delete_saved_${game.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = CrimsonCheckAlert,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", color = CrimsonCheckAlert)
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    val renaming = gameToRename
    if (renaming != null) {
        AlertDialog(
            onDismissRequest = { gameToRename = null },
            title = { Text("Rename Saved Game") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Game Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_saved_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameGame(renaming.id, renameText)
                        gameToRename = null
                    },
                    modifier = Modifier.testTag("confirm_rename_saved_button")
                ) {
                    Text("Save Name")
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    val deleting = gameToDelete
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            title = { Text("Delete Saved Game?") },
            text = { Text("Permanently remove \"${deleting.title}\" from offline storage?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGame(deleting.id)
                        gameToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonCheckAlert),
                    modifier = Modifier.testTag("confirm_delete_saved_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // PGN / FEN Inspector Modal
    val inspecting = gameForPgnInspect
    if (inspecting != null) {
        AlertDialog(
            onDismissRequest = { gameForPgnInspect = null },
            title = { Text(inspecting.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FEN:", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = inspecting.currentFen,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text("PGN:", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = inspecting.pgnText,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { gameForPgnInspect = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsAndStatsScreen(
    settings: AppSettingsEntity,
    difficultyStats: List<DifficultyStatsEntity>,
    puzzleProgress: List<PuzzleProgressEntity>,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSelectBoardTheme: (String) -> Unit,
    onSelectPieceStyle: (String) -> Unit,
    onToggleShowLegalMoves: (Boolean) -> Unit,
    onToggleShowEvalBar: (Boolean) -> Unit,
    onToggleAutoFlipTwoPlayer: (Boolean) -> Unit,
    onToggleSoundEffects: (Boolean) -> Unit,
    onUpdateDefaultDifficulty: (Int) -> Unit,
    onUpdateDefaultClock: (Int, Int) -> Unit,
    onUpdateAnalysisDepth: (Int) -> Unit,
    onResetAllStatistics: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showResetStatsConfirm by remember { mutableStateOf(false) }

    val totalGames = difficultyStats.sumOf { it.gamesPlayed }
    val totalWins = difficultyStats.sumOf { it.wins }
    val totalDraws = difficultyStats.sumOf { it.draws }
    val totalLosses = difficultyStats.sumOf { it.losses }
    val winRate = if (totalGames > 0) ((totalWins * 100.0) / totalGames).toInt() else 0

    val puzzlesSolved = puzzleProgress.count { it.isSolved }
    val puzzleAttempts = puzzleProgress.sumOf { it.attempts }
    val puzzleSuccesses = puzzleProgress.sumOf { it.successes }
    val puzzleAccuracy = if (puzzleAttempts > 0) ((puzzleSuccesses * 100.0) / puzzleAttempts).toInt() else 100

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_stats_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button)
                    )
                }
                Column {
                    Text(
                        text = "Settings & Statistics",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Customize themes, board palettes, clocks & inspect offline records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1. Appearance & Board Theme Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Appearance & Board Themes",
                        style = MaterialTheme.typography.titleMedium,
                        color = ChampionshipGold
                    )

                    SettingsToggleRow(
                        title = "Dark Grandmaster Theme",
                        subtitle = "Switch between Dark Teal & Gold and Light Parchment",
                        checked = settings.isDarkTheme,
                        testTag = "switch_dark_theme",
                        onCheckedChange = onToggleDarkTheme
                    )

                    Text("Chessboard Palette:", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (palette in BoardThemePalette.entries) {
                            FilterChip(
                                selected = settings.boardThemeId == palette.id,
                                onClick = { onSelectBoardTheme(palette.id) },
                                label = { Text(palette.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChampionshipGold,
                                    selectedLabelColor = DeepSlateTeal
                                ),
                                modifier = Modifier.testTag("board_theme_${palette.id.lowercase()}")
                            )
                        }
                    }

                    Text("2D Chess Piece Set:", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (style in PieceVisualStyle.entries) {
                            FilterChip(
                                selected = settings.pieceStyleId == style.id,
                                onClick = { onSelectPieceStyle(style.id) },
                                label = { Text(style.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChampionshipGold,
                                    selectedLabelColor = DeepSlateTeal
                                ),
                                modifier = Modifier.testTag("piece_style_${style.id.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // 2. Gameplay & Audio Toggles
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Gameplay & Assistance",
                        style = MaterialTheme.typography.titleMedium,
                        color = ChampionshipGold
                    )
                    SettingsToggleRow(
                        title = "Highlight Legal Moves",
                        subtitle = "Show target dots and capture rings for the selected piece",
                        checked = settings.showLegalMoveHighlights,
                        testTag = "switch_legal_moves",
                        onCheckedChange = onToggleShowLegalMoves
                    )
                    SettingsToggleRow(
                        title = "Live Evaluation Bar",
                        subtitle = "Display real-time centipawn/mate advantage bar beside the board",
                        checked = settings.showEvaluationBar,
                        testTag = "switch_eval_bar",
                        onCheckedChange = onToggleShowEvalBar
                    )
                    SettingsToggleRow(
                        title = "Auto-Flip Board in Two-Player Mode",
                        subtitle = "Rotate board automatically after each move in Pass & Play",
                        checked = settings.autoFlipTwoPlayer,
                        testTag = "switch_auto_flip",
                        onCheckedChange = onToggleAutoFlipTwoPlayer
                    )
                    SettingsToggleRow(
                        title = "Offline Sound Effects",
                        subtitle = "Acoustic synthesis for moves, captures, and checks (off by default)",
                        checked = settings.soundEnabled,
                        testTag = "switch_sound_effects",
                        onCheckedChange = onToggleSoundEffects
                    )
                }
            }
        }

        // 3. Default Difficulty, Clock & Engine Analysis Strength
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Defaults & Analysis Strength",
                        style = MaterialTheme.typography.titleMedium,
                        color = ChampionshipGold
                    )
                    Text(
                        text = "Default Computer Difficulty: Level ${settings.defaultDifficulty}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (lvl in listOf(1, 3, 5, 7, 10)) {
                            FilterChip(
                                selected = settings.defaultDifficulty == lvl,
                                onClick = { onUpdateDefaultDifficulty(lvl) },
                                label = { Text("Lv.$lvl") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChampionshipGold,
                                    selectedLabelColor = DeepSlateTeal
                                )
                            )
                        }
                    }

                    Text(
                        text = "Default Clock Control: ${ClockPresets.formatLabel(settings.defaultClockMinutes, settings.defaultIncrementSeconds)}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (mins in ClockPresets.MINUTE_OPTIONS) {
                            FilterChip(
                                selected = settings.defaultClockMinutes == mins,
                                onClick = { onUpdateDefaultClock(mins, settings.defaultIncrementSeconds) },
                                label = { Text(if (mins == 0) "Untimed" else "${mins}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChampionshipGold,
                                    selectedLabelColor = DeepSlateTeal
                                )
                            )
                        }
                    }

                    Text(
                        text = "Hint & Position Analysis Depth: Depth ${settings.analysisDepth}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (d in 2..5) {
                            FilterChip(
                                selected = settings.analysisDepth == d,
                                onClick = { onUpdateAnalysisDepth(d) },
                                label = { Text("Depth $d") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChampionshipGold,
                                    selectedLabelColor = DeepSlateTeal
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Offline Statistics Dashboard
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .testTag("statistics_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Player Statistics (Local Only)",
                            style = MaterialTheme.typography.titleMedium,
                            color = ChampionshipGold
                        )
                        OutlinedButton(
                            onClick = { showResetStatsConfirm = true },
                            modifier = Modifier.testTag("btn_reset_statistics")
                        ) {
                            Text("Reset Stats", color = CrimsonCheckAlert)
                        }
                    }

                    Text(
                        text = "Matches Played: $totalGames • Wins: $totalWins • Draws: $totalDraws • Losses: $totalLosses • Win Rate: $winRate%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Puzzles Solved: $puzzlesSolved / 34 • Attempts: $puzzleAttempts • Accuracy: $puzzleAccuracy%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Breakdown by Engine Difficulty:", style = MaterialTheme.typography.labelLarge)
                    for (tier in EngineDifficultyLevels.ALL) {
                        val rowStat = difficultyStats.firstOrNull { it.difficultyLevel == tier.level }
                            ?: DifficultyStatsEntity(difficultyLevel = tier.level)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lv.${tier.level} ${tier.title}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${rowStat.wins}W / ${rowStat.draws}D / ${rowStat.losses}L (${rowStat.winRatePercent}%)",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Open-Source & Engine License Notice
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Open-Source & Offline Engine Notice",
                        style = MaterialTheme.typography.titleSmall,
                        color = ChampionshipGold
                    )
                    Text(
                        text = "Chess Master runs 100% offline in airplane mode with zero network permissions, zero ads, and zero cloud APIs. Includes a local UCI-protocol chess engine and FIDE rules validator written in open-source Kotlin under the GPLv3/MIT-compatible open license.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showResetStatsConfirm) {
        AlertDialog(
            onDismissRequest = { showResetStatsConfirm = false },
            title = { Text("Reset All Statistics?") },
            text = { Text("Clear all Win/Draw/Loss records and puzzle progress stored on this device?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetStatsConfirm = false
                        onResetAllStatistics()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonCheckAlert),
                    modifier = Modifier.testTag("confirm_reset_stats_button")
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetStatsConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
