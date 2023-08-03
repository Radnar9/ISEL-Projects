package pdm.chessroyale.puzzleofday

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DownloadDailyPuzzleWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app: PuzzleOfDayApplication = applicationContext as PuzzleOfDayApplication
        val repo = PuzzleOfDayRepository(app.puzzleOfDayService, app.historyDb.getHistoryPuzzleDao())

        repo.fetchQuoteOfDay(mustSaveToDB = true)
        return Result.success()
    }
}