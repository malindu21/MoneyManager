package dev.chester_lloyd.moneymanager

import android.content.Context
import android.widget.Toast
import dev.chester_lloyd.moneymanager.MainActivity.Companion.getTimesToday
import java.util.*

/**
 * A [Class] that stores all the information about a recurring transaction that the user has saved.
 *
 * @author Chester Lloyd
 * @since 1.5
 */
class RecurringTransaction {
    var recurringTransactionID: Int = 0
    var transactions = arrayListOf<Transaction>()
    var account = Account()
    var category = Category()
    var name = ""
    var amount: Double = 0.00
    var start: Calendar = Calendar.getInstance()
    var end: Calendar = Calendar.getInstance()
    var next: Calendar = Calendar.getInstance()
    var frequencyUnit: Int = 0
    var frequencyPeriod: String = ""

    /**
     * Creates a complete [RecurringTransaction] when all necessary fields have been provided.
     *
     * @param recurringTransactionID The ID of the recurring transaction.
     * @param transactions An [ArrayList] of [Transaction]s involved in this recurring transaction.
     * @param account The [Account] ID that future transactions should use.
     * @param category The [Category] ID that future transactions should use.
     * @param name The name (merchant) that future transactions should use.
     * @param amount The amount that future transactions should use.
     * @param start The date that the transaction starts to recur (date of first transaction in the
     * set).
     * @param end The date that the transaction stops recurring (date of last transaction in the
     * set).
     * @param next The date that the next transaction should be added.
     * @param frequencyUnit The integer value of how often the transaction repeats. e.g if it
     * repeats every 30 days, this would be "30".
     * @param frequencyPeriod The string value of how often the transaction repeats. e.g if it
     * repeats every 30 days, this would be "days".
     */
    constructor(
        recurringTransactionID: Int,
        transactions: ArrayList<Transaction>,
        account: Account,
        category: Category,
        name: String,
        amount: Double,
        start: Calendar,
        end: Calendar,
        next: Calendar,
        frequencyUnit: Int,
        frequencyPeriod: String
    ) {
        this.recurringTransactionID = recurringTransactionID
        this.transactions = transactions
        this.account = account
        this.category = category
        this.name = name
        this.amount = amount
        this.start = start
        this.end = end
        this.next = next
        this.frequencyUnit = frequencyUnit
        this.frequencyPeriod = frequencyPeriod
    }

    /**
     * Creates an empty [RecurringTransaction] and sets its ID to 0.
     */
    constructor() {
        this.recurringTransactionID = 0
    }

    /**
     * Returns the date this transaction is to stop recurring.
     *
     * @param context The context.
     * @return The date the transactions will stop occurring in the user specified date format.
     */
    fun getFormattedEndDate(context: Context): String {
        val now = Calendar.getInstance()
        if (end.get(Calendar.YEAR) > (now.get(Calendar.YEAR) + NO_END_DATE_YEARS_THRESHOLD)) {
            return context.getString(R.string.indefinitely)
        }

        return MainActivity.getFormattedDate(context, end)
    }

    /**
     * Sets the next date for transactions added in the future.
     */
    fun setNextDueDateFutureTransaction() {
        this.next.timeInMillis = incrementDate(this.next).timeInMillis
    }

    /**
     * Sets the next date.
     *
     * @param context The context.
     */
    fun setNextDueDate(context: Context) {
        this.next.timeInMillis = findNextDueDate(context, false).timeInMillis
    }

    /**
     * Sets the next date and creates a transaction per occurrence between first transaction and
     * today.
     *
     * @param context The context.
     */
    fun setNextDueDateAndCreateTransactions(context: Context) {
        this.next.timeInMillis = findNextDueDate(context, true).timeInMillis
    }

    /**
     * Returns the date this transaction is set to recur.
     *
     * @param context The context.
     * @return The date of the next transaction in the user specified date format.
     */
    fun getFormattedNextDueDate(context: Context): String {
        return MainActivity.getFormattedDate(context, next)
    }

    /**
     * Returns the date this transaction is set to recur based on the existing next date.
     *
     * @param context The context.
     * @param createTransactions Whether to create [Transaction]s for each occurrence between the
     * last transaction, and the next to be added.
     * @return The date of the next transaction.
     */
    private fun findNextDueDate(context: Context, createTransactions: Boolean): Calendar {
        val endOfToday = getTimesToday()[1]
        val nextDue = Calendar.getInstance()
        nextDue.timeInMillis = next.timeInMillis
        while (nextDue.timeInMillis <= endOfToday.timeInMillis) {
            nextDue.timeInMillis = incrementDate(nextDue).timeInMillis

            // Add transactions for each occurrence
            if (createTransactions && nextDue.timeInMillis <= endOfToday.timeInMillis) {
                createTransaction(context, nextDue)
            }
        }

        return nextDue
    }

    /**
     * Increments the given dated based on the recurring settings.
     *
     * @param date The date to increment.
     * @return The date incremented by one unit.
     */
    private fun incrementDate(date: Calendar): Calendar {
        when (frequencyPeriod) {
            "days" -> date.add(Calendar.DATE, frequencyUnit)
            "weeks" -> date.add(Calendar.DATE, (7 * frequencyUnit))
            "months" -> date.add(Calendar.MONTH, frequencyUnit)
            "years" -> date.add(Calendar.YEAR, frequencyUnit)
        }
        return date
    }

    /**
     * Creates a transaction based on the [RecurringTransaction] information.
     *
     * @param context The context.
     * @param date The date to add the transaction for.
     * @return The [Transaction] that has been inserted.
     */
    fun createTransaction(context: Context, date: Calendar): Transaction {
        val dbManager = DBManager(context)

        // Insert transaction
        val transaction = Transaction()
        transaction.category = this.category
        transaction.merchant = this.name
        transaction.amount = this.amount
        transaction.date = date
        val transactionID = dbManager.insertTransaction(transaction)
        transaction.transactionID = transactionID.toInt()

        // Insert payment
        val payment = Payment(
            transaction,
            this.account,
            this.amount
        )
        dbManager.insertPayment(payment)

        // Insert into recurs table
        dbManager.insertRecursRecord(
            this.recurringTransactionID,
            transaction.transactionID
        )

        dbManager.sqlDB!!.close()
        return transaction
    }

    /**
     * Returns the frequency as a string using the [frequencyUnit] and [frequencyPeriod] variables.
     * This takes into account stripping the 's' for non plural units.
     *
     * e.g "week" or "2 weeks"
     *
     * @return String
     */
    fun getFrequencyString(): String {
        var frequencyString = "${this.frequencyUnit} ${this.frequencyPeriod}"
        if (this.frequencyUnit == 1) {
            frequencyString = this.frequencyPeriod.trimEnd('s')
        }
        return frequencyString
    }

    /**
     * Validates inputs when adding a transaction.
     *
     * @param context The context.
     * @param transaction The transaction set to recur
     * @param frequencyUnit The integer value of how often the transaction repeats.
     * @return True if no errors found.
     */
    fun validateWithTransaction(
        context: Context,
        transaction: Transaction,
        frequencyUnit: Int
    ): Boolean {
        this.frequencyUnit = frequencyUnit
        when {
            this.frequencyUnit <= 0 -> {
                // Frequency unit is not valid, show an error
                Toast.makeText(
                    context, R.string.transaction_recurring_validation_unit,
                    Toast.LENGTH_SHORT
                ).show()
            }
            this.frequencyPeriod == "" -> {
                // Frequency period is not valid, show an error
                Toast.makeText(
                    context, R.string.transaction_recurring_validation_period,
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                return true
            }
        }
        return false
    }

    /**
     * Validates end date when adding a transaction.
     *
     * @param context The context.
     * @param transaction The transaction set to recur
     * @return True if no errors found.
     */
    fun validateEndDateWithTransaction(context: Context, transaction: Transaction): Boolean {
        when {
            this.end.timeInMillis < transaction.date.timeInMillis -> {
                // End date must be after the transaction date, show an error
                Toast.makeText(
                    context, R.string.transaction_recurring_validation_end_before,
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                return true
            }
        }
        return false
    }

    /**
     * Validates inputs when updating a recurring transaction
     *
     * @param context The context.
     * @return True if no errors found.
     */
    fun validateUpdate(context: Context): Boolean {
        when {
            this.name == "" -> {
                // Name is not valid, show an error
                Toast.makeText(
                    context, R.string.transaction_recurring_validation_name,
                    Toast.LENGTH_SHORT
                ).show()
            }
            this.frequencyUnit <= 0 -> {
                // Frequency unit is not valid, show an error
                Toast.makeText(
                    context, R.string.transaction_recurring_validation_unit,
                    Toast.LENGTH_SHORT
                ).show()
            }
            this.frequencyPeriod == "" -> {
                // Frequency period is not valid, show an error
                Toast.makeText(
                    context, R.string.transaction_recurring_validation_period,
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                return true
            }
        }
        return false
    }

    companion object {
        const val NO_END_DATE_YEARS = 1000
        const val NO_END_DATE_YEARS_THRESHOLD = 800
    }
}