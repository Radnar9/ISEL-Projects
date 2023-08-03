package pdm.chessroyale.multiplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pdm.chessroyale.models.Color
import pdm.chessroyale.MainActivity
import pdm.chessroyale.R
import pdm.chessroyale.challenges.ChallengeInfo
import pdm.chessroyale.databinding.ActivityMultiplayerBinding
import pdm.chessroyale.models.ChessBoard
import pdm.chessroyale.models.pieces.Piece
import pdm.chessroyale.models.pieces.Position
import pdm.chessroyale.views.Tile


private const val GAME_EXTRA = "GameActivity.GameInfoExtra"
private const val LOCAL_PLAYER_EXTRA = "GameActivity.LocalPlayerExtra"

/**
 * The activity that displays the board.
 */
class MultiplayerActivity : AppCompatActivity() {

    companion object {
        fun buildIntent(origin: Context, local: Color, turn: Color, challengeInfo: ChallengeInfo) =
            Intent(origin, MultiplayerActivity::class.java)
                .putExtra(GAME_EXTRA, MultiplayerState(
                    challengeInfo.id, turn, null, null, false, null)
                )
                .putExtra(LOCAL_PLAYER_EXTRA, local.name)
    }

    private val binding by lazy { ActivityMultiplayerBinding.inflate(layoutInflater) }

    private val localPlayer: Color by lazy {
        val local = intent.getStringExtra(LOCAL_PLAYER_EXTRA)
        if (local != null) Color.valueOf(local)
        else throw IllegalArgumentException("Mandatory extra $LOCAL_PLAYER_EXTRA not present")
    }

    private val initialState: MultiplayerState by lazy {
        intent.getParcelableExtra<MultiplayerState>(GAME_EXTRA) ?:
        throw IllegalArgumentException("Mandatory extra $GAME_EXTRA not present")
    }

    private val viewModel: MultiplayerViewModel by viewModels {
        @Suppress("UNCHECKED_CAST")
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MultiplayerViewModel(application, initialState, localPlayer, SavedStateHandle()) as T
            }
        }
    }

    private fun displayError() {
        Toast.makeText(this, R.string.get_multiplayer_error, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            viewModel.getPuzzle()
        }

        viewModel.chessData.observe(this) {
            binding.boardView.print(it.getChessBoard())
            if(it.isGameOver()) {
                if (it.isSurrender() != null) {
                    binding.nextPlayer.text = getString(R.string.winner)
                    binding.nextColor.text = it.isSurrender()!!.name
                    binding.status.visibility = View.VISIBLE
                    binding.status.text = getString(R.string.game_over)
                } else {
                    val colorWinner = it.isCheckMate()
                    binding.nextPlayer.text = getString(R.string.winner)
                    binding.status.visibility = View.VISIBLE
                    if(colorWinner != null) {
                        binding.nextColor.text = colorWinner.name
                        binding.status.text = getString(R.string.checkmate)
                    } else {
                        binding.nextColor.text = it.getCurrColor().name
                        binding.status.text = getString(R.string.game_over)
                    }
                }
                binding.surrenderButton.visibility = View.INVISIBLE
                binding.homeButton.visibility = View.VISIBLE
                binding.homeButton.setOnClickListener {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            } else {
                binding.surrenderButton.visibility = View.VISIBLE
                binding.nextColor.visibility = View.VISIBLE
                binding.nextPlayer.visibility = View.VISIBLE
                binding.status.visibility = View.INVISIBLE
                binding.nextColor.text = it.getCurrColor().name
                startOnTileClickedListener()
                surrenderListener()
            }
        }

        viewModel.error.observe(this) { displayError() }
    }

    private fun startOnTileClickedListener() {
        binding.boardView.onTileClickedListener = { currTile: Tile, _: Int, _: Int ->
            val chessBoard = viewModel.getChessInstance()
            if (chessBoard.isPlayable()) {
                val currentColor = chessBoard.getCurrColor()
                if (chessBoard.prevTile == null) {
                    if (currTile.piece.getColor() == chessBoard.getCurrColor() &&
                        currTile.piece.getColor() == localPlayer) {
                        currTile.enableSelectedColor()
                        chessBoard.prevTile = currTile
                    } else {
                        chessBoard.prevTile?.disableSelectedColor()
                        Toast.makeText(
                            this,
                            getString(R.string.wrong_piece_color),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    chessBoard.prevTile!!.disableSelectedColor()
                    if (chessBoard.tryToMove(chessBoard.prevTile!!, currTile)) {
                        val prevPos = chessBoard.prevTile!!.piece.getPosition()
                        if (!verifyPromotion(currTile.piece, chessBoard, prevPos)) {
                            viewModel.updateChessData()
                            viewModel.sendPlay(prevPos, currTile.piece.getPosition(), false)
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.invalid_move), Toast.LENGTH_SHORT)
                            .show()
                    }
                    chessBoard.prevTile = null
                }

                if (viewModel.getChessInstance().isGameOver()) {
                    binding.nextPlayer.text = getString(R.string.winner)
                    val color = viewModel.getChessInstance().isCheckMate()
                    if(color != null) {
                        binding.nextColor.text = color.name
                        binding.status.visibility = View.VISIBLE
                        binding.status.text = getString(R.string.checkmate)
                    } else {
                        binding.nextColor.text = currentColor.name
                    }
                }
            }
        }
    }

    private fun verifyPromotion(currPiece: Piece, chessBoard: ChessBoard, prevPos: Position): Boolean {
        if (chessBoard.isPromotion(currPiece)) {
            chessBoard.setPlayable(false)
            binding.nextColor.visibility= View.INVISIBLE
            binding.nextPlayer.visibility= View.INVISIBLE
            setPromoteLayoutVisibility(true)
            promotePieceListener(currPiece.getPosition(), chessBoard, prevPos)
            return true
        }
        return false
    }

    private fun setPromoteLayoutVisibility(v: Boolean) {
        val visibility = if (v) View.VISIBLE else View.INVISIBLE
        binding.bishopButton.visibility = visibility
        binding.knightButton.visibility = visibility
        binding.queenButton.visibility = visibility
        binding.rookButton.visibility = visibility
        binding.promoteText.visibility = visibility
    }

    private fun promotePieceListener(toPromotePos: Position, chess: ChessBoard, prevPos: Position) {
        binding.queenButton.setOnClickListener {
            updateForPromotionChess('Q', toPromotePos, chess, prevPos)
        }
        binding.bishopButton.setOnClickListener {
            updateForPromotionChess('B', toPromotePos, chess, prevPos)
        }
        binding.knightButton.setOnClickListener {
            updateForPromotionChess('K', toPromotePos, chess, prevPos)
        }
        binding.rookButton.setOnClickListener {
            updateForPromotionChess('R', toPromotePos, chess, prevPos)
        }
    }

    private fun updateForPromotionChess(pieceToBePromoted: Char, toPromotePos: Position,
                                        chess: ChessBoard, prevPos: Position) {
        val piecePromoted = chess.getPromotedPiece(toPromotePos, pieceToBePromoted)
        chess.getChessBoard()[toPromotePos.line]!![toPromotePos.column] = piecePromoted
        setPromoteLayoutVisibility(false)
        binding.nextColor.visibility = View.VISIBLE
        binding.nextPlayer.visibility = View.VISIBLE
        viewModel.updateChessData()
        viewModel.getChessInstance().setPlayable(true)
        viewModel.sendPlay(prevPos, toPromotePos, true)
    }

    private fun surrenderListener() {
        binding.surrenderButton.setOnClickListener {
            val winnerColor = if (localPlayer == Color.BLACK) Color.WHITE else Color.BLACK
            binding.surrenderButton.visibility = View.INVISIBLE
            binding.nextPlayer.text = getString(R.string.winner)
            binding.nextColor.text = winnerColor.name
            binding.status.text = getString(R.string.game_over)
            binding.status.visibility = View.VISIBLE
            viewModel.getChessInstance().setSurrender(winnerColor)
            viewModel.surrender(winnerColor)
        }
    }
}