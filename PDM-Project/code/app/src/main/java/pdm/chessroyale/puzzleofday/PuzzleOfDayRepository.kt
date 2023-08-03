package pdm.chessroyale.puzzleofday

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pdm.chessroyale.*
import pdm.chessroyale.history.HistoryPuzzleDao
import pdm.chessroyale.history.PuzzleEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun PuzzleEntity.toPuzzleDTO() = PuzzleDTO(
    puzzleInfo = PuzzleInfo(game = Game(puzzle), puzzle = Puzzle(solution.split(" "))),
    date = id,
    isCompleted = isCompleted
)

class PuzzleOfDayRepository (
    private val puzzleService: DailyPuzzleService,
    private val historyPuzzleDao: HistoryPuzzleDao
){

    private suspend fun maybeGetPuzzleFromDB(): PuzzleEntity? =
        withContext(Dispatchers.IO) {
            historyPuzzleDao.getLast(1).firstOrNull()
        }


    private suspend fun getTodayPuzzleFromAPI(): PuzzleDTO =
        withContext(Dispatchers.IO) {
            val response = puzzleService.getPuzzle().execute()
            val dailyPuzzle = response.body()
            if (dailyPuzzle != null && response.isSuccessful)
                PuzzleDTO(
                    PuzzleInfo(Game(dailyPuzzle.game.pgn), dailyPuzzle.puzzle),
                    getDate(),
                    false
                )
            else throw ServiceUnavailable()
        }

    private suspend fun saveToDB(puzzleDTO: PuzzleDTO) =
        withContext(Dispatchers.IO) {
            historyPuzzleDao.insert(
                PuzzleEntity(
                    puzzleDTO.date,
                    puzzleDTO.puzzleInfo.game.pgn,
                    puzzleDTO.puzzleInfo.puzzle.solution.joinToString(" "),
                    puzzleDTO.isCompleted
                )
            )
        }

    suspend fun updateToDB(puzzleDTO: PuzzleDTO) =
        withContext(Dispatchers.IO) {
            historyPuzzleDao.update(PuzzleEntity(
                puzzleDTO.date,
                puzzleDTO.puzzleInfo.game.pgn,
                puzzleDTO.puzzleInfo.puzzle.solution.joinToString(" "),
                puzzleDTO.isCompleted
            ))
        }

    suspend fun fetchQuoteOfDay(mustSaveToDB: Boolean = false): PuzzleDTO =
        withContext(Dispatchers.IO) {
            val maybePuzzle = maybeGetPuzzleFromDB()
            if (maybePuzzle != null) maybePuzzle.toPuzzleDTO()
            else {
                val todayPuzzle = getTodayPuzzleFromAPI()
                try { saveToDB(todayPuzzle); todayPuzzle }
                catch (e: Exception) {
                    if (mustSaveToDB) throw e
                    else todayPuzzle
                }
            }
        }

    private fun getDate(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return current.format(formatter)
    }
 }