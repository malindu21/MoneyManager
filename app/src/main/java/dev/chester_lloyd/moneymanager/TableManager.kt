package dev.chester_lloyd.moneymanager

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import dev.chester_lloyd.moneymanager.MainActivity.Companion.TRANSFER_CATEGORY_ID
import lecho.lib.hellocharts.model.SliceValue

/**
 * A [DBManager] subclass to handle creating tables and collecting data from the database.
 *
 * @param context Context.
 * @author Chester Lloyd
 * @since 1.1
 */
class TableManager(private val context: Context) : DBManager(context) {

    private val tableLayout by lazy { TableLayout(context) }

    /**
     * Gets the total amount for each category for a given month.
     *
     * @param month The month to get data for.
     * @param direction Can be In for income or Out for expenses.
     * @return [SliceValue] data that can be used to construct a pie chart.
     */
    fun transactionTotal(month: String, year: String, direction: String, yearTotal: Boolean = false): Double {
        var total = 0.0
        val selectionArgs: Array<String?>
        var query = "SELECT SUM(T.${colAmount}) AS total FROM $dbTransactionTable T "

        when (direction) {
            "in" -> query += "WHERE T.${colAmount} > 0 "
            "out" -> query += "WHERE T.${colAmount} < 0 "
        }

        if (yearTotal) {
            query += "AND strftime('%Y', T.${colDate}) = ? "
            selectionArgs = arrayOf(year)
        } else {
            query += "AND strftime('%m', T.${colDate}) = ? AND strftime('%Y', T.${colDate}) = ? "
            selectionArgs = arrayOf(month, year)
        }
        query += "AND T.${colCategoryID} != $TRANSFER_CATEGORY_ID "

        val cursor = sqlDB!!.rawQuery(query, selectionArgs)
        if (cursor.moveToFirst()) {
            do {
                total = cursor.getDouble(cursor.getColumnIndex("total"))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return total
    }

    /**
     * Creates a [TableLayout] to hold the table
     *
     * @return TableLayout.
     */
    fun createTable(): TableLayout {
        val tableLayoutParams = TableLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        tableLayout.apply {
            layoutParams = tableLayoutParams
        }
        return tableLayout
    }

    /**
     * Adds data from a given array to a new row in the provided table
     *
     * @param table The [TableLayout] to add the new [data] to.
     * @param data The data to add as a row. Each item in the [ArrayList] is a column in the row.
     */
    fun addTableRow(table: TableLayout, data: ArrayList<String>, header: Boolean, activity: Activity) {
        // Make a row
        val row = TableRow(context)
        row.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Add given data to the row
        for (index in data.indices) {
            val textBox = TextView(context)

            var paddingRight = 40
            if (index == data.size) {
                paddingRight = 0
            }
            textBox.apply {
                text = data[index]
                textSize = 18F
                setPadding(0, 10, paddingRight, 10)
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }
            if (header) {
                textBox.textSize = 28F
            }

            row.addView(textBox)
        }

        // Add row to table
        table.addView(row)
    }
}