package pdm.chessroyale.models.pieces

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Position(var line: Int, var column: Int): Parcelable