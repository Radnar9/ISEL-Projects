package pdm.chessroyale.challenges.list

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import pdm.chessroyale.models.Color
import pdm.chessroyale.MainActivity
import pdm.chessroyale.R
import pdm.chessroyale.challenges.ChallengeInfo
import pdm.chessroyale.challenges.create.CreateChallengeActivity
import pdm.chessroyale.databinding.ActivityChallengesListBinding
import pdm.chessroyale.multiplayer.MultiplayerActivity

/**
 * The activity used to display the list of existing challenges.
 */
class ChallengesListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityChallengesListBinding.inflate(layoutInflater) }
    private val viewModel: ChallengesListViewModel by viewModels()

    /**
     * Sets up the screen behaviour
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.challengesList.setHasFixedSize(true)
        binding.challengesList.layoutManager = LinearLayoutManager(this)

        viewModel.challenges.observe(this) { result ->
            result.onSuccess {
                binding.challengesList.adapter = ChallengesListAdapter(it, ::challengeSelected)
                binding.refreshLayout.isRefreshing = false
            }
            result.onFailure {
                Toast.makeText(this, R.string.error_getting_list, Toast.LENGTH_LONG).show()
            }
        }

        binding.refreshLayout.setOnRefreshListener { updateChallengesList() }
        binding.createChallengeButton.setOnClickListener {
            startActivity(Intent(this, CreateChallengeActivity::class.java))
        }

        viewModel.enrolmentResult.observe(this) {
            it?.onSuccess { createdGameInfo ->
                val local = if (createdGameInfo.second.turn == Color.WHITE) Color.BLACK else Color.WHITE
                val intent = MultiplayerActivity.buildIntent(
                    origin = this,
                    turn = createdGameInfo.second.turn,
                    local = local,
                    challengeInfo = createdGameInfo.first
                )
                startActivity(intent)
            }
        }
    }

    /**
     * The screen is about to become visible: refresh its contents.
     */
    override fun onStart() {
        super.onStart()
        updateChallengesList()
    }

    /**
     * Called whenever the challenges list is to be fetched again.
     */
    private fun updateChallengesList() {
        binding.refreshLayout.isRefreshing = true
        viewModel.fetchChallenges()
    }

    /**
     * Called whenever a list element is selected. The player that accepts the challenge is the
     * first to make a move.
     *
     * @param challenge the selected challenge
     */
    private fun challengeSelected(challenge: ChallengeInfo) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.accept_challenge_dialog_title, challenge.challengerName))
            .setPositiveButton(R.string.accept_challenge_dialog_ok) { _, _ -> viewModel.tryAcceptChallenge(challenge) }
            .setNegativeButton(R.string.accept_challenge_dialog_cancel, null)
            .create()
            .show()
    }
}