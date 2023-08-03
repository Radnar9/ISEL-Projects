package pdm.chessroyale.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pdm.chessroyale.databinding.ActivityHistoryBinding
import pdm.chessroyale.puzzleofday.PuzzleOfDayActivity.Companion.buildIntent

class HistoryActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHistoryBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<HistoryActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.puzzleList.layoutManager = LinearLayoutManager(this)

        viewModel.puzzlesHistory.observe(this) {
            binding.puzzleList.adapter = HistoryAdapter(it) { puzzleDTO ->
                startActivity(buildIntent(this, puzzleDTO))
            }
        }
    }
}