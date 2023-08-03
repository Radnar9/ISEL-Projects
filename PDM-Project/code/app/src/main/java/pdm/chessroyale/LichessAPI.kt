package pdm.chessroyale

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import retrofit2.Call
import retrofit2.http.GET

const val URL = "https://lichess.org/api/"

@Parcelize
data class Game(val pgn: String) : Parcelable

@Parcelize
data class Puzzle(val solution: List<String>) : Parcelable

@Parcelize
data class PuzzleInfo(val game: Game, val puzzle: Puzzle) : Parcelable

@Parcelize
data class PuzzleDTO(val puzzleInfo: PuzzleInfo, val date: String, var isCompleted: Boolean) :
    Parcelable

interface DailyPuzzleService {
    @GET("puzzle/daily")
    fun getPuzzle(): Call<PuzzleInfo>
}

class ServiceUnavailable(message: String = "", cause: Throwable? = null) : Exception(message, cause)