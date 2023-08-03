package pdm.chessroyale.singleplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import pdm.chessroyale.R
import pdm.chessroyale.databinding.ActivitySingleplayerBinding
import pdm.chessroyale.models.ChessBoard
import pdm.chessroyale.models.pieces.Piece
import pdm.chessroyale.models.pieces.Position
import pdm.chessroyale.views.Tile

class SingleplayerActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySingleplayerBinding.inflate(layoutInflater) }
    private val viewModel: SingleplayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            viewModel.getPuzzle()
        }

        viewModel.chessData.observe(this) {
            binding.boardView.print(it.getChessBoard())
            if(it.isGameOver()) {
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
            } else {
                binding.nextColor.visibility = View.VISIBLE
                binding.nextPlayer.visibility = View.VISIBLE
                binding.status.visibility = View.INVISIBLE
                binding.nextColor.text = it.getCurrColor().name
                startOnTileClickedListener()
            }
        }
    }

    private fun startOnTileClickedListener() {
        binding.boardView.onTileClickedListener = { currTile: Tile, _: Int, _: Int ->
            val chessBoard = viewModel.getChessInstance()
            if (chessBoard.isPlayable()) {
                val currentColor = chessBoard.getCurrColor()
                if (chessBoard.prevTile == null) {
                    if (currTile.piece.getColor() == chessBoard.getCurrColor()) {
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
                        if (!verifyPromotion(currTile.piece, chessBoard)) {
                            viewModel.updateChessData()
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.invalid_move), Toast.LENGTH_SHORT)
                            .show()
                    }
                    chessBoard.prevTile = null
                }

                if (chessBoard.isGameOver()) {
                    binding.nextPlayer.text = getString(R.string.winner)
                    val color = chessBoard.isCheckMate()
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

    private fun verifyPromotion(currPiece: Piece, chessBoard: ChessBoard): Boolean {
        if (chessBoard.isPromotion(currPiece)) {
            chessBoard.setPlayable(false)
            binding.nextColor.visibility= View.INVISIBLE
            binding.nextPlayer.visibility= View.INVISIBLE
            setPromoteLayoutVisibility(true)
            promotePieceListener(currPiece.getPosition(), chessBoard)
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

    private fun promotePieceListener(toPromotePos: Position, chess: ChessBoard) {
        binding.queenButton.setOnClickListener {
            updateForPromotionChess('Q', toPromotePos, chess)
        }
        binding.bishopButton.setOnClickListener {
            updateForPromotionChess('B', toPromotePos, chess)
        }
        binding.knightButton.setOnClickListener {
            updateForPromotionChess('K', toPromotePos, chess)
        }
        binding.rookButton.setOnClickListener {
            updateForPromotionChess('R', toPromotePos, chess)
        }
    }

    private fun updateForPromotionChess(pieceToBePromoted: Char, toPromotePos: Position, chess: ChessBoard) {
        val piecePromoted = chess.getPromotedPiece(toPromotePos, pieceToBePromoted)
        chess.getChessBoard()[toPromotePos.line]!![toPromotePos.column] = piecePromoted
        setPromoteLayoutVisibility(false)
        binding.nextColor.visibility= View.VISIBLE
        binding.nextPlayer.visibility= View.VISIBLE
        viewModel.updateChessData()
        viewModel.getChessInstance().setPlayable(true)
    }
}