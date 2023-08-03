package pdm.chessroyale.history

import androidx.room.*

@Entity(tableName = "history_puzzle")
data class PuzzleEntity(
    @PrimaryKey val id: String,
    val puzzle: String,
    val solution: String,
    val isCompleted: Boolean
)

@Dao
interface HistoryPuzzleDao {
    @Insert
    fun insert(puzzle: PuzzleEntity)

    @Update
    fun update(puzzle: PuzzleEntity)

    @Delete
    fun delete(puzzle: PuzzleEntity)

    @Query("SELECT * FROM history_puzzle ORDER BY id DESC LIMIT 100")
    fun getAll(): List<PuzzleEntity>

    @Query("SELECT * FROM history_puzzle ORDER BY id DESC LIMIT :count")
    fun getLast(count: Int): List<PuzzleEntity>
}

@Database(entities = [PuzzleEntity::class], version = 1)
abstract class HistoryDatabase: RoomDatabase() {
    abstract fun getHistoryPuzzleDao(): HistoryPuzzleDao
}