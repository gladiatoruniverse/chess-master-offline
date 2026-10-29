package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ChessPuzzle
import com.example.data.DifficultyStatsEntity
import com.example.data.SavedGameEntity
import com.example.domain.ClockPresets
import com.example.domain.EngineDifficultyLevels
import com.example.domain.Piece
import com.example.domain.PieceColor
import com.example.domain.PieceType
import com.example.domain.PlayerColorChoice
import com.example.ui.components.ChessPieceIcon
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.DeepSlateTeal
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PieceVisualStyle
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ComputerSetupUiState

@Composable
fun HomeScreen(
    dailyPuzzle: ChessPuzzle,
    latestSavedGame: SavedGameEntity?,
    totalGamesPlayed: Int,
    totalWins: Int,
    puzzlesSolvedCount: Int,
    pieceStyle: PieceVisualStyle,
    onNavigate: (AppScreen) -> Unit,
    onStartTwoPlayer: () -> Unit,
    onOpenDailyPuzzle: () -> Unit,
    onResumeGame: (SavedGameEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Banner with Grandmaster Emblem
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ChampionshipGold.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(186.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_chess_hero_banner_1793287284250),
                        contentDescription = stringResource(R.string.cd_hero_banner),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x550B1D21),
                                        Color(0xE60B1D21)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = ChampionshipGold.copy(alpha = 0.22f),
                                modifier = Modifier
                                    .size(44.dp)
                                    .border(1.dp, ChampionshipGold, CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    ChessPieceIcon(
                                        piece = Piece(PieceType.KNIGHT, PieceColor.WHITE),
                                        pieceStyle = pieceStyle,
                                        size = 30.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "CHESS MASTER",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color(0xFFF5F2EB)
                                )
                                Text(
                                    text = "100% Offline Grandmaster Chess Studio",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChampionshipGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatPillBadge(label = "Matches", value = "$totalGamesPlayed")
                            StatPillBadge(label = "Victories", value = "$totalWins")
                            StatPillBadge(label = "Puzzles Solved", value = "$puzzlesSolvedCount / 34")
                        }
                    }
                }
            }
        }

        // 2. Quick Resume Unfinished Match Banner
        if (latestSavedGame != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ChampionshipGold.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .clickable { onResumeGame(latestSavedGame) }
                        .testTag("quick_resume_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RESUME ACTIVE MATCH",
                                style = MaterialTheme.typography.labelSmall,
                                color = ChampionshipGold
                            )
                            Text(
                                text = latestSavedGame.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = latestSavedGame.resultSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onResumeGame(latestSavedGame) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampionshipGold,
                                contentColor = DeepSlateTeal
                            ),
                            modifier = Modifier.testTag("resume_match_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                    }
                }
            }
        }

        // 3. Primary Navigation Grid (6 Modes)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeModeCard(
                        title = "Play vs Computer",
                        subtitle = "10 Stockfish UCI Tiers",
                        icon = Icons.Default.SmartToy,
                        accentColor = ChampionshipGold,
                        testTag = "nav_play_computer",
                        onClick = { onNavigate(AppScreen.COMPUTER_SETUP) },
                        modifier = Modifier.weight(1f)
                    )
                    HomeModeCard(
                        title = "Two Players",
                        subtitle = "Pass & Play + Clocks",
                        icon = Icons.Default.Group,
                        accentColor = EmeraldSuccess,
                        testTag = "nav_two_players",
                        onClick = onStartTwoPlayer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeModeCard(
                        title = "Chess Puzzles",
                        subtitle = "34 Tactical Challenges",
                        icon = Icons.Default.Extension,
                        accentColor = Color(0xFFE5A93C),
                        testTag = "nav_puzzles",
                        onClick = { onNavigate(AppScreen.PUZZLES) },
                        modifier = Modifier.weight(1f)
                    )
                    HomeModeCard(
                        title = "Learn Chess",
                        subtitle = "10 Lessons & Openings",
                        icon = Icons.Default.School,
                        accentColor = Color(0xFF5AB1BB),
                        testTag = "nav_learn_chess",
                        onClick = { onNavigate(AppScreen.LEARN_CHESS) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeModeCard(
                        title = "Saved Games",
                        subtitle = "Archives, FEN & PGN",
                        icon = Icons.Default.Bookmark,
                        accentColor = Color(0xFFC88A4B),
                        testTag = "nav_saved_games",
                        onClick = { onNavigate(AppScreen.SAVED_GAMES) },
                        modifier = Modifier.weight(1f)
                    )
                    HomeModeCard(
                        title = "Settings & Stats",
                        subtitle = "Themes, Clocks & W/D/L",
                        icon = Icons.Default.Settings,
                        accentColor = Color(0xFF9FB3B8),
                        testTag = "nav_settings",
                        onClick = { onNavigate(AppScreen.SETTINGS_STATS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Daily Puzzle Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .clickable { onOpenDailyPuzzle() }
                    .testTag("daily_puzzle_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ChampionshipGold.copy(alpha = 0.18f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Daily Puzzle",
                                    tint = ChampionshipGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DAILY OFFLINE PUZZLE",
                                style = MaterialTheme.typography.labelSmall,
                                color = ChampionshipGold
                            )
                            Text(
                                text = "${dailyPuzzle.title} (${dailyPuzzle.category.displayName})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Difficulty: ${dailyPuzzle.difficulty} • Tap to solve",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Solve →",
                        style = MaterialTheme.typography.labelLarge,
                        color = ChampionshipGold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPillBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0x99132A30),
        modifier = Modifier.border(0.8.dp, ChampionshipGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9FB3B8)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ChampionshipGold
            )
        }
    }
}

@Composable
private fun HomeModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.16f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComputerSetupScreen(
    setupState: ComputerSetupUiState,
    difficultyStats: List<DifficultyStatsEntity>,
    pieceStyle: PieceVisualStyle,
    onSelectColor: (PlayerColorChoice) -> Unit,
    onSelectDifficulty: (Int) -> Unit,
    onSelectClock: (Int, Int) -> Unit,
    onStartMatch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val currentTier = EngineDifficultyLevels.getByLevel(setupState.difficultyLevel)
    val statsForTier = difficultyStats.firstOrNull { it.difficultyLevel == setupState.difficultyLevel }
        ?: DifficultyStatsEntity(difficultyLevel = setupState.difficultyLevel)
    val totalPlayed = difficultyStats.sumOf { it.gamesPlayed }
    val totalWins = difficultyStats.sumOf { it.wins }
    val totalDraws = difficultyStats.sumOf { it.draws }
    val totalLosses = difficultyStats.sumOf { it.losses }
    val overallWinRate = if (totalPlayed > 0) ((totalWins * 100.0) / totalPlayed).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("computer_setup_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("setup_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Play vs Computer",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Configure Offline Stockfish UCI Opponent & Clock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 1. Choose Piece Color (White, Black, Random)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Choose Your Side",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (choice in PlayerColorChoice.entries) {
                                val selected = setupState.colorChoice == choice
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) {
                                        ChampionshipGold.copy(alpha = 0.22f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) ChampionshipGold else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectColor(choice) }
                                        .padding(vertical = 12.dp)
                                        .testTag("color_choice_${choice.name.lowercase()}")
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        ChessPieceIcon(
                                            piece = Piece(
                                                PieceType.KING,
                                                if (choice == PlayerColorChoice.BLACK) PieceColor.BLACK else PieceColor.WHITE
                                            ),
                                            pieceStyle = pieceStyle,
                                            size = 34.dp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = choice.displayName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) ChampionshipGold else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Ten Selectable Difficulty Levels (Beginner to Champion)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. Engine Difficulty (1–10)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ChampionshipGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Level ${currentTier.level}: ${currentTier.title}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ChampionshipGold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${currentTier.subtitle} • Nodes: ${currentTier.maxNodes} • Max Time: ${currentTier.movetimeMs}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = setupState.difficultyLevel.toFloat(),
                            onValueChange = { onSelectDifficulty(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.testTag("difficulty_slider")
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (tier in EngineDifficultyLevels.ALL) {
                                val isSelected = tier.level == setupState.difficultyLevel
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectDifficulty(tier.level) },
                                    label = { Text("${tier.level}. ${tier.title}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ChampionshipGold,
                                        selectedLabelColor = DeepSlateTeal
                                    ),
                                    modifier = Modifier.testTag("difficulty_level_${tier.level}")
                                )
                            }
                        }
                    }
                }
            }

            // 3. Clock Settings (Untimed, 1m, 3m, 5m, 10m, 15m + Increment)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = ChampionshipGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "3. Chess Clock & Increment",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Base Time Control:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (mins in ClockPresets.MINUTE_OPTIONS) {
                                val selected = setupState.clockMinutes == mins
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectClock(mins, setupState.incrementSeconds) },
                                    label = { Text(if (mins == 0) "Untimed" else "$mins min") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ChampionshipGold,
                                        selectedLabelColor = DeepSlateTeal
                                    ),
                                    modifier = Modifier.testTag("clock_mins_$mins")
                                )
                            }
                        }

                        if (setupState.clockMinutes > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Increment per Move:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (inc in ClockPresets.INCREMENT_OPTIONS) {
                                    val selected = setupState.incrementSeconds == inc
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectClock(setupState.clockMinutes, inc) },
                                        label = { Text("+${inc}s") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ChampionshipGold,
                                            selectedLabelColor = DeepSlateTeal
                                        ),
                                        modifier = Modifier.testTag("clock_inc_$inc")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Local Win / Draw / Loss / Win-Rate Statistics
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your Record vs Level ${currentTier.level} (${currentTier.title})",
                            style = MaterialTheme.typography.titleSmall,
                            color = ChampionshipGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMetricColumn("Wins", "${statsForTier.wins}")
                            StatMetricColumn("Draws", "${statsForTier.draws}")
                            StatMetricColumn("Losses", "${statsForTier.losses}")
                            StatMetricColumn("Win Rate", "${statsForTier.winRatePercent}%")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "All-Levels Record: $totalWins W / $totalDraws D / $totalLosses L ($overallWinRate% Win Rate)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 5. Pinned Start New Game CTA Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onStartMatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampionshipGold,
                    contentColor = DeepSlateTeal
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .height(54.dp)
                    .testTag("start_computer_game_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Match (${ClockPresets.formatLabel(setupState.clockMinutes, setupState.incrementSeconds)})",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun StatMetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
