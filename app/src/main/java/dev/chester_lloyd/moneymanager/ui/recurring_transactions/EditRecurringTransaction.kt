package dev.chester_lloyd.moneymanager.ui.recurring_transactions

import android.app.AlertDialog
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.*
import dev.chester_lloyd.moneymanager.*
import dev.chester_lloyd.moneymanager.RecurringTransaction.Companion.NO_END_DATE_YEARS
import dev.chester_lloyd.moneymanager.ui.CurrencyValidator
import dev.chester_lloyd.moneymanager.ui.Icon
import dev.chester_lloyd.moneymanager.ui.IconManager
import dev.chester_lloyd.moneymanager.ui.IconSpinner
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.*
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.etAmount
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.etFrequencyEndDate
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.etFrequencyUnit
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.llFrequencyEnds
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.spCategory
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.swFrequencySetEndDate
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.tvSuffix as tvSuffix1
import kotlinx.android.synthetic.main.activity_edit_recurring_transaction.tvSymbol as tvSymbol1
import java.text.SimpleDateFormat
import java.util.*

/**
 * An [AppCompatActivity] subclass to add or edit a [RecurringTransaction].
 *
 * @author Chester Lloyd
 * @since 1.5
 */
@Suppress("NAME_SHADOWING")
class EditRecurringTransaction : AppCompatActivity() {

    var recurringTransaction = RecurringTransaction()
    private var income = false
    private var hasEndDate = false
    private var initialCategory = Category()
    private var account = Account()
    private var category = Category()

    /**
     * An [onCreate] method that sets up the supportActionBar, icon spinners, FAB and view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.hideInMultitasking(window, applicationContext)
        setContentView(R.layout.activity_edit_recurring_transaction)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.manage_recurring_transaction)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Add the currency symbol and suffix to the amount row
        val format = MainActivity.getCurrencyFormat(this)
        tvSymbol1.text = format[0]
        tvSuffix1.text = format[3]

        // Get details for the recurring transaction
        val dbManager = DBManager(this)
        val recurringTransactionID = intent.getIntExtra("recurringTransactionID", 0)
        recurringTransaction = dbManager.selectRecurringTransaction(recurringTransactionID)

        // Set up the frequency period spinner
        val frequencyPeriods =
            applicationContext.resources.getStringArray(R.array.transaction_frequency_periods)
        val spFrequencyPeriod: Spinner = this.findViewById(R.id.spFrequencyPeriod)
        ArrayAdapter.createFromResource(
            applicationContext,
            R.array.transaction_frequency_periods,
            R.layout.spinner_text
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_text)
            spFrequencyPeriod.adapter = adapter
        }
        spFrequencyPeriod.setSelection(0)

        // Update the recurring payment when this value changes
        spFrequencyPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                recurringTransaction.frequencyPeriod = frequencyPeriods[position]
            }
        }

        // Create a date picker, set values for class date value
        val sdfFreq = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        etFrequencyNextDate!!.setText(sdfFreq.format(recurringTransaction.next.time))

        val frequencyDateNextListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                recurringTransaction.next.set(Calendar.YEAR, year)
                recurringTransaction.next.set(Calendar.MONTH, monthOfYear)
                recurringTransaction.next.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                etFrequencyNextDate!!.setText(sdfFreq.format(recurringTransaction.next.time))
            }

        // When the date edit text has focus (clicked), open the date picker
        etFrequencyNextDate.onFocusChangeListener = View.OnFocusChangeListener { _, gainFocus ->
            if (gainFocus) {
                DatePickerDialog(
                    this@EditRecurringTransaction,
                    frequencyDateNextListener,
                    // Set to point to next transaction date
                    recurringTransaction.next.get(Calendar.YEAR),
                    recurringTransaction.next.get(Calendar.MONTH),
                    recurringTransaction.next.get(Calendar.DAY_OF_MONTH)
                ).show()
                etFrequencyNextDate.clearFocus()
            }
        }

        // Listen for when set end date switch is changed
        swFrequencySetEndDate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                llFrequencyEnds.visibility = View.VISIBLE
            } else {
                llFrequencyEnds.visibility = View.GONE
            }
            hasEndDate = isChecked
        }

        // Create a date picker, set values for class date value
        etFrequencyEndDate!!.setText(sdfFreq.format(recurringTransaction.end.time))

        val frequencyDateEndListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                recurringTransaction.end.set(Calendar.YEAR, year)
                recurringTransaction.end.set(Calendar.MONTH, monthOfYear)
                recurringTransaction.end.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                etFrequencyEndDate!!.setText(sdfFreq.format(recurringTransaction.end.time))
            }

        // When the date edit text has focus (clicked), open the date picker
        etFrequencyEndDate.onFocusChangeListener = View.OnFocusChangeListener { _, gainFocus ->
            if (gainFocus) {
                DatePickerDialog(
                    this@EditRecurringTransaction,
                    frequencyDateEndListener,
                    // set to point to tomorrow's date
                    recurringTransaction.end.get(Calendar.YEAR),
                    recurringTransaction.end.get(Calendar.MONTH),
                    recurringTransaction.end.get(Calendar.DAY_OF_MONTH)
                ).show()
                etFrequencyEndDate.clearFocus()
            }
        }

        // Setup the account spinner
        val accounts: ArrayList<Account> = dbManager.selectAccounts("active", null)

        val iconManager = IconManager(this)
        val accountIcons = arrayOfNulls<Icon>(accounts.size)
        val accountBackgrounds = arrayOfNulls<Icon>(accounts.size)

        for (account in 0 until accounts.size) {
            accountIcons[account] = Icon(
                account, iconManager.getIconByID(
                    iconManager.accountIcons,
                    accounts[account].icon
                ).drawable, accounts[account].name!!,
                null
            )

            accountBackgrounds[account] = Icon(
                account, iconManager.getIconByID(
                    iconManager.colourIcons,
                    accounts[account].colour
                ).drawable, "", null
            )
        }

        val accountSpinner = findViewById<Spinner>(R.id.spAccount)
        accountSpinner.adapter = IconSpinner(
            applicationContext,
            accountIcons.requireNoNulls(), accountBackgrounds.requireNoNulls(), null, "icon"
        )

        // Add selected account to transaction object
        spAccount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                account = accounts[position]
            }
        }

        // Setup the category spinner
        val categories: ArrayList<Category> = dbManager.selectCategories()

        val icons = arrayOfNulls<Icon>(categories.size)
        val backgrounds = arrayOfNulls<Icon>(categories.size)

        for (category in 0 until categories.size) {
            icons[category] = Icon(
                category, iconManager.getIconByID(
                    iconManager.categoryIcons,
                    categories[category].icon
                ).drawable, categories[category].name,
                null
            )

            backgrounds[category] = Icon(
                category, iconManager.getIconByID(
                    iconManager.colourIcons,
                    categories[category].colour
                ).drawable, "", null
            )
        }

        val categorySpinner = findViewById<Spinner>(R.id.spCategory)
        categorySpinner.adapter = IconSpinner(
            applicationContext,
            icons.requireNoNulls(), backgrounds.requireNoNulls(), null, "icon"
        )

        // Add selected category to transaction object
        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category = categories[position]
            }
        }

        dbManager.sqlDB!!.close()

        // If the category ID > 0 (not a new one) then auto fill these fields with the saved values
        if (recurringTransactionID > 0) {
            etName.setText(recurringTransaction.name)
            etAmount.setText(CurrencyValidator.getEditTextAmount(
                recurringTransaction.amount,
                format[2])
            )
            if (recurringTransaction.amount >= 0) {
                income = true
            }

            etFrequencyUnit.setText(recurringTransaction.frequencyUnit.toString())

            for (period in frequencyPeriods.indices) {
                if (frequencyPeriods[period] == recurringTransaction.frequencyPeriod) {
                    spFrequencyPeriod.setSelection(period)
                    break
                }
            }

            // Toggle slider if end date set, else hide end date as indefinite
            if (recurringTransaction.getFormattedEndDate(this) != this.getString(R.string.indefinitely)) {
                swFrequencySetEndDate.toggle()

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                etFrequencyEndDate!!.setText(sdf.format(recurringTransaction.end.time))
            } else {
                etFrequencyEndDate!!.setText("")
                llFrequencyEnds.visibility = View.GONE
            }

            for (account in 0 until accounts.size) {
                if (accounts[account].accountID == recurringTransaction.account.accountID) {
                    spAccount.setSelection(account)
                    break
                }
            }

            for (category in 0 until categories.size) {
                if (categories[category].categoryID == recurringTransaction.category.categoryID) {
                    spCategory.setSelection(category)
                    initialCategory = categories[category]
                    break
                }
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

        // Update the recurring transaction (and sub transactions) on FAB click
        fabUpdateRecurringTransaction.setOnClickListener {
            recurringTransaction.name = etName.text.toString()
            recurringTransaction.amount = amountValidator.getBalance(format[2])
            if (!income) recurringTransaction.amount *= -1
            recurringTransaction.account = account
            recurringTransaction.category = category
            recurringTransaction.frequencyUnit = etFrequencyUnit.text.toString().toInt()

            if (!hasEndDate) {
                // Add 1000 years as no end date set
                recurringTransaction.end.add(Calendar.YEAR, NO_END_DATE_YEARS)
            }

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
            } else if (hasEndDate && etFrequencyEndDate.text.toString() == "") {
                // Transaction date is empty, show an error
                Toast.makeText(
                    this, R.string.transaction_validation_date,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (recurringTransaction.validateUpdate(this)) {

                if (category.categoryID != initialCategory.categoryID) {
                    // Build an alert dialog to get user confirmation
                    val alertDialog = AlertDialog.Builder(this)

                    alertDialog.setMessage(resources.getString(R.string.alert_message_recurring_transaction_update_category))
                        .setCancelable(false)
                        .setPositiveButton(resources.getString(R.string.yes)) { dialog, id ->
                            finish()
                            // Update all previous transactions
                            for (transaction in recurringTransaction.transactions) {
                                transaction.category = category
                                val dbManager = DBManager(this)
                                dbManager.updateTransaction(
                                    transaction,
                                    "ID=?",
                                    arrayOf(transaction.transactionID.toString())
                                )
                                dbManager.sqlDB!!.close()
                            }
                            update()
                        }
                        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                            // Do nothing as this is handled below, close box
                            finish()
                            update()
                        }

                    val alert = alertDialog.create()
                    alert.setTitle(resources.getString(R.string.alert_title_recurring_transaction_update_category))
                    alert.show()
                } else {
                    update()
                }
            }
        }
    }

    /**
     * Method to start updating this recurring transaction
     */
    private fun update() {
        // All data has been filled out correctly, start saving
        val dbManager = DBManager(this)
        val id = dbManager.updateRecurringTransaction(
            recurringTransaction,
            "ID=?",
            arrayOf(recurringTransaction.recurringTransactionID.toString())
        )
        dbManager.sqlDB!!.close()
        if (id > 0) {
            // Updated in the database, return to previous recurring transaction details fragment
            Toast.makeText(this, R.string.recurring_transaction_update_success, Toast.LENGTH_LONG)
                .show()
            this.finish()
        } else {
            // Failed to update, show this error
            Toast.makeText(this, R.string.recurring_transaction_update_fail, Toast.LENGTH_LONG)
                .show()
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
