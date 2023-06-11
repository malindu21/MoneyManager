package dev.chester_lloyd.moneymanager.ui

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.chester_lloyd.moneymanager.*
import dev.chester_lloyd.moneymanager.ui.accounts.TransferFunds
import dev.chester_lloyd.moneymanager.ui.transactions.AddTransaction
import kotlinx.android.synthetic.main.activity_transation_details.*

/**
 * An [AppCompatActivity] subclass to show the payments for a transaction. This also displays
 * options to edit and delete the [Transaction].
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class TransactionDetails : AppCompatActivity() {

    private var transaction = Transaction()

    /**
     * An [onCreate] method that sets up the supportActionBar, [transaction] and view
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transation_details)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.transaction_details)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val dbManager = DBManager(this)
        transaction = dbManager.selectTransaction(
            intent
                .getIntExtra("transactionID", 0)
        )
        dbManager.sqlDB!!.close()

        tvName.text = transaction.merchant
        tvAmount.text = MainActivity.stringBalance(this, transaction.amount)

        val iconManager = IconManager(this)
        ivIcon.setImageResource(
            iconManager.getIconByID(
                iconManager.categoryIcons, transaction.category.icon
            ).drawable
        )
        ivIcon.setBackgroundResource(
            iconManager.getIconByID(
                iconManager.colourIcons, transaction.category.colour
            ).drawable
        )

        // Cancel relevant recurring transaction notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(transaction.transactionID)
        }
    }

//  If we have come back (after updating) show potential updated account status
    /**
     * An [onResume] method that adds all of the transaction's payments to a ListView and updates
     * the view to show any potential changes if the page is reloaded
     */
    override fun onResume() {
        super.onResume()
        val dbManager = DBManager(this)

        if (intent.getIntExtra("transactionID", 0) > 0) {
            // Read current transaction from database
            transaction = dbManager.selectTransaction(
                intent
                    .getIntExtra("transactionID", 0)
            )
        }

        // Update entry fields with account info
        tvName.text = transaction.merchant
        tvAmount.text = MainActivity.stringBalance(this, transaction.amount)
        val iconManager = IconManager(this)
        ivIcon.setImageResource(
            iconManager.getIconByID(
                iconManager.categoryIcons, transaction.category.icon
            ).drawable
        )
        ivIcon.setBackgroundResource(
            iconManager.getIconByID(
                iconManager.colourIcons, transaction.category.colour
            ).drawable
        )

        // Show details text if user has written it
        if (transaction.details != null) {
            tvDetails.text = transaction.details
            tvDetails.visibility = View.VISIBLE
        }

        // Get payments as an array list from database
        val listPayments = dbManager
            .selectPayments(transaction.transactionID, "transaction")

        // Pass this to the list view adaptor and populate
        this.lvPayments.adapter = ListViewManager(
            listPayments.toTypedArray(),
            layoutInflater,
            applicationContext,
            "transaction details"
        )

        dbManager.sqlDB!!.close()
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
            var intent = Intent(this, AddTransaction::class.java)
            val bundle = Bundle()

            if (transaction.transferTransactionID != 0) {
                // This is a transfer, show the transfer page
                intent = Intent(this, TransferFunds::class.java)
            }

            bundle.putInt("transactionID", transaction.transactionID)
            intent.putExtras(bundle)
            startActivity(intent)
            true
        }
        R.id.menuDelete -> {
            // Delete icon clicked, build an alert dialog to get user confirmation
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage(resources.getString(R.string.alert_message_delete_transaction))
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    finish()
                    // Delete the transaction
                    DBManager(this).deleteTransaction(
                        arrayOf(transaction.transactionID.toString())
                    )
                }
                .setNegativeButton(resources.getString(R.string.no_cancel)) {
                    // Do nothing, close box
                        dialog, _ ->
                    dialog.cancel()
                }

            val alert = alertDialog.create()
            alert.setTitle(resources.getString(R.string.alert_title_delete_transaction))
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
