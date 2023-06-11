package dev.chester_lloyd.moneymanager

import android.annotation.SuppressLint
import android.content.Context
import dev.chester_lloyd.moneymanager.MainActivity.Companion.TRANSFER_CATEGORY_ID
import dev.chester_lloyd.moneymanager.ui.IconManager
import lecho.lib.hellocharts.model.SliceValue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

/**
 * A [DBManager] subclass to handle collecting data from the database when creating pie charts.
 *
 * @param context Context.
 * @author Chester Lloyd
 * @since 1.0
 */
class PieManager(private val context: Context) : DBManager(context) {

    val iconManager = IconManager(context)

    /**
     * Gets the total amount for each category for a given month.
     *
     * @param month The month to get data for.
     * @param direction Can be In for income or Out for expenses.
     * @return [SliceValue] data that can be used to construct a pie chart.
     */
    fun categoryMonth(month: String, direction: String): ArrayList<SliceValue> {
        val pieData = ArrayList<SliceValue>()
        var selectionArgs = arrayOfNulls<String>(0)
        var query = "SELECT P.${colID}, P.${colTransactionID}, " +
                "P.${colAccountID}, P.${colAmount}, SUM(P.${colAmount}) AS total FROM $dbPaymentsTable P " +
                "JOIN $dbTransactionTable T ON T.${colID} = P.${colTransactionID} " +
                "JOIN $dbAccountTable A ON A.${colID} = P.${colAccountID} "

        when (direction) {
            "in" -> query += "WHERE P.${colAmount} > 0 "
            "out" -> query += "WHERE P.${colAmount} < 0 "
        }

        if (month != "all") {
            query += "AND strftime('%m', T.${colDate}) = ? "
            selectionArgs = arrayOf(month)
        }

        query += "AND T.${colCategoryID} != $TRANSFER_CATEGORY_ID " +
                 "GROUP BY T.${colCategoryID} ORDER BY total DESC"

        val cursor = sqlDB!!.rawQuery(query, selectionArgs)
        if (cursor.moveToFirst()) {
            do {
                val transactionID = cursor.getInt(cursor.getColumnIndex(colTransactionID))
                val accountID = cursor.getInt(cursor.getColumnIndex(colAccountID))
                val amount = cursor.getDouble(cursor.getColumnIndex("total"))
                val payment = Payment(
                    selectTransaction(transactionID), selectAccount(accountID),
                    amount
                )

                pieData.add(
                    SliceValue(
                        (payment.amount.absoluteValue).toFloat(),
                        (iconManager.getIconByID(
                            iconManager.colourIcons,
                            payment.transaction.category.colour
                        ).colour!!)
                    ).setLabel(
                        payment.transaction.category.name + " " +
                                MainActivity.stringBalance(context, payment.amount)
                    )
                )

            } while (cursor.moveToNext())
        }
        cursor.close()
        return pieData
    }

    /**
     * Gets all the months that are present between the first transaction and the current date.
     *
     * @return [Calendar] for each month that is found.
     */
    @SuppressLint("SimpleDateFormat")
    fun getAllMonths(): ArrayList<Calendar> {
        val dates = ArrayList<Calendar>()

        // Get first transaction date
        val firstTransaction = Calendar.getInstance()
        val selectionArgs = arrayOfNulls<String>(0)
        val query = "SELECT T.${colDate} FROM $dbTransactionTable T WHERE T.${colAmount} != 0 " +
                    "ORDER BY T.${colDate} ASC LIMIT 1"
        val cursor = sqlDB!!.rawQuery(query, selectionArgs)
        if (cursor.moveToFirst()) {
            val date = cursor.getString(cursor.getColumnIndex(colDate))
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            firstTransaction.time = sdf.parse(date)
        }
        cursor.close()

        /*
         * If looping date is before the current date, add it to the array.
         * Start from first transaction to 1 month prior to today so add this after.
         * This will add older dates first so reverse the array at the end.
         */
        val now = Calendar.getInstance()
        while (firstTransaction.time.before(now.time)) {
            val cal = Calendar.getInstance()
            cal.set(
                firstTransaction.get(Calendar.YEAR),
                (SimpleDateFormat("MM").format(firstTransaction.time).toInt() - 1),
                1
            )
            dates.add(cal)
            firstTransaction.add(Calendar.MONTH, 1)
        }

        if (dates.isEmpty()) {
            dates.add(now)
        } else if (dates[dates.lastIndex].get(Calendar.MONTH) != now.get(Calendar.MONTH)) {
            dates.add(now)
        }
        dates.reverse()
        return dates
    }
}