package dev.chester_lloyd.moneymanager.ui.recurring_transactions

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import dev.chester_lloyd.moneymanager.DBManager
import dev.chester_lloyd.moneymanager.MainActivity
import dev.chester_lloyd.moneymanager.R
import dev.chester_lloyd.moneymanager.RecurringTransaction
import dev.chester_lloyd.moneymanager.ui.IconManager
import dev.chester_lloyd.moneymanager.ui.ListViewManager
import kotlinx.android.synthetic.main.activity_recurring_transaction_details.*

/**
 * An [AppCompatActivity] subclass to show the transactions for a given category. This also displays
 * options to edit and delete the [category].
 *
 * @author Chester Lloyd
 * @since 1.5
 */
class RecurringTransactionDetails : AppCompatActivity() {

    private var recurringTransaction = RecurringTransaction()

    /**
     * An [onCreate] method that sets up the supportActionBar, [recurringTransaction] and view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recurring_transaction_details)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.menu_recurring_transactions)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recurringTransaction.recurringTransactionID =
            intent.getIntExtra("recurringTransactionID", 0)
    }

    /**
     * An [onResume] method that adds all recurring transaction information to the view to show
     * any potential changes if the page is reloaded.
     */
    override fun onResume() {
        super.onResume()

        if (recurringTransaction.recurringTransactionID > 0) {
            // Read current recurring transaction data from database
            val dbManager = DBManager(this)
            recurringTransaction =
                dbManager.selectRecurringTransaction(recurringTransaction.recurringTransactionID)
            dbManager.sqlDB!!.close()
        }

        // Update top transaction with info
        tvName.text = recurringTransaction.name
        tvAmount.text =
            MainActivity.stringBalance(this, recurringTransaction.amount)

        val iconManager = IconManager(this)
        ivIcon.setImageResource(
            iconManager.getIconByID(
                iconManager.categoryIcons, recurringTransaction.category.icon
            ).drawable
        )
        ivIcon.setBackgroundResource(
            iconManager.getIconByID(
                iconManager.colourIcons, recurringTransaction.category.colour
            ).drawable
        )

        // Update details with info
        tvFrequency.text = this.getString(
            R.string.frequency_repeats_every,
            recurringTransaction.getFrequencyString()
        )
        tvNextDue.text = recurringTransaction.getFormattedNextDueDate(this)
        tvUntil.text = recurringTransaction.getFormattedEndDate(this)

        // Pass this to the list view adaptor and populate
        this.lvTransactions.adapter = ListViewManager(
            recurringTransaction.transactions.toTypedArray(),
            layoutInflater,
            this,
            "recurring transaction"
        )
    }

    /**
     * An [onCreateOptionsMenu] method that adds the edit menu to the toolbar. This includes an
     * edit and delete button.
     *
     * @param menu The options menu to place items.
     * @return True to display the menu, or false to not show the menu.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * An [onOptionsItemSelected] method that adds functionality when the menu buttons are clicked.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menuEdit -> {
            // Edit icon clicked, go to edit page (pass all recurring transaction details)
            val intent = Intent(this, EditRecurringTransaction::class.java)

            val bundle = Bundle()
            bundle.putInt("recurringTransactionID", recurringTransaction.recurringTransactionID)
            intent.putExtras(bundle)

            startActivity(intent)
            true
        }
        R.id.menuDelete -> {
            // Delete icon clicked, build an alert dialog to get user confirmation
            val alertDialog = AlertDialog.Builder(this)

            alertDialog.setMessage(resources.getString(R.string.alert_message_delete_recurring_transaction))
                .setCancelable(true)
                .setPositiveButton(resources.getString(R.string.yes)) { dialog, id ->
                    finish()
                    // Delete the recurring transaction
                    val dbManager = DBManager(this)
                    dbManager.deleteRecurringTransaction(recurringTransaction.recurringTransactionID)
                    dbManager.sqlDB!!.close()
                }
                .setNegativeButton(resources.getString(R.string.no_cancel)) {
                    // Do nothing, close box
                        dialog, _ ->
                    dialog.cancel()
                }

            val alert = alertDialog.create()
            alert.setTitle(resources.getString(R.string.alert_title_delete_recurring_transaction))
            alert.show()
            true
        }
        else -> {
            // Unknown action (not edit or delete) invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * An [onOptionsItemSelected] method that closes this activity (goes to previous page) once
     * toolbar back button is pressed.
     *
     * @return true if Up navigation completed successfully and this Activity was finished, false
     * otherwise.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
