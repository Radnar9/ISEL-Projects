package pdm.chessroyale.puzzleofday

import android.app.Application
import androidx.room.Room
import androidx.work.*
import com.google.gson.Gson
import pdm.chessroyale.DailyPuzzleService
import pdm.chessroyale.URL
import pdm.chessroyale.challenges.ChallengesRepository
import pdm.chessroyale.multiplayer.MultiplayerRepository
import pdm.chessroyale.history.HistoryDatabase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PuzzleOfDayApplication : Application() {
    val puzzleOfDayService: DailyPuzzleService by lazy {
        Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DailyPuzzleService::class.java)
    }

    val historyDb: HistoryDatabase by lazy {
        Room.inMemoryDatabaseBuilder(this, HistoryDatabase::class.java).build()
    }

    private val mapper: Gson by lazy { Gson() }

    /**
     * The challenges' repository
     */
    val challengesRepository: ChallengesRepository by lazy { ChallengesRepository() }

    /**
     * The multiplayer's repository
     */
     val multiplayerRepository: MultiplayerRepository by lazy { MultiplayerRepository(mapper) }

    override fun onCreate() {
        super.onCreate()

//        historyDb.getHistoryPuzzleDao().insert(
//            PuzzleEntity(
//                id = "2021-11-09",
//                puzzle = "e4 e6 d4 d5 Nc3 Nf6 Bg5 h6 Bxf6 Qxf6 " +
//                    "exd5 Bb4 Bc4 O-O Ne2 exd5 Bxd5 Bxc3+ bxc3 Rd8 Bf3 c5 O-O Nc6 Re1 Be6 Bxc6 " +
//                    "bxc6 Qd2 Rab8 h3 Rb2 a3 Bc4 Qc1 Rdb8 dxc5 Bxe2 Rxe2 Qxc3 Qf4 Qxc5 Rae1 Rf8 " +
//                    "Re8 Qxa3",
//                solution = "e1e7 a3e7 e8e7",
//                isCompleted = false)
//        )

        val workRequest = PeriodicWorkRequestBuilder<DownloadDailyPuzzleWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "DownloadDailyQuote",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
}