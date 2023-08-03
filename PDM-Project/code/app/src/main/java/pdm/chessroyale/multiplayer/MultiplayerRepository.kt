package pdm.chessroyale.multiplayer

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import pdm.chessroyale.models.Color
import pdm.chessroyale.challenges.ChallengeInfo

/**
 * The path of the Firestore collection that contains all the active games
 */
private const val GAMES_COLLECTION = "games"

private const val GAME_STATE_KEY = "game"

/**
 * The repository for the ChessRoyale games, implemented using Firebase.
 */
class MultiplayerRepository(private val mapper: Gson) {

    /**
     * Creates the game for the given challenge ID
     */
    fun createGame(
        challenge: ChallengeInfo,
        onComplete: (Result<Pair<ChallengeInfo, MultiplayerState>>) -> Unit
    ) {
        val multiplayerState = MultiplayerState(challenge.id, Color.WHITE, null, null, false, null)
        Firebase.firestore.collection(GAMES_COLLECTION)
            .document(challenge.id)
            .set(hashMapOf(GAME_STATE_KEY to mapper.toJson(multiplayerState)))
            .addOnSuccessListener { onComplete(Result.success(Pair(challenge, multiplayerState))) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    /**
     * Updates the shared multiplayer state
     */
    fun updateMultiplayerState(multiplayerState: MultiplayerState, onComplete: (Result<MultiplayerState>) -> Unit) {

        Firebase.firestore.collection(GAMES_COLLECTION)
            .document(multiplayerState.id)
            .set(hashMapOf(GAME_STATE_KEY to mapper.toJson(multiplayerState)))
            .addOnSuccessListener { onComplete(Result.success(multiplayerState)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }

    /**
     * Subscribes for changes in the challenge identified by [challengeId]
     */
    fun subscribeToMultiplayerStateChanges(
        challengeId: String,
        onSubscriptionError: (Exception) -> Unit,
        onMultiplayerStateChange: (MultiplayerState) -> Unit
    ): ListenerRegistration {

        return Firebase.firestore
            .collection(GAMES_COLLECTION)
            .document(challengeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onSubscriptionError(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    val multiplayerState = mapper.fromJson(
                        snapshot.get(GAME_STATE_KEY) as String,
                        MultiplayerState::class.java
                    )
                    onMultiplayerStateChange(multiplayerState)
                }
            }
    }

    /**
     * Deletes the shared multiplayer state for the given challenge.
     */
    fun deleteGame(challengeId: String, onComplete: (Result<Unit>) -> Unit) {
        Firebase.firestore.collection(GAMES_COLLECTION)
            .document(challengeId)
            .delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }
}
