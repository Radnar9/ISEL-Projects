package pdm.chessroyale.puzzleofday

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pdm.chessroyale.*
import pdm.chessroyale.models.ChessBoard

private const val PUZZLE_OF_DAY_ACTIVITY_VIEW_STATE = "PuzzleOfDayActivity.ViewState"

class PuzzleOfDayViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {

    val chessData: LiveData<ChessBoard> = savedState.getLiveData(PUZZLE_OF_DAY_ACTIVITY_VIEW_STATE)
    private lateinit var chess: ChessBoard

    private val repo: PuzzleOfDayRepository by lazy {
        val app = getApplication<PuzzleOfDayApplication>()
        PuzzleOfDayRepository(app.puzzleOfDayService, app.historyDb.getHistoryPuzzleDao())
    }

    private val _error: MutableLiveData<Throwable> = MutableLiveData()
    val error: LiveData<Throwable> = _error

    fun getPuzzleOfDay() {
        viewModelScope.launch {
            val result = repo.fetchQuoteOfDay()
            chess = ChessBoard(result)
            savedState.set<ChessBoard>(PUZZLE_OF_DAY_ACTIVITY_VIEW_STATE, chess)
        }
    }

    fun updateChessData(chessBoard: ChessBoard?) {
        savedState.set(PUZZLE_OF_DAY_ACTIVITY_VIEW_STATE, chessBoard)
    }

    fun getChessInstance(): ChessBoard {
        return chess
    }

    fun buildNewPuzzle(PuzzleDto: PuzzleDTO) {
        chess = ChessBoard(PuzzleDto)
        savedState.set<ChessBoard>(PUZZLE_OF_DAY_ACTIVITY_VIEW_STATE, chess)
    }

    fun setPuzzleCompleted() {
        viewModelScope.launch {
            repo.updateToDB(chess.setPuzzleCompleted())
        }
    }

    fun buildPuzzleSolution() {
        chess.buildSolution()
        updateChessData(chess)
    }

    fun setPuzzlePlayable(state: Boolean) {
        chess.setPlayable(state)
        updateChessData(chess)
    }

    fun isCompleted(): Boolean {
        return chess.isCompleted()
    }
}