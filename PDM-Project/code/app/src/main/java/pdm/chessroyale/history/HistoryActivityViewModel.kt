package pdm.chessroyale.history

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pdm.chessroyale.*
import pdm.chessroyale.puzzleofday.PuzzleOfDayApplication
import pdm.chessroyale.puzzleofday.toPuzzleDTO

class HistoryActivityViewModel(application: Application) : AndroidViewModel(application) {

    var history: LiveData<List<PuzzleDTO>>? = null
        private set

    private val historyDao: HistoryPuzzleDao by lazy {
        getApplication<PuzzleOfDayApplication>().historyDb.getHistoryPuzzleDao()
    }

    /**
     * Holds a [LiveData] with the list of quotes
     */
    val puzzlesHistory: LiveData<List<PuzzleDTO>> = liveData {
        withContext(Dispatchers.IO) {
            val quotes = historyDao.getAll().map { it.toPuzzleDTO() }
            emit(quotes)
        }
    }
}