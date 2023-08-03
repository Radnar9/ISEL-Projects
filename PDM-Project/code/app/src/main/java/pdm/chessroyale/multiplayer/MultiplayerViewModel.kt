package pdm.chessroyale.multiplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import pdm.chessroyale.models.Color
import pdm.chessroyale.puzzleofday.PuzzleOfDayApplication
import pdm.chessroyale.models.ChessBoard
import pdm.chessroyale.models.pieces.*
import pdm.chessroyale.views.Tile

private const val PUZZLE_MULTIPLAYER_VIEW_STATE = "MultiplayerActivity.ViewState"

class MultiplayerViewModel(
    application: Application,
    private val initialMultiplayerState: MultiplayerState,
    private val localPlayer: Color,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application){

    val chessData: LiveData<ChessBoard> = savedState.getLiveData(PUZZLE_MULTIPLAYER_VIEW_STATE)
    private lateinit var chess: ChessBoard

    private val _error: MutableLiveData<Throwable> = MutableLiveData()
    val error: LiveData<Throwable> = _error

    private val multiplayerSubscription = getApplication<PuzzleOfDayApplication>()
        .multiplayerRepository.subscribeToMultiplayerStateChanges(
            challengeId = initialMultiplayerState.id,
            onSubscriptionError = { _error.value = it },
            onMultiplayerStateChange = {
                if (it.endGame) {
                    if (it.nextPos != null && !chess.isKing(it.nextPos)) {
                        chess.setCheckMate(it.turn)
                    }
                    if (it.nextPos == null && chess.isSurrender() == null) {
                        chess.setSurrender(localPlayer)
                    }
                    chess.setPlayable(false)
                    chess.setGameOver()
                    updateChessData()
                }
                if (it.turn == localPlayer) {
                    chess.movePiece(it.prevPos, it.nextPos)
                    if (it.promotedTo != null && it.nextPos != null) {
                        val piecePromoted = chess.getPromotedPiece(it.nextPos, it.promotedTo)
                        chess.getChessBoard()[it.nextPos.line]!![it.nextPos.column] = piecePromoted
                    }
                    updateChessData()
                }
            }
        )

    fun getPuzzle() {
        chess = ChessBoard(null)
        savedState.set<ChessBoard>(PUZZLE_MULTIPLAYER_VIEW_STATE, chess)
    }

    fun updateChessData() {
        savedState.set(PUZZLE_MULTIPLAYER_VIEW_STATE, chess)
    }

    fun getChessInstance(): ChessBoard {
        return chess
    }

    private fun getPromotedPieceChar(promotedPos: Position): Char? {
        return when (chess.getChessBoard()[promotedPos.line]!![promotedPos.column]) {
            is Queen -> 'Q'
            is Knight -> 'K'
            is Rook -> 'R'
            is Bishop -> 'B'
            else -> null
        }
    }

    fun sendPlay(prevPos: Position, currPos: Position, isPromotion: Boolean) {
        val endGame = chess.isGameOver()
        val nextColor = chess.getCurrColor()
        val promotedPieceChar = if (isPromotion) getPromotedPieceChar(currPos) else null
        getApplication<PuzzleOfDayApplication>().multiplayerRepository.updateMultiplayerState(
            multiplayerState = MultiplayerState(
                initialMultiplayerState.id,
                nextColor,
                prevPos,
                currPos,
                endGame,
                promotedPieceChar
            ),
            onComplete = { result ->
                result.onFailure { _error.value = it }
            }
        )
    }

    fun surrender(winnerColor: Color) {
        getApplication<PuzzleOfDayApplication>().multiplayerRepository.updateMultiplayerState(
            multiplayerState = MultiplayerState(
                initialMultiplayerState.id,
                winnerColor,
                null,
                null,
                true,
                null
            ),
            onComplete = { result ->
                result.onFailure { _error.value = it }
            }
        )
    }

    /**
     * View model is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        getApplication<PuzzleOfDayApplication>().multiplayerRepository.deleteGame(
            challengeId = initialMultiplayerState.id,
            onComplete = { }
        )
        multiplayerSubscription.remove()
    }
}