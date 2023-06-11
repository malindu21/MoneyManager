package dev.chester_lloyd.moneymanager.ui.accounts

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import dev.chester_lloyd.moneymanager.*
import dev.chester_lloyd.moneymanager.ui.IconManager
import dev.chester_lloyd.moneymanager.ui.ListViewManager
import kotlinx.android.synthetic.main.activity_account_transactions.*
import kotlinx.android.synthetic.main.activity_account_transactions.ivIcon
import kotlinx.android.synthetic.main.activity_account_transactions.tvName

/**
 * An [AppCompatActivity] subclass to show the transactions for a given account. This also displays
 * options to edit and delete the [account].
 *
 * @author Chester Lloyd
 * @since 1.0
 */
@Suppress("NAME_SHADOWING")
class AccountTransactions : AppCompatActivity() {

    private var account = Account()

    /**
     * An [onCreate] method that sets up the supportActionBar, [account] and view
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_transactions)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.manage_account)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        account = Account(
            intent.getIntExtra("accountID", 0),
            intent.getStringExtra("name")!!,
            intent.getDoubleExtra("balance", 0.0),
            intent.getIntExtra("icon", 0),
            intent.getIntExtra("colour", 0),
            intent.getBooleanExtra("default", false)
        )

        tvName.text = account.name
        tvBalance.text = MainActivity.stringBalance(this, account.balance)

        val iconManager = IconManager(this)
        ivIcon.setImageResource(
            iconManager.getIconByID(
                iconManager.accountIcons, account.icon
            ).drawable
        )
        ivIcon.setBackgroundResource(
            iconManager.getIconByID(
                iconManager.colourIcons, account.colour
            ).drawable
        )
    }

    /**
     * An [onResume] method that adds all account transactions to a ListView and updates the view
     * to show any potential changes if the page is reloaded
     */
    override fun onResume() {
        super.onResume()
        val dbManager = DBManager(this)

        if (intent.getIntExtra("accountID", 0) > 0) {
            // Read current account from database
            account = dbManager.selectAccount(intent.getIntExtra("accountID", 0))
        }

        // Update entry fields with account info
        tvName.text = account.name
        tvBalance.text = MainActivity.stringBalance(this, account.balance)
        val iconManager = IconManager(this)
        ivIcon.setImageResource(
            iconManager.getIconByID(
                iconManager.accountIcons, account.icon
            ).drawable
        )
        ivIcon.setBackgroundResource(
            iconManager.getIconByID(
                iconManager.colourIcons, account.colour
            ).drawable
        )

        // Get transactions (as payments) as an array list from database
        val listPayments = dbManager
            .selectPayments(account.accountID, "accounts")
        dbManager.sqlDB!!.close()

        // Pass this to the list view adaptor and populate
        this.lvTransactions.adapter = ListViewManager(
            listPayments.toTypedArray(),
            layoutInflater,
            applicationContext,
            "account transactions"
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
            // Edit icon clicked, go to edit page (pass all account details)
            val intent = Intent(this, AddAccount::class.java)

            val bundle = Bundle()
            bundle.putInt("accountID", account.accountID)
            bundle.putString("name", account.name)
            bundle.putDouble("balance", account.balance)
            bundle.putInt("icon", account.icon)
            bundle.putInt("colour", account.colour)
            bundle.putBoolean("default", account.default)
            intent.putExtras(bundle)

            startActivity(intent)
            true
        }
        R.id.menuDelete -> {
            // Delete icon clicked, determine if account involves recurring transactions
            val dbManager = DBManager(applicationContext)
            val recurringTransactions =
                dbManager.selectRecurringTransactions(null, account.accountID, null)
            dbManager.sqlDB!!.close()

            // Build an alert dialog to get user confirmation
            val alertDialog = AlertDialog.Builder(this)

            if (recurringTransactions.size > 0) {
                alertDialog.setMessage(
                    resources.getString(
                        R.string.alert_message_delete_account_contains_recurring_transactions
                    )
                )
            } else {
                alertDialog.setMessage(resources.getString(R.string.alert_message_delete_account))
            }
            alertDialog
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    finish()
                    // Delete all recurring transactions
                    val dbManager = DBManager(applicationContext)
                    for (recurringTransaction in recurringTransactions) {
                        dbManager.deleteRecurringTransaction(recurringTransaction.recurringTransactionID)
                    }

                    // Delete the account
                    dbManager.delete(
                        dbManager.dbAccountTable,
                        arrayOf(account.accountID.toString())
                    )
                    dbManager.sqlDB!!.close()
                }
                .setNegativeButton(resources.getString(R.string.no_cancel)) {
                    // Do nothing, close box
                        dialog, _ ->
                    dialog.cancel()
                }

            val alert = alertDialog.create()
            alert.setTitle(resources.getString(R.string.alert_title_delete_account))
            alert.show()
            true
        }
        else -> {
            // Unknown action (not edit or delete) invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * An [onSupportNavigateUp] method that closes this activity (goes to previous page) once
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
