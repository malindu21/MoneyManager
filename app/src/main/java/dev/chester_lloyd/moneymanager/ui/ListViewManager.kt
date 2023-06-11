package dev.chester_lloyd.moneymanager.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import dev.chester_lloyd.moneymanager.*
import dev.chester_lloyd.moneymanager.ui.accounts.AccountTransactions
import dev.chester_lloyd.moneymanager.ui.accounts.TransferFunds
import dev.chester_lloyd.moneymanager.ui.categories.CategoryTransaction
import dev.chester_lloyd.moneymanager.ui.recurring_transactions.RecurringTransactionDetails
import kotlinx.android.synthetic.main.account.view.*
import kotlinx.android.synthetic.main.account.view.ivIcon
import kotlinx.android.synthetic.main.account.view.tvName
import kotlinx.android.synthetic.main.transaction.view.*

/**
 * A [BaseAdapter] subclass to manage all actions involved in setting up a generic ListView.
 *
 * @param listObjects A list of objects that will be added to the ListView.
 * @param layoutInflater layoutInflater.
 * @param context Context.
 * @param page The page that is calling this adaptor. Used to change how the lists are displayed.
 * @author Chester Lloyd
 * @since 1.2
 */
class ListViewManager(
    private val listObjects: Array<Any>,
    private val layoutInflater: LayoutInflater,
    private val context: Context,
    private val page: String
) : BaseAdapter() {

    /**
     * Creates a new row within the list view
     *
     * @param position Position of row in the ListView.
     * @param convertView A View object
     * @param parent The parent's ViewGroup
     * @return A View for a row in the ListView.
     * @suppress InflateParams as the layout is inflating without a Parent
     */
    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        when (listObjects[position]) {
            is Account -> {
                // We are adding Accounts to the list
                val rowView = layoutInflater.inflate(R.layout.account, null)
                val account = listObjects[position] as Account
                rowView.tvName.text = account.name
                rowView.tvBalance.text = MainActivity.stringBalance(context, account.balance)

                // Get the account's icon and colour
                val iconManager = IconManager(context)
                rowView.ivIcon.setImageResource(
                    iconManager.getIconByID(iconManager.accountIcons, account.icon).drawable
                )
                rowView.ivIcon.setBackgroundResource(
                    iconManager.getIconByID(iconManager.colourIcons, account.colour).drawable
                )

                // When an account is clicked
                rowView.setOnClickListener {
                    // Setup an intent to send this across to view the account's transactions
                    val intent = Intent(context, AccountTransactions::class.java)
                    val bundle = Bundle()
                    bundle.putInt("accountID", account.accountID)
                    bundle.putString("name", account.name)
                    bundle.putDouble("balance", account.balance)
                    bundle.putInt("icon", account.icon)
                    bundle.putInt("colour", account.colour)
                    bundle.putBoolean("default", account.default)
                    intent.putExtras(bundle)
                    context.startActivity(intent)
                }
                return rowView
            }

            is Category -> {
                // We are adding Categories to the list
                val rowView = layoutInflater.inflate(R.layout.category, null)
                val category = listObjects[position] as Category
                rowView.tvName.text = category.name

                // Get the category's icon and colour
                val iconManager = IconManager(context)
                rowView.ivIcon.setImageResource(
                    iconManager.getIconByID(iconManager.categoryIcons, category.icon).drawable
                )
                rowView.ivIcon.setBackgroundResource(
                    iconManager.getIconByID(iconManager.colourIcons, category.colour).drawable
                )

                // When an account is clicked
                rowView.setOnClickListener {
                    // Setup an intent to send this across to view the account's transactions
                    val intent = Intent(context, CategoryTransaction::class.java)
                    val bundle = Bundle()
                    bundle.putInt("categoryID", category.categoryID)
                    bundle.putString("name", category.name)
                    bundle.putInt("icon", category.icon)
                    bundle.putInt("colour", category.colour)
                    intent.putExtras(bundle)
                    context.startActivity(intent)
                }
                return rowView
            }

            is Payment -> {
                // We are showing transactions (Icon, name, date and amount)
                var rowView = layoutInflater.inflate(R.layout.transaction, null)
                val payment = listObjects[position] as Payment
                rowView.tvName.text = payment.transaction.merchant
                rowView.tvDate.text = MainActivity.getFormattedDate(context, payment.transaction.date)
                rowView.tvAmount.text = MainActivity.stringBalance(context, payment.amount)

                // Use IconManager to load the icons
                val iconManager = IconManager(context)
                rowView.ivIcon.setImageResource(
                    iconManager.getIconByID(
                        iconManager.categoryIcons, payment.transaction.category.icon
                    ).drawable
                )
                rowView.ivIcon.setBackgroundResource(
                    iconManager.getIconByID(
                        iconManager.colourIcons, payment.transaction.category.colour
                    ).drawable
                )

                if (page == "transaction details") {
                    // We are showing payments (icon, name and amount)
                    rowView = layoutInflater.inflate(R.layout.account, null)
                    rowView.tvName.text = payment.account.name
                    rowView.tvBalance.text = MainActivity.stringBalance(context, payment.amount)

                    rowView.ivIcon.setImageResource(
                        iconManager.getIconByID(
                            iconManager.accountIcons, payment.account.icon
                        ).drawable
                    )
                    rowView.ivIcon.setBackgroundResource(
                        iconManager.getIconByID(
                            iconManager.colourIcons, payment.account.colour
                        ).drawable
                    )
                }

                // When a a payment is clicked
                rowView.setOnClickListener {
                    var intent = Intent(context, CategoryTransaction::class.java)
                    val bundle = Bundle()
                    when (page) {
                        "transaction details" -> {
                            // We don't want an endless rabbit hole...
                            return@setOnClickListener
                        }
                        "account transactions" -> {
                            // Setup an intent to send this across to view the transaction's details
                            intent = Intent(context, TransactionDetails::class.java)
                            bundle.putInt("transactionID", payment.transaction.transactionID)
                        }
                        else -> {
                            // Setup an intent to send this across to view the category's transactions
                            bundle.putInt("categoryID", payment.transaction.category.categoryID)
                            bundle.putString("name", payment.transaction.category.name)
                            bundle.putInt("icon", payment.transaction.category.icon)
                            bundle.putInt("colour", payment.transaction.category.colour)
                        }
                    }
                    intent.putExtras(bundle)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
                return rowView
            }

            is Transaction -> {
                // We are adding Transactions to the list
                val rowView = layoutInflater.inflate(R.layout.transaction, null)
                val transaction = listObjects[position] as Transaction
                rowView.tvName.text = transaction.merchant
                rowView.tvDate.text = MainActivity.getFormattedDate(context, transaction.date)
                rowView.tvAmount.text = MainActivity.stringBalance(context, transaction.amount)

                // Show positive amounts for transfers as we're only show the one transaction
                if (transaction.transferTransactionID > 0) {
                    rowView.tvAmount.text =
                        MainActivity.stringBalance(context, (transaction.amount * -1))
                }

                // Get the transaction's category icon and colour
                val iconManager = IconManager(context)
                rowView.ivIcon.setImageResource(
                    iconManager.getIconByID(
                        iconManager.categoryIcons, transaction.category.icon
                    ).drawable
                )
                rowView.ivIcon.setBackgroundResource(
                    iconManager.getIconByID(
                        iconManager.colourIcons, transaction.category.colour
                    ).drawable
                )

                // When a transactions is clicked
                rowView.setOnClickListener {
                    // Setup an intent to send this across to view this transaction's details
                    var intent = Intent(context, TransactionDetails::class.java)
                    val bundle = Bundle()

                    if (transaction.transferTransactionID != 0) {
                        // This is a transfer, show the transfer page
                        intent = Intent(context, TransferFunds::class.java)
                    }

                    bundle.putInt("transactionID", transaction.transactionID)
                    intent.putExtras(bundle)
                    context.startActivity(intent)
                }
                return rowView
            }

            is RecurringTransaction -> {
                // We are adding Recurring Transactions to the list
                val rowView = layoutInflater.inflate(R.layout.transaction, null)
                val recurringTransaction = listObjects[position] as RecurringTransaction
                rowView.tvName.text = recurringTransaction.name
                rowView.tvDate.text = context.resources.getString(
                    R.string.transaction_recurring_every,
                    recurringTransaction.getFrequencyString()
                )
                rowView.tvAmount.text = MainActivity.stringBalance(
                    context,
                    recurringTransaction.amount
                )

                // Get the recurring transaction's category icon and colour
                val iconManager = IconManager(context)
                rowView.ivIcon.setImageResource(
                    iconManager.getIconByID(
                        iconManager.categoryIcons, recurringTransaction.category.icon
                    ).drawable
                )
                rowView.ivIcon.setBackgroundResource(
                    iconManager.getIconByID(
                        iconManager.colourIcons, recurringTransaction.category.colour
                    ).drawable
                )

                // When a recurring transaction is clicked
                rowView.setOnClickListener {
                    // Setup an intent to send this across to view this transaction's details
                    val intent = Intent(context, RecurringTransactionDetails::class.java)
                    val bundle = Bundle()
                    bundle.putInt("recurringTransactionID", recurringTransaction.recurringTransactionID)
                    intent.putExtras(bundle)
                    context.startActivity(intent)
                }
                return rowView
            }

            else -> return null
        }
    }

    /**
     * Get the object at a given [position] in the ListView.
     *
     * @param position Position of row in the ListView.
     * @return An object of the item at the given position.
     */
    override fun getItem(position: Int): Any {
        return listObjects[position]
    }

    /**
     * Get the row ID associated with the specified [position] in the list.
     *
     * @param position The position of the item within the list whose row ID we want.
     * @return The ID of the item at the specified position.
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Returns number of items in the ListView.
     *
     * @return The size of the ListView.
     */
    override fun getCount(): Int {
        return listObjects.size
    }
}