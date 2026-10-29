package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_games")
data class SavedGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val gameMode: String, // "VS_COMPUTER" or "TWO_PLAYER"
    val difficultyLevel: Int,
    val playerColor: String, // "WHITE" or "BLACK"
    val initialFen: String,
    val currentFen: String,
    val moveHistoryEncoded: String, // Pipe-delimited "fromUci|toUci|promo|san|prevFen|resFen"
    val positionHistoryEncoded: String, // Pipe-delimited core FEN signatures
    val pgnText: String,
    val whiteTimeRemainingMs: Long,
    val blackTimeRemainingMs: Long,
    val initialClockMinutes: Int,
    val incrementSeconds: Int,
    val status: String, // GameStatus name
    val winnerColor: String?, // "WHITE", "BLACK", or null
    val resultSummary: String,
    val isAutoSave: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "difficulty_stats")
data class DifficultyStatsEntity(
    @PrimaryKey val difficultyLevel: Int,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0
) {
    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((wins * 100.0) / gamesPlayed).toInt() else 0
}

@Entity(tableName = "puzzle_progress")
data class PuzzleProgressEntity(
    @PrimaryKey val puzzleId: Int,
    val isSolved: Boolean = false,
    val attempts: Int = 0,
    val successes: Int = 0,
    val hintsUsed: Int = 0,
    val lastAttemptAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkTheme: Boolean = true,
    val boardThemeId: String = "WARM_WOOD",
    val pieceStyleId: String = "CLASSIC_STAUNTON",
    val showLegalMoveHighlights: Boolean = true,
    val showEvaluationBar: Boolean = true,
    val autoFlipTwoPlayer: Boolean = false,
    val soundEnabled: Boolean = false, // Disabled by default per requirements
    val defaultDifficulty: Int = 4,
    val defaultClockMinutes: Int = 10,
    val defaultIncrementSeconds: Int = 0,
    val analysisDepth: Int = 4,
    val completedLessonIdsCsv: String = ""
)

@Dao
interface ChessDao {
    // Saved Games
    @Query("SELECT * FROM saved_games ORDER BY isAutoSave DESC, updatedAt DESC")
    fun observeAllSavedGames(): Flow<List<SavedGameEntity>>

    @Query("SELECT * FROM saved_games WHERE isAutoSave = 1 LIMIT 1")
    suspend fun getAutoSavedGame(): SavedGameEntity?

    @Query("SELECT * FROM saved_games WHERE id = :id LIMIT 1")
    suspend fun getSavedGameById(id: Long): SavedGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSavedGame(game: SavedGameEntity): Long

    @Query("UPDATE saved_games SET title = :newTitle, updatedAt = :timestamp WHERE id = :id")
    suspend fun renameSavedGame(id: Long, newTitle: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM saved_games WHERE id = :id")
    suspend fun deleteSavedGameById(id: Long)

    // Difficulty Statistics
    @Query("SELECT * FROM difficulty_stats ORDER BY difficultyLevel ASC")
    fun observeDifficultyStats(): Flow<List<DifficultyStatsEntity>>

    @Query("SELECT * FROM difficulty_stats WHERE difficultyLevel = :level LIMIT 1")
    suspend fun getStatsForDifficulty(level: Int): DifficultyStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDifficultyStats(stats: DifficultyStatsEntity)

    @Query("DELETE FROM difficulty_stats")
    suspend fun clearAllDifficultyStats()

    // Puzzle Progress
    @Query("SELECT * FROM puzzle_progress")
    fun observePuzzleProgress(): Flow<List<PuzzleProgressEntity>>

    @Query("SELECT * FROM puzzle_progress WHERE puzzleId = :puzzleId LIMIT 1")
    suspend fun getPuzzleProgress(puzzleId: Int): PuzzleProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPuzzleProgress(progress: PuzzleProgressEntity)

    @Query("DELETE FROM puzzle_progress")
    suspend fun clearAllPuzzleProgress()

    // App Settings
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getAppSettingsOnce(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppSettings(settings: AppSettingsEntity)
}

@Database(
    entities = [
        SavedGameEntity::class,
        DifficultyStatsEntity::class,
        PuzzleProgressEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChessDatabase : RoomDatabase() {
    abstract fun chessDao(): ChessDao

    companion object {
        @Volatile
        private var INSTANCE: ChessDatabase? = null

        fun getInstance(context: Context): ChessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChessDatabase::class.java,
                    "chess_master_offline.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ChessRepository(private val dao: ChessDao) {
    val savedGamesFlow: Flow<List<SavedGameEntity>> = dao.observeAllSavedGames()
    val difficultyStatsFlow: Flow<List<DifficultyStatsEntity>> = dao.observeDifficultyStats()
    val puzzleProgressFlow: Flow<List<PuzzleProgressEntity>> = dao.observePuzzleProgress()
    val settingsFlow: Flow<AppSettingsEntity?> = dao.observeAppSettings()

    suspend fun ensureDefaultsInitialized() {
        if (dao.getAppSettingsOnce() == null) {
            dao.upsertAppSettings(AppSettingsEntity())
        }
        for (lvl in 1..10) {
            if (dao.getStatsForDifficulty(lvl) == null) {
                dao.upsertDifficultyStats(DifficultyStatsEntity(difficultyLevel = lvl))
            }
        }
    }

    suspend fun saveGame(entity: SavedGameEntity): Long {
        if (entity.isAutoSave) {
            val existingAuto = dao.getAutoSavedGame()
            if (existingAuto != null) {
                return dao.insertOrUpdateSavedGame(entity.copy(id = existingAuto.id))
            }
        }
        return dao.insertOrUpdateSavedGame(entity)
    }

    suspend fun getSavedGame(id: Long): SavedGameEntity? = dao.getSavedGameById(id)

    suspend fun renameSavedGame(id: Long, newTitle: String) {
        dao.renameSavedGame(id, newTitle.trim().ifEmpty { "Saved Match" })
    }

    suspend fun deleteSavedGame(id: Long) {
        dao.deleteSavedGameById(id)
    }

    suspend fun recordMatchOutcome(difficultyLevel: Int, outcome: String) {
        val current = dao.getStatsForDifficulty(difficultyLevel)
            ?: DifficultyStatsEntity(difficultyLevel = difficultyLevel)
        val updated = when (outcome) {
            "WIN" -> current.copy(gamesPlayed = current.gamesPlayed + 1, wins = current.wins + 1)
            "LOSS" -> current.copy(gamesPlayed = current.gamesPlayed + 1, losses = current.losses + 1)
            else -> current.copy(gamesPlayed = current.gamesPlayed + 1, draws = current.draws + 1)
        }
        dao.upsertDifficultyStats(updated)
    }

    suspend fun recordPuzzleAttempt(puzzleId: Int, solved: Boolean, usedHint: Boolean) {
        val current = dao.getPuzzleProgress(puzzleId) ?: PuzzleProgressEntity(puzzleId = puzzleId)
        val updated = current.copy(
            isSolved = current.isSolved || solved,
            attempts = current.attempts + 1,
            successes = if (solved) current.successes + 1 else current.successes,
            hintsUsed = if (usedHint) current.hintsUsed + 1 else current.hintsUsed,
            lastAttemptAt = System.currentTimeMillis()
        )
        dao.upsertPuzzleProgress(updated)
    }

    suspend fun updateSettings(transform: (AppSettingsEntity) -> AppSettingsEntity) {
        val current = dao.getAppSettingsOnce() ?: AppSettingsEntity()
        dao.upsertAppSettings(transform(current))
    }

    suspend fun resetAllStatistics() {
        dao.clearAllDifficultyStats()
        dao.clearAllPuzzleProgress()
        for (lvl in 1..10) {
            dao.upsertDifficultyStats(DifficultyStatsEntity(difficultyLevel = lvl))
        }
    }
}
