package pdm.chessroyale.singleplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import pdm.chessroyale.models.ChessBoard

private const val PUZZLE_SINGLEPLAYER_VIEW_STATE = "PuzzleOfDayActivity.ViewState"

class SingleplayerViewModel(
    application: Application,
    private val savedState: SavedStateHandle
    ) : AndroidViewModel(application){

    val chessData: LiveData<ChessBoard> = savedState.getLiveData(PUZZLE_SINGLEPLAYER_VIEW_STATE)
    private lateinit var chess: ChessBoard

    fun getPuzzle() {
        chess = ChessBoard(null)
        savedState.set<ChessBoard>(PUZZLE_SINGLEPLAYER_VIEW_STATE, chess)
    }

    fun updateChessData() {
        savedState.set(PUZZLE_SINGLEPLAYER_VIEW_STATE, chess)
    }

    fun getChessInstance(): ChessBoard {
        return chess
    }
}