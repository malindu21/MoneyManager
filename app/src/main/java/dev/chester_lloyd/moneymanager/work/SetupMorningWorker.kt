package dev.chester_lloyd.moneymanager.work

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * A class to run in the background for a one off task. This is used to offset and enqueue the
 * [MorningWorker].
 *
 * @param appContext Context.
 * @param workerParams Parameters for a [Worker].
 * @author Chester Lloyd
 * @since 1.5
 */
class SetupMorningWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    /**
     * The work that is performed. This will enqueue the [MorningWorker].
     *
     * @return The outcome of the work completing successfully.
     */
    override fun doWork(): Result {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<MorningWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            MorningWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "dev.chester_lloyd.moneymanager.work.SetupMorningWorker"
    }
}