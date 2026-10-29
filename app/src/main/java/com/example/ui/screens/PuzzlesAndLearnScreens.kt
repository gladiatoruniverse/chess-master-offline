package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AppSettingsEntity
import com.example.data.ChessLesson
import com.example.data.ChessOpeningLine
import com.example.data.ChessPuzzle
import com.example.data.OfflineLessonsRepository
import com.example.data.OfflinePuzzlesRepository
import com.example.data.PuzzleCategory
import com.example.data.PuzzleProgressEntity
import com.example.domain.PieceColor
import com.example.domain.PieceType
import com.example.domain.Square
import com.example.ui.components.InteractiveChessBoardWithEval
import com.example.ui.components.PromotionPickerDialog
import com.example.ui.theme.BoardThemePalette
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrimsonCheckAlert
import com.example.ui.theme.DeepSlateTeal
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PieceVisualStyle
import com.example.viewmodel.LearnTab
import com.example.viewmodel.LearnUiState
import com.example.viewmodel.PuzzleUiState

@Composable
fun PuzzlesScreen(
    puzzleState: PuzzleUiState,
    puzzleProgress: List<PuzzleProgressEntity>,
    settings: AppSettingsEntity,
    onSelectCategory: (PuzzleCategory) -> Unit,
    onSelectPuzzle: (ChessPuzzle) -> Unit,
    onOpenDailyPuzzle: () -> Unit,
    onSquareClicked: (Square) -> Unit,
    onPromotionSelected: (PieceType) -> Unit,
    onRequestHint: () -> Unit,
    onRevealSolution: () -> Unit,
    onRetryPuzzle: () -> Unit,
    onNextPuzzle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val boardTheme = BoardThemePalette.fromId(settings.boardThemeId)
    val pieceStyle = PieceVisualStyle.fromId(settings.pieceStyleId)
    val solvedSet = puzzleProgress.filter { it.isSolved }.map { it.puzzleId }.toSet()
    val totalAttempts = puzzleProgress.sumOf { it.attempts }
    val totalSuccesses = puzzleProgress.sumOf { it.successes }
    val accuracyPercent = if (totalAttempts > 0) ((totalSuccesses * 100.0) / totalAttempts).toInt() else 100
    val categoryPuzzles = OfflinePuzzlesRepository.getPuzzlesByCategory(puzzleState.selectedCategory)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .testTag("puzzles_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + Accuracy
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("puzzles_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button)
                    )
                }
                Column {
                    Text(
                        text = if (puzzleState.isDailyPuzzleMode) "Daily Chess Puzzle" else "Tactical Chess Puzzles",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Solved: ${solvedSet.size}/${OfflinePuzzlesRepository.ALL_PUZZLES.size} • Accuracy: $accuracyPercent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenDailyPuzzle,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("btn_daily_puzzle")
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Daily", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Category Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (cat in PuzzleCategory.entries) {
                FilterChip(
                    selected = puzzleState.selectedCategory == cat,
                    onClick = { onSelectCategory(cat) },
                    label = { Text(cat.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChampionshipGold,
                        selectedLabelColor = DeepSlateTeal
                    ),
                    modifier = Modifier.testTag("puzzle_cat_${cat.name.lowercase()}")
                )
            }
        }

        // Puzzle Selector Strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categoryPuzzles, key = { it.id }) { p ->
                val isCurrent = p.id == puzzleState.currentPuzzle.id
                val isDone = solvedSet.contains(p.id)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCurrent) ChampionshipGold else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (isDone) EmeraldSuccess else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectPuzzle(p) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("puzzle_item_${p.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${p.id} ${p.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isCurrent) DeepSlateTeal else MaterialTheme.colorScheme.onSurface
                        )
                        if (isDone) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Solved",
                                tint = if (isCurrent) DeepSlateTeal else EmeraldSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Current Puzzle Info Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${puzzleState.currentPuzzle.id}: ${puzzleState.currentPuzzle.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ChampionshipGold
                    )
                    Text(
                        text = "${puzzleState.currentPuzzle.category.displayName} • ${puzzleState.currentPuzzle.difficulty}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val bannerColor = when {
                    puzzleState.isSolved -> EmeraldSuccess
                    puzzleState.isFailedAttempt -> CrimsonCheckAlert
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Text(
                    text = puzzleState.feedbackBanner,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (puzzleState.isSolved) FontWeight.Bold else FontWeight.Normal,
                    color = bannerColor,
                    modifier = Modifier.testTag("puzzle_feedback_banner")
                )
            }
        }

        // Interactive Puzzle Board
        InteractiveChessBoardWithEval(
            boardState = puzzleState.boardState,
            selectedSquare = puzzleState.selectedSquare,
            legalMovesForSelected = puzzleState.legalDestinations,
            isFlipped = ChessRulesEngineColorHelper.isBlackToMoveInitially(puzzleState.currentPuzzle.initialFen),
            showLegalMoveHighlights = settings.showLegalMoveHighlights,
            showEvalBar = false,
            boardTheme = boardTheme,
            pieceStyle = pieceStyle,
            onSquareClicked = onSquareClicked
        )

        // Puzzle Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onRequestHint,
                enabled = !puzzleState.isSolved && !puzzleState.solutionRevealed,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_puzzle_hint")
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hint")
            }

            FilledTonalButton(
                onClick = onRetryPuzzle,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_puzzle_retry")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry")
            }

            FilledTonalButton(
                onClick = onRevealSolution,
                enabled = !puzzleState.isSolved && !puzzleState.solutionRevealed,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_puzzle_solution")
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Solution")
            }

            Button(
                onClick = onNextPuzzle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampionshipGold,
                    contentColor = DeepSlateTeal
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_puzzle_next")
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (puzzleState.pendingPromotionFromTo != null) {
        PromotionPickerDialog(
            color = puzzleState.boardState.activeColor,
            pieceStyle = pieceStyle,
            onPieceChosen = onPromotionSelected,
            onDismiss = {}
        )
    }
}

private object ChessRulesEngineColorHelper {
    fun isBlackToMoveInitially(fen: String): Boolean {
        val parts = fen.trim().split(Regex("\\s+"))
        return parts.getOrNull(1) == "b"
    }
}

@Composable
fun LearnChessScreen(
    learnState: LearnUiState,
    settings: AppSettingsEntity,
    onSelectTab: (LearnTab) -> Unit,
    onSelectLesson: (ChessLesson) -> Unit,
    onLessonSquareClicked: (Square) -> Unit,
    onResetLesson: () -> Unit,
    onSelectOpening: (ChessOpeningLine) -> Unit,
    onStepOpeningForward: () -> Unit,
    onStepOpeningBackward: () -> Unit,
    onResetOpening: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val boardTheme = BoardThemePalette.fromId(settings.boardThemeId)
    val pieceStyle = PieceVisualStyle.fromId(settings.pieceStyleId)
    val completedLessonIds = settings.completedLessonIdsCsv
        .split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .toSet()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .testTag("learn_chess_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("learn_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_button)
                )
            }
            Column {
                Text(
                    text = "Learn Chess & Openings",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Completed ${completedLessonIds.size}/${OfflineLessonsRepository.ALL_LESSONS.size} Interactive Lessons",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tabs: 10 Lessons vs Opening Explorer
        TabRow(
            selectedTabIndex = learnState.selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = ChampionshipGold
        ) {
            Tab(
                selected = learnState.selectedTab == LearnTab.LESSONS,
                onClick = { onSelectTab(LearnTab.LESSONS) },
                text = { Text("Interactive Lessons (10)") },
                modifier = Modifier.testTag("tab_lessons")
            )
            Tab(
                selected = learnState.selectedTab == LearnTab.OPENING_EXPLORER,
                onClick = { onSelectTab(LearnTab.OPENING_EXPLORER) },
                text = { Text("Opening Explorer (6)") },
                modifier = Modifier.testTag("tab_opening_explorer")
            )
        }

        if (learnState.selectedTab == LearnTab.LESSONS) {
            // Lesson Selector Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(OfflineLessonsRepository.ALL_LESSONS, key = { it.id }) { lesson ->
                    val isSelected = lesson.id == learnState.currentLesson.id
                    val isDone = completedLessonIds.contains(lesson.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectLesson(lesson) },
                        label = {
                            Text(
                                text = if (isDone) "✓ ${lesson.title}" else lesson.title
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampionshipGold,
                            selectedLabelColor = DeepSlateTeal
                        ),
                        modifier = Modifier.testTag("lesson_chip_${lesson.id}")
                    )
                }
            }

            // Lesson Theory Card
            val lesson = learnState.currentLesson
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = ChampionshipGold
                    )
                    Text(
                        text = lesson.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    for (pt in lesson.summaryPoints) {
                        Text(
                            text = "• $pt",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lesson.detailedText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Interactive Exercise Prompt Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (learnState.exerciseCompleted) {
                    EmeraldSuccess.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (learnState.exerciseCompleted) EmeraldSuccess else ChampionshipGold,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("lesson_exercise_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = learnState.exerciseFeedback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (learnState.exerciseCompleted) EmeraldSuccess else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onResetLesson) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Exercise")
                    }
                }
            }

            // Lesson Practice Board
            InteractiveChessBoardWithEval(
                boardState = learnState.lessonBoardState,
                selectedSquare = learnState.selectedSquare,
                legalMovesForSelected = learnState.legalDestinations,
                showLegalMoveHighlights = true,
                showEvalBar = false,
                boardTheme = boardTheme,
                pieceStyle = pieceStyle,
                onSquareClicked = onLessonSquareClicked
            )
        } else {
            // Opening Explorer Content
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(OfflineLessonsRepository.OPENING_EXPLORER_LINES, key = { it.id }) { opening ->
                    val isSelected = opening.id == learnState.currentOpening.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectOpening(opening) },
                        label = { Text("${opening.ecoCode} • ${opening.name}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampionshipGold,
                            selectedLabelColor = DeepSlateTeal
                        ),
                        modifier = Modifier.testTag("opening_chip_${opening.id}")
                    )
                }
            }

            val opening = learnState.currentOpening
            val currentStep = if (learnState.openingStepIndex > 0) {
                opening.steps.getOrNull(learnState.openingStepIndex - 1)
            } else {
                null
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${opening.name} (${opening.ecoCode})",
                        style = MaterialTheme.typography.titleLarge,
                        color = ChampionshipGold
                    )
                    Text(
                        text = opening.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentStep?.let {
                            "Step ${learnState.openingStepIndex}/${opening.steps.size} (${it.san}): ${it.commentary}"
                        } ?: "Tap 'Next Move' below to step through the book moves of ${opening.name}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            InteractiveChessBoardWithEval(
                boardState = learnState.openingBoardState,
                selectedSquare = null,
                legalMovesForSelected = emptyList(),
                showLegalMoveHighlights = false,
                showEvalBar = false,
                boardTheme = boardTheme,
                pieceStyle = pieceStyle,
                onSquareClicked = {}
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onResetOpening,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_opening_reset")
                ) {
                    Text("Reset")
                }
                FilledTonalButton(
                    onClick = onStepOpeningBackward,
                    enabled = learnState.openingStepIndex > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_opening_prev")
                ) {
                    Text("← Prev")
                }
                Button(
                    onClick = onStepOpeningForward,
                    enabled = learnState.openingStepIndex < opening.steps.size,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampionshipGold,
                        contentColor = DeepSlateTeal
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_opening_next")
                ) {
                    Text("Next Move →")
                }
            }
        }
    }
}
