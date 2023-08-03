package pdm.chessroyale.puzzleofday

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pdm.chessroyale.PuzzleDTO
import pdm.chessroyale.R
import pdm.chessroyale.databinding.ActivityPuzzleOfDayBinding
import pdm.chessroyale.views.Tile

private const val PUZZLE_EXTRA = "PuzzleActivity.Extra.Puzzle"
class PuzzleOfDayActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPuzzleOfDayBinding.inflate(layoutInflater) }
    private val puzzleOfDayViewModel: PuzzleOfDayViewModel by viewModels()

    companion object {
        fun buildIntent(origin: Activity, puzzleDto: PuzzleDTO): Intent {
            val msg = Intent(origin, PuzzleOfDayActivity::class.java)
            msg.putExtra(PUZZLE_EXTRA, puzzleDto)
            return msg
        }
    }

    private fun displayError() {
        Toast.makeText(this, R.string.get_puzzle_error, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.nextColor.visibility = View.INVISIBLE
        binding.nextPlayer.visibility = View.INVISIBLE
        binding.status.visibility = View.VISIBLE
        binding.status.text = getString(R.string.loading_puzzle)

        val puzzleDto = intent.getParcelableExtra<PuzzleDTO>(PUZZLE_EXTRA)

        if (savedInstanceState == null) {
            if (puzzleDto == null) puzzleOfDayViewModel.getPuzzleOfDay()
            else {
                puzzleOfDayViewModel.buildNewPuzzle(puzzleDto)
                binding.playAgain?.visibility = View.VISIBLE
                binding.solution?.visibility = if (puzzleOfDayViewModel.isCompleted()) View.VISIBLE else View.INVISIBLE
                puzzleOfDayViewModel.setPuzzlePlayable(false)
            }
        }

        puzzleOfDayViewModel.chessData.observe(this) {
            binding.nextColor.visibility = View.VISIBLE
            binding.nextPlayer.visibility = View.VISIBLE
            binding.status.visibility = View.INVISIBLE
            binding.nextColor.text = it.getCurrColor().name
            binding.boardView.print(it.getChessBoard())
            startOnTileClickedListener(it.isPlayable())
        }

        puzzleOfDayViewModel.error.observe(this) { displayError() }

        binding.solution?.setOnClickListener {
            puzzleOfDayViewModel.buildPuzzleSolution()
            binding.solution!!.visibility = View.INVISIBLE
            binding.status.visibility = View.VISIBLE
            binding.nextColor.visibility = View.INVISIBLE
            binding.nextPlayer.visibility = View.INVISIBLE
            binding.status.text = getString(R.string.solution_presented)
        }

        binding.playAgain?.setOnClickListener {
            if (puzzleDto != null) {
                puzzleOfDayViewModel.buildNewPuzzle(puzzleDto)
                binding.playAgain?.visibility = View.INVISIBLE
                binding.solution?.visibility = View.INVISIBLE
                puzzleOfDayViewModel.setPuzzlePlayable(true)
            }
        }
    }

    private fun startOnTileClickedListener(isPlayable: Boolean) {
        val solutionList = puzzleOfDayViewModel.getChessInstance().getChessBoardSolution()
        binding.boardView.onTileClickedListener = { currTile: Tile, _: Int, _: Int ->
            if (!isPlayable) {
                Toast.makeText(this, getString(R.string.puzzle_not_started), Toast.LENGTH_SHORT).show()
            } else {
                val chessBoard = puzzleOfDayViewModel.getChessInstance()
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
                    val aux = chessBoard.play(chessBoard.prevTile!!, currTile)
                    puzzleOfDayViewModel.updateChessData(chessBoard)
                    chessBoard.prevTile!!.disableSelectedColor()
                    chessBoard.prevTile = null
                    if (aux) {
                        chessBoard.removeFirstSolutionMove()
                        if (solutionList.isEmpty()) {
                            binding.status.visibility = View.VISIBLE
                            binding.status.text = getString(R.string.puzzle_solved)
                            binding.nextColor.visibility = View.INVISIBLE
                            binding.nextPlayer.visibility = View.INVISIBLE
                            binding.boardView.onTileClickedListener = null
                            puzzleOfDayViewModel.setPuzzleCompleted()
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.wrong_move), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}