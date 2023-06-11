package dev.chester_lloyd.moneymanager.ui.accounts

import android.app.AlertDialog
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import dev.chester_lloyd.moneymanager.*
import dev.chester_lloyd.moneymanager.MainActivity.Companion.TRANSFER_CATEGORY_ID
import dev.chester_lloyd.moneymanager.ui.CurrencyValidator
import dev.chester_lloyd.moneymanager.ui.Icon
import dev.chester_lloyd.moneymanager.ui.IconManager
import dev.chester_lloyd.moneymanager.ui.IconSpinner
import kotlinx.android.synthetic.main.activity_add_transaction.*
import kotlinx.android.synthetic.main.activity_transfer_funds.*
import kotlinx.android.synthetic.main.activity_transfer_funds.etAmount
import kotlinx.android.synthetic.main.activity_transfer_funds.etDate
import kotlinx.android.synthetic.main.activity_transfer_funds.tvDesc
import kotlinx.android.synthetic.main.activity_transfer_funds.tvSuffix
import kotlinx.android.synthetic.main.activity_transfer_funds.tvSymbol
import java.text.SimpleDateFormat
import java.util.*

/**
 * An [AppCompatActivity] subclass to transfer funds between [Account]s.
 *
 * @author Chester Lloyd
 * @since 1.2
 */
@Suppress("NAME_SHADOWING")
class TransferFunds : AppCompatActivity() {

    private var transferDate: Calendar = Calendar.getInstance()
    private var accountSource = Account()
    private var accountDestination = Account()
    private var transactionSource = Transaction()
    private var transactionDestination = Transaction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.hideInMultitasking(window, applicationContext)
        setContentView(R.layout.activity_transfer_funds)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.transfer_funds)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Add the currency symbol and suffix to the amount row
        val format = MainActivity.getCurrencyFormat(this)
        tvSymbol.text = format[0]
        tvSuffix.text = format[3]

        // Setup the date to the current device date
        val etDate = this.etDate
        updateDateInView()

        val dbManager = DBManager(this)

        // Get transaction from database, if ID given (to edit)
        val transactionID = intent.getIntExtra("transactionID", 0)

        if (transactionID > 0) {
            transactionSource = dbManager.selectTransaction(transactionID)
            transactionDestination =
                dbManager.selectTransaction(transactionSource.transferTransactionID)
            accountSource = dbManager.getAccountByTransaction(transactionSource)!!
            accountDestination = dbManager.getAccountByTransaction(transactionDestination)!!

            transferDate = transactionSource.date
            updateDateInView()
        }

        // Create a date picker, set values for class date value
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                transferDate.set(Calendar.YEAR, year)
                transferDate.set(Calendar.MONTH, monthOfYear)
                transferDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        // When the date edit text has focus (clicked), open the date picker
        etDate.onFocusChangeListener = View.OnFocusChangeListener { _, gainFocus ->
            if (gainFocus) {
                DatePickerDialog(
                    this@TransferFunds,
                    dateSetListener,
                    // set to point to today's date when it loads up
                    transferDate.get(Calendar.YEAR),
                    transferDate.get(Calendar.MONTH),
                    transferDate.get(Calendar.DAY_OF_MONTH)
                ).show()
                etDate.clearFocus()
            }
        }

        // Setup the source and destination icon spinners
        val accounts: ArrayList<Account> = dbManager.selectAccounts("active", null)

        // If we are editing a transaction, make sure to add any deleted accounts that were involved
        if (transactionID > 0) {
            var accountSourcePresent = false
            var accountDestinationPresent = false
            for (account in 0 until accounts.size) {
                if (accounts[account].accountID == accountSource.accountID) {
                    accountSourcePresent = true
                } else if (accounts[account].accountID == accountDestination.accountID) {
                    accountDestinationPresent = true
                }
            }

            if (!accountSourcePresent) {
                accounts.add(accountSource)
            }
            if (!accountDestinationPresent) {
                accounts.add(accountDestination)
            }
        }

        val iconManager = IconManager(this)
        val icons = arrayOfNulls<Icon>(accounts.size)
        val backgrounds = arrayOfNulls<Icon>(accounts.size)
        val balances = arrayOfNulls<String>(accounts.size)

        for (account in 0 until accounts.size) {
            icons[account] = Icon(
                account,
                iconManager.getIconByID(
                    iconManager.accountIcons,
                    accounts[account].icon
                ).drawable,
                accounts[account].name!!,
                accounts[account].colour
            )

            backgrounds[account] = Icon(
                account,
                iconManager.getIconByID(
                    iconManager.colourIcons,
                    accounts[account].colour
                ).drawable,
                "",
                null
            )

            balances[account] = MainActivity.stringBalance(this, accounts[account].balance)
        }

        val sourceSpinner = findViewById<Spinner>(R.id.spSource)
        sourceSpinner.adapter = IconSpinner(
            applicationContext, icons.requireNoNulls(), backgrounds.requireNoNulls(), balances.requireNoNulls(), "account"
        )
        val destinationSpinner = findViewById<Spinner>(R.id.spDestination)
        destinationSpinner.adapter = IconSpinner(
            applicationContext, icons.requireNoNulls(), backgrounds.requireNoNulls(), balances.requireNoNulls(), "account"
        )

        // Add selected account to transaction source/destination object
        spSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                accountSource = accounts[position]
            }
        }
        spDestination.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                accountDestination = accounts[position]
            }
        }

        // Validate the amount field
        val amountValidator = CurrencyValidator(etAmount)
        etAmount.keyListener = DigitsKeyListener.getInstance("0123456789${format[2]}")
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Update balance before changes have been made (i.e user changed it)
                amountValidator.beforeTextChangedListener(s)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                amountValidator.onTextChangedListener(s, format[2])
            }
        })

        if (transactionID > 0) {
            this.supportActionBar?.title = getString(R.string.edit_transaction)
            tvDesc.setText(R.string.text_edit_transaction_desc)
            etAmount.setText(CurrencyValidator.getEditTextAmount(transactionSource.amount, format[2]))
            updateDateInView()

            for (account in 0 until accounts.size) {
                if (accounts[account].accountID == accountSource.accountID) {
                    spSource.setSelection(account)
                } else if (accounts[account].accountID == accountDestination.accountID) {
                    spDestination.setSelection(account)
                }
            }
        }

        // Save or update the transactions on FAB click
        fabTransferFunds.setOnClickListener {
            if (etAmount.text.toString() == "") {
                // Transaction amount is empty, show an error
                Toast.makeText(
                    this, R.string.transaction_validation_amount,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (etAmount.text.toString() == format[2]) {
                // Transaction amount is the decimal sign only, show an error
                Toast.makeText(
                    this, R.string.transaction_validation_amount_invalid,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (amountValidator.getBalance(format[2]) == 0.0) {
                // Transaction amount is zero (or the currency equivalent), show an error
                Toast.makeText(
                    this, R.string.transaction_validation_amount_zero,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (etDate.text.toString() == "") {
                // Transaction date is empty, show an error
                Toast.makeText(
                    this, R.string.transaction_validation_date,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (accountSource.accountID == accountDestination.accountID) {
                // Source and destination are the same, show an error
                Toast.makeText(
                    this, R.string.transfer_same_accounts,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // All data has been filled out, start saving
                val dbManager = DBManager(this)

                // Update source transaction with selected details
                transactionSource.category = dbManager.selectCategory(TRANSFER_CATEGORY_ID)
                transactionSource.merchant = this.resources.getString(
                    R.string.transfer_to,
                    accountSource.name,
                    accountDestination.name
                )
                transactionSource.details = this.resources.getString(
                    R.string.transfer_from_to,
                    accountSource.name,
                    accountDestination.name
                )
                transactionSource.amount = (amountValidator.getBalance(format[2]) * -1)
                transactionSource.date = transferDate

                // Update destination transaction with selected details
                transactionDestination.category = transactionSource.category
                transactionDestination.merchant = transactionSource.merchant
                transactionDestination.details = transactionSource.details
                transactionDestination.amount = (transactionSource.amount * -1)
                transactionDestination.date = transactionSource.date

                var saveSuccess = false
                if (transactionSource.transactionID == 0) {
                    // Insert two new transactions into the transactions table for the transfer
                    val idSource = dbManager.insertTransaction(transactionSource)
                    if (idSource > 0) {
                        transactionSource.transactionID = idSource.toInt()

                        // Update destination transaction with ID of newly saved source transaction
                        transactionDestination.transferTransactionID = idSource.toInt()

                        val idDest = dbManager.insertTransaction(transactionDestination)
                        if (idDest > 0) {
                            transactionDestination.transactionID = idDest.toInt()

                            // Update source transaction with ID of newly saved destination transaction
                            transactionSource.transferTransactionID =
                                transactionDestination.transactionID
                            val selectionArgs = arrayOf(transactionSource.transactionID.toString())
                            dbManager.updateTransaction(
                                transactionSource, "ID=?",
                                selectionArgs
                            )

                            // Add source and destination payments
                            dbManager.insertPayment(
                                Payment(
                                    transactionSource,
                                    accountSource,
                                    transactionSource.amount
                                )
                            )
                            dbManager.insertPayment(
                                Payment(
                                    transactionDestination,
                                    accountDestination,
                                    transactionDestination.amount
                                )
                            )
                            saveSuccess = true
                            dbManager.sqlDB!!.close()

                            // Transaction saved to database, return to previous fragment
                            Toast.makeText(
                                this, R.string.transaction_insert_success,
                                Toast.LENGTH_LONG
                            ).show()
                            setResult(RESULT_OK)
                            this.finish()
                        }
                    }
                    if (!saveSuccess) {
                        dbManager.sqlDB!!.close()
                        // Failed to save, show this error
                        Toast.makeText(
                            this, R.string.transaction_insert_fail,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // Update this transfer in the database
                    val paymentSource = dbManager.selectPayments(
                        transactionSource.transactionID,
                        "transaction"
                    )[0]
                    val originalAccountSourceID = paymentSource.account.accountID
                    val originalSourceAmount = paymentSource.amount
                    println("Original src account $originalAccountSourceID")
                    println("Original payment src of ${paymentSource.amount} from " +
                            "${paymentSource.account.name} " +
                            "(balance ${paymentSource.account.balance})")

                    if (originalAccountSourceID != accountSource.accountID) {
                        println("New src account ${accountSource.accountID}")
                        // If different source, reimburse the account
                        paymentSource.account.balance =
                            paymentSource.account.balance - paymentSource.amount //- a -ve payment
                        println("Correcting previous src balance to ${paymentSource.account.balance}")
                        dbManager.updateAccount(
                            paymentSource.account,
                            "ID = ?",
                            arrayOf(paymentSource.account.accountID.toString())
                        )
                    }
                    paymentSource.account = accountSource
                    paymentSource.amount = transactionSource.amount

                    val paymentDestination = dbManager.selectPayments(
                        transactionDestination.transactionID,
                        "transaction"
                    )[0]
                    val originalAccountDestinationID = paymentDestination.account.accountID
                    val originalDestinationAmount = paymentDestination.amount
                    println("Original dst account $originalAccountDestinationID")
                    println("Original payment dst of ${paymentDestination.amount} to " +
                            "${paymentDestination.account.name} " +
                            "(balance ${paymentDestination.account.balance})")

                    if (originalAccountDestinationID != accountDestination.accountID) {
                        println("New dst account ${accountDestination.accountID}")
                        // If different destination, remove the amount from the account
                        paymentDestination.account.balance =
                            paymentDestination.account.balance - paymentDestination.amount
                        println("Correcting previous dst balance to ${paymentDestination.account.balance}")
                        dbManager.updateAccount(
                            paymentDestination.account,
                            "ID = ?",
                            arrayOf(paymentDestination.account.accountID.toString())
                        )
                    }
                    paymentDestination.account = accountDestination
                    paymentDestination.amount = transactionDestination.amount

                    val selectionArgsSource = arrayOf(transactionSource.transactionID.toString())
                    val idSource = dbManager.updateTransaction(
                        transactionSource, "ID=?",
                        selectionArgsSource
                    )
                    if (idSource > 0) {
                        // Update the source payment
                        dbManager.updatePaymentTransfer(
                            originalAccountSourceID,
                            originalSourceAmount,
                            paymentSource,
                            paymentSource.account.accountID == originalAccountDestinationID
                        )

                        val selectionArgsDest = arrayOf(transactionDestination.transactionID.toString())
                        val idDest = dbManager.updateTransaction(
                            transactionDestination, "ID=?",
                            selectionArgsDest
                        )
                        if (idDest > 0) {
                            // Update the destination payment
                            dbManager.updatePaymentTransfer(
                                originalAccountDestinationID,
                                originalDestinationAmount,
                                paymentDestination,
                                paymentDestination.account.accountID == originalAccountSourceID
                            )

                            saveSuccess = true
                            dbManager.sqlDB!!.close()

                            // Transactions updated in the database, return to previous fragment
                            Toast.makeText(
                                this, R.string.transaction_update_success,
                                Toast.LENGTH_LONG
                            ).show()
                            setResult(RESULT_OK)
                            this.finish()
                        }

                        if (!saveSuccess) {
                            // Failed to update, show this error
                            Toast.makeText(
                                this, R.string.transaction_update_fail,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
        dbManager.sqlDB!!.close()
    }

    /**
     * Update [etDate] value with the class date variable.
     */
    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
        etDate!!.setText(sdf.format(transferDate.time))
    }

    /**
     * An [onCreateOptionsMenu] method that adds the edit menu to the toolbar. This includes an
     * edit and delete button.
     *
     * @param menu The options menu to place items.
     * @return True to display the menu, or false to not show the menu.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (transactionSource.transactionID > 0) {
            // Only show delete menu if we're editing a transfer
            menuInflater.inflate(R.menu.delete, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * An [onOptionsItemSelected] method that adds functionality when the menu buttons are clicked.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menuDelete -> {
            // Delete icon clicked, build an alert dialog to get user confirmation
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage(resources.getString(R.string.alert_message_delete_transaction))
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    finish()
                    // Delete the transaction
                    DBManager(this).deleteTransaction(
                        arrayOf(transactionSource.transactionID.toString())
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
     * An [onResume] method that launches the PIN code lock if enabled.
     */
    override fun onResume() {
        super.onResume()
        MainActivity.launchPinLock(this, applicationContext)
    }

    /**
     * An [onTrimMemory] method that sets the authenticated variable to false, as the app has been
     * sent to the background.
     */
    override fun onTrimMemory(level: Int) {
        MainActivity.authenticated = false
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