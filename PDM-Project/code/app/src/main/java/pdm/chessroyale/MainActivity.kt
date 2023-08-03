package pdm.chessroyale

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import pdm.chessroyale.challenges.list.ChallengesListActivity
import pdm.chessroyale.databinding.ActivityMainBinding
import pdm.chessroyale.history.HistoryActivity
import pdm.chessroyale.puzzleofday.PuzzleOfDayActivity
import pdm.chessroyale.singleplayer.SingleplayerActivity

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.puzzleOfDayButton.setOnClickListener {
           startActivity(Intent(this, PuzzleOfDayActivity::class.java))
        }

        binding.singleplayerButton.setOnClickListener {
            startActivity(Intent(this, SingleplayerActivity::class.java))
        }

        binding.multiplayerButton.setOnClickListener {
            startActivity(Intent(this, ChallengesListActivity::class.java))
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}