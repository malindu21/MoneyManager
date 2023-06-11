package dev.chester_lloyd.moneymanager.work

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import dev.chester_lloyd.moneymanager.*
import dev.chester_lloyd.moneymanager.MainActivity.Companion.getTimesToday
import dev.chester_lloyd.moneymanager.ui.IconManager
import dev.chester_lloyd.moneymanager.ui.TransactionDetails
import java.util.*

/**
 * A class to run in the background each morning. This is responsible for adding recurring
 * transactions when they are due. The [doWork] method runs once a day at 6AM.
 *
 * @param appContext Context.
 * @param workerParams Parameters for a [Worker].
 * @author Chester Lloyd
 * @since 1.5
 */
@Suppress("NAME_SHADOWING")
class MorningWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    /**
     * The work that is performed. This will add all recurring transaction that occur today.
     *
     * @return The outcome of the work completing successfully.
     */
    override fun doWork(): Result {
        // Use current time for potential transaction date
        val currentDate = Calendar.getInstance()

        val dbManager = DBManager(applicationContext)
        val recurringTransactions = dbManager.selectRecurringTransactions(null, null, null)
        dbManager.sqlDB!!.close()

        for (recurringTransaction in recurringTransactions) {
            val timesToday = getTimesToday()
            if (recurringTransaction.next.timeInMillis >= timesToday[0].timeInMillis &&
                recurringTransaction.next.timeInMillis <= timesToday[1].timeInMillis &&
                recurringTransaction.next.timeInMillis <= recurringTransaction.end.timeInMillis
            ) {
                // This transaction is due to recur today, add it now
                val dbManager = DBManager(applicationContext)
                val freshRecurringTransaction =
                    dbManager.selectRecurringTransaction(recurringTransaction.recurringTransactionID)

                // Check if this one has already been handled
                if (freshRecurringTransaction.next.timeInMillis <= timesToday[1].timeInMillis) {
                    val transaction =
                        freshRecurringTransaction.createTransaction(applicationContext, currentDate)
                    freshRecurringTransaction.setNextDueDate(applicationContext)
                    dbManager.updateRecurringTransaction(
                        freshRecurringTransaction,
                        "ID=?",
                        arrayOf(freshRecurringTransaction.recurringTransactionID.toString())
                    )

                    buildNotification(freshRecurringTransaction, transaction)
                }

                dbManager.sqlDB!!.close()
            }
        }

        return Result.success()
    }

    /**
     * Method to fire a notification when a transaction has been added
     *
     * @param recurringTransaction The recurring transaction that has added the transaction
     * @param transaction The new transaction
     */
    private fun buildNotification(
        recurringTransaction: RecurringTransaction,
        transaction: Transaction
    ) {
        // Intent to load transaction details
        val intent = Intent(applicationContext, TransactionDetails::class.java)
        val bundle = Bundle()
        bundle.putInt("transactionID", transaction.transactionID)
        intent.putExtras(bundle)

        // Intent when user clicks back button
        val backIntent = Intent(applicationContext, MainActivity::class.java)
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Intents joined to on notification tap
        val pendingIntent = PendingIntent.getActivities(
            applicationContext,
            transaction.transactionID,
            arrayOf(backIntent, intent),
            PendingIntent.FLAG_ONE_SHOT
        )

        var notificationText = R.string.notification_recurring_transactions_text_income
        if (transaction.amount < 0) {
            notificationText = R.string.notification_recurring_transactions_text_outgoing
        }

        val builder =
            NotificationCompat.Builder(
                applicationContext,
                applicationContext.getString(R.string.notification_recurring_transactions_channel_id)
            )
                .setColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorPrimaryDark
                    )
                )
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(createNotificationIcon(recurringTransaction))
                .setContentTitle(applicationContext.getString(R.string.notification_recurring_transactions_title))
                .setContentText(
                    applicationContext.resources.getString(
                        notificationText,
                        MainActivity.stringBalance(
                            applicationContext,
                            recurringTransaction.amount,
                            false
                        ),
                        recurringTransaction.name
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            // ID is a unique notification ID, can be used to reference it later
            notify(transaction.transactionID, builder.build())
        }
    }

    /**
     * Creates the large notification icon of the transaction
     *
     * @param recurringTransaction The recurring transaction that has an icon and colour set.
     * @return A [Bitmap] used in the notification.
     */
    private fun createNotificationIcon(recurringTransaction: RecurringTransaction): Bitmap {
        val iconManager = IconManager(applicationContext)
        val ivNotificationIcon = ImageView(applicationContext)
        ivNotificationIcon.setImageResource(
            iconManager.getIconByID(
                iconManager.categoryIcons, recurringTransaction.category.icon
            ).drawable
        )
        ivNotificationIcon.setBackgroundResource(
            iconManager.getIconByID(
                iconManager.colourIcons, recurringTransaction.category.colour
            ).drawable
        )

        val scale = applicationContext.resources.displayMetrics.density
        val dpToPixels = (30 * scale + 0.5f).toInt()
        ivNotificationIcon.setPadding(dpToPixels, dpToPixels, dpToPixels, dpToPixels)

        ivNotificationIcon.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        ivNotificationIcon.layout(
            0, 0, ivNotificationIcon.measuredWidth, ivNotificationIcon.measuredHeight
        )

        val bitmap = Bitmap.createBitmap(
            ivNotificationIcon.measuredWidth,
            ivNotificationIcon.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val background: Drawable = ivNotificationIcon.getBackground()
        background.draw(canvas)
        ivNotificationIcon.draw(canvas)

        return bitmap
    }

    companion object {
        const val WORK_NAME = "dev.chester_lloyd.moneymanager.work.MorningWorker"
    }
}