package pdm.chessroyale.multiplayer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pdm.chessroyale.models.Color
import pdm.chessroyale.models.pieces.Position

/**
 * Data type used to represent the game state externally, that is, when the game state crosses
 * process boundaries and device boundaries.
 */
@Parcelize
data class MultiplayerState(
    val id: String,
    val turn: Color,
    val prevPos: Position?,
    val nextPos: Position?,
    val endGame: Boolean,
    val promotedTo: Char?
): Parcelable