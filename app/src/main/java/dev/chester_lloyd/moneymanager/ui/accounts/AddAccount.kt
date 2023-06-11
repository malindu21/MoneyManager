package dev.chester_lloyd.moneymanager.ui.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.chester_lloyd.moneymanager.Account
import dev.chester_lloyd.moneymanager.R
import dev.chester_lloyd.moneymanager.DBManager
import dev.chester_lloyd.moneymanager.MainActivity
import dev.chester_lloyd.moneymanager.ui.CurrencyValidator
import dev.chester_lloyd.moneymanager.ui.IconManager
import dev.chester_lloyd.moneymanager.ui.IconSpinner
import kotlinx.android.synthetic.main.activity_add_account.*
import kotlinx.android.synthetic.main.activity_add_account.tvDesc
import kotlinx.android.synthetic.main.activity_add_account.tvSuffix
import kotlinx.android.synthetic.main.activity_add_account.tvSymbol

/**
 * An [AppCompatActivity] subclass to add or edit an [Account].
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class AddAccount : AppCompatActivity() {

    /**
     * An [onCreate] method that sets up the supportActionBar, icon spinners, balance validator,
     * FAB and view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.hideInMultitasking(window, applicationContext)
        setContentView(R.layout.activity_add_account)

        // Add the currency symbol and suffix to the amount row
        val format = MainActivity.getCurrencyFormat(this)
        tvSymbol.text = format[0]
        tvSuffix.text = format[3]
        val iconManager = IconManager(this)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.button_add_account)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up the account icon spinner
        val iconSpinner = findViewById<Spinner>(R.id.spIcon)
        iconSpinner.adapter = IconSpinner(
            applicationContext,
            iconManager.accountIcons, null, null, "icon"
        )

        // Set up the account color spinner
        val colourSpinner = findViewById<Spinner>(R.id.spColour)
        colourSpinner.adapter = IconSpinner(
            applicationContext,
            null, iconManager.colourIcons, null, "colour"
        )

        // Validate the balance field
        val balanceValidator = CurrencyValidator(etBalance)
        etBalance.keyListener = DigitsKeyListener.getInstance("-0123456789${format[2]}")
        etBalance.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Update balance before changes have been made (i.e user changed it)
                balanceValidator.beforeTextChangedListener(s)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                balanceValidator.onTextChangedListener(s, format[2])
            }
        })

        val account = Account()
        account.accountID = intent.getIntExtra("accountID", 0)


        val dbManager = DBManager(this)
        val defaultAccount = dbManager.getDefaultAccount()

        // Listen for when default account switch is changed
        swSetDefault.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (defaultAccount != null) {
                    if (defaultAccount.accountID != 0 && defaultAccount.accountID != account.accountID) {
                        // Delete icon clicked, build an alert dialog to get user confirmation
                        val alertDialog = AlertDialog.Builder(this)

                        alertDialog.setMessage("Are you sure you want this account to be your default, " +
                                "instead of ${defaultAccount.name}?")
                            .setCancelable(false)
                            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                                swSetDefault.isChecked = true
                                account.default = true
                            }
                            .setNegativeButton(resources.getString(R.string.no_cancel)) {
                                // Do nothing, close box
                                    dialog, _ ->
                                dialog.cancel()
                                swSetDefault.isChecked = false
                                account.default = false
                            }

                        val alert = alertDialog.create()
                        alert.setTitle("You already have a default account")
                        alert.show()
                    }
                } else {
                    // No account, set this as default
                    swSetDefault.isChecked = true
                    account.default = true
                }
            }
        }

        // Disable toggle if this will be the only account
        if (dbManager.selectAccounts("active", null).isEmpty()) {
            swSetDefault.isChecked = true
            swSetDefault.isEnabled = false
            account.default = true
        }
        dbManager.sqlDB!!.close()

        // If the account ID > 0 (not a new one) then auto fill these fields with the saved values
        if (account.accountID > 0) {
            this.supportActionBar?.title = getString(R.string.edit_account)
            tvDesc.setText(R.string.text_edit_account_desc)
            etName.setText(intent.getStringExtra("name"))
            etBalance.setText(
                CurrencyValidator.getEditTextAmountNeg(
                    intent.getDoubleExtra("balance", 0.0),
                    format[2]
                )
            )

            // Set default switch to on
            if (intent.getBooleanExtra("default", false)) {
                swSetDefault.isChecked = true
            }

            // Disable toggle if this is the only account
            val dbManager = DBManager(this)
            if (dbManager.selectAccounts("active", null).size == 1) {
                swSetDefault.isChecked = true
                swSetDefault.isEnabled = false
                account.default = true
            }
            dbManager.sqlDB!!.close()

            spIcon.setSelection(
                iconManager.getIconPositionID(
                    iconManager.accountIcons,
                    intent.getIntExtra("icon", 0)
                )
            )

            spColour.setSelection(
                iconManager.getIconPositionID(
                    iconManager.colourIcons,
                    intent.getIntExtra("colour", 0)
                )
            )
        }

        // Save or update the account on FAB click
        fabAddAccount.setOnClickListener {
            account.name = etName.text.toString()

            if (account.name == "") {
                // Account name is empty, show an error
                Toast.makeText(this, R.string.account_validation_name, Toast.LENGTH_SHORT).show()
            } else if (etBalance.text.toString() == "") {
                // Account balance is empty, show an error
                Toast.makeText(this, R.string.account_validation_balance, Toast.LENGTH_SHORT).show()
            } else if (etBalance.text.toString() == format[2]
                || etBalance.text.toString() == "-"
                || etBalance.text.toString() == "-${format[2]}"
            ) {
                // Account balance is only the decimal sign or minus sign, show an error
                Toast.makeText(this, R.string.account_validation_balance_invalid, Toast.LENGTH_SHORT).show()
            } else {
                // All data has been filled out, start saving
                account.balance = balanceValidator.getBalance(format[2])

                // Get instance of the database manager class
                val dbManager = DBManager(this)

                if (account.accountID == 0) {
                    // Insert this new account into the accounts table
                    val id = dbManager.insertAccount(account)
                    if (id > 0) {
                        // Account saved to database, return to previous accounts fragment
                        Toast.makeText(this, R.string.account_insert_success, Toast.LENGTH_LONG)
                            .show()
                        this.finish()
                    } else {
                        // Failed to save, show this error
                        Toast.makeText(this, R.string.account_insert_fail, Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    // Update this account in the database
                    val selectionArgs = arrayOf(account.accountID.toString())
                    val id = dbManager.updateAccount(account, "ID=?", selectionArgs)
                    if (id > 0) {
                        // Account updated in the database, return to previous accounts fragment
                        Toast.makeText(this, R.string.account_update_success, Toast.LENGTH_LONG)
                            .show()
                        this.finish()
                    } else {
                        // Failed to save, show this error
                        Toast.makeText(this, R.string.account_update_fail, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                dbManager.sqlDB!!.close()
            }
        }

        // Add selected icon to account object
        spIcon?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                account.icon = iconManager.accountIcons[position].id
            }
        }

        // Add selected colour to account object
        spColour?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                account.colour = iconManager.colourIcons[position].id
            }
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
