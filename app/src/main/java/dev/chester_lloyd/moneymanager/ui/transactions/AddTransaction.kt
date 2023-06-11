package dev.chester_lloyd.moneymanager.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import dev.chester_lloyd.moneymanager.*
import dev.chester_lloyd.moneymanager.RecurringTransaction.Companion.NO_END_DATE_YEARS
import dev.chester_lloyd.moneymanager.ui.CurrencyValidator
import dev.chester_lloyd.moneymanager.ui.Icon
import dev.chester_lloyd.moneymanager.ui.IconManager
import dev.chester_lloyd.moneymanager.ui.IconSpinner
import kotlinx.android.synthetic.main.activity_add_transaction.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * An [AppCompatActivity] subclass to add or edit a [Transaction].
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class AddTransaction : AppCompatActivity() {

    var transaction = Transaction()
    var recurringTransaction = RecurringTransaction()
    private var isRecurring = false
    private var hasEndDate = false
    private var income = false
    private var paymentMethodIds = HashMap<Int, ArrayList<Int>>()
    private lateinit var accounts: ArrayList<Account>
    private lateinit var initialPayments: ArrayList<Payment>

    /**
     * An [onCreate] method that sets up the supportActionBar, category and account spinners,
     * amount validator, date picker, FAB and view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.hideInMultitasking(window, applicationContext)
        setContentView(R.layout.activity_add_transaction)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.button_add_transaction)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Add the currency symbol and suffix to the amount row
        val format = MainActivity.getCurrencyFormat(this)
        tvSymbol.text = format[0]
        tvSuffix.text = format[3]

        // Get transaction from database, if ID given (to edit)
        val transactionID = intent.getIntExtra("transactionID", 0)

        // Listen for when income switch is changed
        swIncome.setOnCheckedChangeListener { _, isChecked ->
            income = isChecked
        }

        // Setup the date to the current device date
        val etDate = this.etDate
        updateDateInView()

        // Create a date picker, set values for class date value
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                transaction.date.set(Calendar.YEAR, year)
                transaction.date.set(Calendar.MONTH, monthOfYear)
                transaction.date.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        // When the date edit text has focus (clicked), open the date picker
        etDate.onFocusChangeListener = View.OnFocusChangeListener { _, gainFocus ->
            if (gainFocus) {
                DatePickerDialog(
                    this@AddTransaction,
                    dateSetListener,
                    // set to point to today's date when it loads up
                    transaction.date.get(Calendar.YEAR),
                    transaction.date.get(Calendar.MONTH),
                    transaction.date.get(Calendar.DAY_OF_MONTH)
                ).show()
                etDate.clearFocus()
            }
        }

        if (transactionID == 0) {
            // Only show recurring settings for new transactions
            swRecurringPayment.visibility = View.VISIBLE

            // Listen for when recurring transactions switch is changed
            swRecurringPayment.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    llFrequency.visibility = View.VISIBLE
                } else {
                    llFrequency.visibility = View.GONE
                }
                isRecurring = isChecked
            }

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
            val sdfFreq = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
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
                        this@AddTransaction,
                        frequencyDateEndListener,
                        // set to point to tomorrow's date
                        recurringTransaction.end.get(Calendar.YEAR),
                        recurringTransaction.end.get(Calendar.MONTH),
                        recurringTransaction.end.get(Calendar.DAY_OF_MONTH)
                    ).show()
                    etFrequencyEndDate.clearFocus()
                }
            }
        }

        val dbManager = DBManager(this)

        // Setup the category icon spinner
        val categories: ArrayList<Category> = dbManager.selectCategories()

        val iconManager = IconManager(this)
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
                transaction.category = categories[position]
            }
        }

        // Create necessary arrays for future payment method spinners
        accounts = dbManager.selectAccounts("active", null)
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

        // Load in accounts data
        val defaultOrFirstAccount = dbManager.getDefaultAccount() ?: accounts[0]

        // Set first payment method spinner to default or first account
        if (transactionID == 0) {
            val firstPaymentMethodHash = addPaymentMethod(
                defaultOrFirstAccount,
                accountIcons.requireNoNulls(),
                accountBackgrounds.requireNoNulls(),
                true
            )
            val firstPaymentMethodSpinnerId = paymentMethodIds[firstPaymentMethodHash]!![1]
            val firstPaymentMethodSpinner = findViewById<Spinner>(firstPaymentMethodSpinnerId)
            for (account in 0 until accounts.size) {
                if (accounts[account].accountID == defaultOrFirstAccount.accountID) {
                    firstPaymentMethodSpinner.setSelection(account)
                    break
                }
            }
        }

        // Add payment method button
        buAddPaymentMethod.setOnClickListener {
            addPaymentMethod(
                defaultOrFirstAccount,
                accountIcons.requireNoNulls(),
                accountBackgrounds.requireNoNulls()
            )
        }

        if (transactionID > 0) {
            this.supportActionBar?.title = getString(R.string.edit_transaction)
            tvDesc.setText(R.string.text_edit_transaction_desc)
            transaction = dbManager.selectTransaction(transactionID)
            etMerchant.setText(transaction.merchant)
            etDetails.setText(transaction.details)
            etAmount.setText(CurrencyValidator.getEditTextAmount(transaction.amount, format[2]))
            if (transaction.amount > 0) {
                swIncome.toggle()
                income = true
            }
            updateDateInView()

            for (category in 0 until categories.size) {
                if (categories[category].categoryID == transaction.category.categoryID) {
                    spCategory.setSelection(category)
                    break
                }
            }

            // Create payment method spinners and autofill
            initialPayments = dbManager.selectPayments(transactionID, "transaction")
            for (payment in 0 until initialPayments.size) {
                if (initialPayments[payment].amount != 0.0) {
                    val paymentMethodHash = addPaymentMethod(
                        initialPayments[payment].account,
                        accountIcons.requireNoNulls(),
                        accountBackgrounds.requireNoNulls(),
                        payment == 0
                    )
                    val paymentMethodEditTextId = paymentMethodIds[paymentMethodHash]!![0]
                    findViewById<EditText>(paymentMethodEditTextId)
                        .setText(
                            CurrencyValidator.getEditTextAmount(
                                initialPayments[payment].amount,
                                format[2]
                            )
                        )
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

                // Work out which account to add the new amount to
                var enteredAmountEditTextID = 0
                for (paymentMethod in paymentMethodIds.values) {
                    if ((findViewById<EditText>(paymentMethod[0])).text.toString() != "") {
                        // If more than one box filled in, give up as too complicated to adjust
                        enteredAmountEditTextID =
                            if (enteredAmountEditTextID == -1 || enteredAmountEditTextID != 0) {
                                -1
                            } else {
                                // Else just update this one
                                paymentMethod[0]
                            }
                    }
                }
                if (enteredAmountEditTextID == 0) {
                    // Update first payment method amount
                    val accountAmount =
                        findViewById<EditText>(paymentMethodIds.entries.first().value[0])
                    accountAmount.setText(etAmount.text.toString())
                } else if (enteredAmountEditTextID != -1) {
                    // Update the account we just worked out
                    val accountAmount = findViewById<EditText>(enteredAmountEditTextID)
                    accountAmount.setText(etAmount.text.toString())
                }
            }
        })

        // Save or update the transaction on FAB click
        fabAddTransaction.setOnClickListener {
            val dbManager = DBManager(this)
            transaction.merchant = etMerchant.text.toString()
            transaction.details = etDetails.text.toString()

            // Get number of accounts used as we can only add recurring transactions against 1
            var accountsUsed = 0
            val accountsUsedIds = ArrayList<Int>()
            var accountsUsedDuplicate = false
            var accountUsed = Account()
            for (paymentMethod in paymentMethodIds.values) {
                val accountValue = (this.findViewById(paymentMethod[0]) as EditText)
                    .text.toString()
                if (accountValue != "" && accountValue != format[2]) {
                    accountsUsed++
                    accountUsed = dbManager.selectAccount(paymentMethod[2])
                }
                if (accountsUsedIds.contains(paymentMethod[2])) {
                    accountsUsedDuplicate = true
                }
                accountsUsedIds.add(paymentMethod[2])
            }
            if (transaction.merchant == "") {
                // Transaction name is empty, show an error
                Toast.makeText(
                    this, R.string.transaction_validation_name,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (etAmount.text.toString() == "") {
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
            } else if (isRecurring && !recurringTransaction.validateWithTransaction(
                    applicationContext,
                    transaction,
                    etFrequencyUnit.text.toString().toInt()
                )
            ) {
                // Validate frequency options
            } else if (hasEndDate && !recurringTransaction.validateEndDateWithTransaction(
                    applicationContext,
                    transaction
                )
            ) {
                // Validate end date frequency options
            } else if (isRecurring && accountsUsed != 1) {
                // Can only use 1 account for recurring payments, show an error
                Toast.makeText(
                    this, R.string.transaction_recurring_validation_multiple_accounts,
                    Toast.LENGTH_SHORT
                ).show()
                // Validate end date frequency options
            } else if (accountsUsedDuplicate) {
                // Cannot use the same payment methods more than once
                Toast.makeText(
                    this, R.string.transaction_validation_duplicate_payment_methods,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // All data has been filled out, start saving
                transaction.amount = amountValidator.getBalance(format[2])
                if (!income) transaction.amount *= -1

                // Get all payments as Payment objects
                val payments = ArrayList<Payment>()
                var totalPayments = 0.0
                for (paymentMethod in paymentMethodIds.values) {
                    if ((this.findViewById(paymentMethod[0]) as EditText)
                            .text.toString() == format[2]
                    ) {
                        // Transaction amount is the decimal sign only, show an error
                        Toast.makeText(
                            this, R.string.transaction_validation_amount_invalid,
                            Toast.LENGTH_SHORT
                        ).show()
                        break
                    } else {
                        var accountValue = CurrencyValidator(
                            this.findViewById(paymentMethod[0])
                        ).getBalance(format[2])
                        if (!income) accountValue *= -1

                        // Round to 2dp (since double would probably do: 20.0000004 or something)
                        val accountValue2DP: Double = String.format("%.2f", accountValue).toDouble()
                        totalPayments += accountValue2DP
                        payments.add(
                            Payment(
                                Transaction(),
                                dbManager.selectAccount(paymentMethod[2]),
                                accountValue
                            )
                        )
                    }
                }

                if (String.format("%.2f", totalPayments).toDouble() != transaction.amount) {
                    // Account payments do not make up the transaction amount, show an error
                    Toast.makeText(
                        this, R.string.transaction_validation_payments,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (transaction.transactionID == 0) {
                        // Insert this new transaction into the transactions table
                        val id = dbManager.insertTransaction(transaction)
                        if (id > 0) {
                            transaction.transactionID = id.toInt()
                            for (payment in 0 until payments.size) {
                                // For each payment method (Account)
                                if (payments[payment].amount != 0.0) {
                                    // Add a payment for this amount
                                    payments[payment].transaction = transaction
                                    dbManager.insertPayment(payments[payment])
                                }
                            }

                            // Transaction saved to database, return to previous fragment
                            var message =
                                applicationContext.resources.getString(R.string.transaction_insert_success)
                            if (isRecurring) {
                                message = applicationContext.resources.getString(
                                    R.string.transaction_recurring_insert_success,
                                    recurringTransaction.getFrequencyString()
                                )

                                // Add the recurring transaction to the database
                                recurringTransaction.transactions = arrayListOf(transaction)
                                recurringTransaction.account = accountUsed
                                recurringTransaction.category = transaction.category
                                recurringTransaction.name = transaction.merchant!!
                                recurringTransaction.amount = transaction.amount
                                recurringTransaction.start = transaction.date
                                if (!hasEndDate) {
                                    // Add 1000 years as no end date set
                                    recurringTransaction.end.add(Calendar.YEAR, NO_END_DATE_YEARS)
                                }
                                val recurringTransactionID =
                                    dbManager.insertRecurringTransaction(recurringTransaction)
                                recurringTransaction.recurringTransactionID =
                                    recurringTransactionID.toInt()
                                recurringTransaction.next = transaction.date

                                if (transaction.date.timeInMillis > Calendar.getInstance().timeInMillis) {
                                    // Set next date to 1 unit past the future set date
                                    recurringTransaction.setNextDueDateFutureTransaction()
                                } else {
                                    // Set next date and back fill earlier transactions
                                    recurringTransaction.setNextDueDateAndCreateTransactions(
                                        applicationContext
                                    )
                                }
                                // Update as next date has changed from calls above
                                dbManager.updateRecurringTransaction(
                                    recurringTransaction,
                                    "ID=?",
                                    arrayOf(recurringTransaction.recurringTransactionID.toString())
                                )
                            }

                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                            setResult(RESULT_OK)
                            this.finish()
                        } else {
                            // Failed to save, show this error
                            Toast.makeText(
                                this, R.string.transaction_insert_fail,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        // Update this transaction in the database
                        val selectionArgs = arrayOf(transaction.transactionID.toString())
                        val id = dbManager.updateTransaction(
                            transaction, "ID=?",
                            selectionArgs
                        )
                        if (id > 0) {
                            val initialPaymentsRemaining = initialPayments
                            // Remove any payment methods that aren't used
                            for (payment in 0 until payments.size) {
                                initialPaymentsRemaining.removeAll { it.account.accountID == payments[payment].account.accountID }
                            }
                            // These are no longer used, so delete
                            for (payment in 0 until initialPaymentsRemaining.size) {
                                initialPaymentsRemaining[payment].amount = 0.0
                            }
                            payments.addAll(initialPaymentsRemaining)
                            for (payment in 0 until payments.size) {
                                // For each payment method (Account) update the payments that are stored
                                payments[payment].transaction = transaction
                                dbManager.updatePayment(
                                    payments[payment],
                                    "TransactionID=? AND AccountID=?",
                                    arrayOf(
                                        transaction.transactionID.toString(),
                                        payments[payment].account.accountID.toString()
                                    )
                                )
                            }
                            // Transaction updated in the database, return to previous fragment
                            Toast.makeText(
                                this, R.string.transaction_update_success,
                                Toast.LENGTH_LONG
                            ).show()
                            setResult(RESULT_OK)
                            this.finish()
                        } else {
                            // Failed to save, show this error
                            Toast.makeText(
                                this, R.string.transaction_update_fail,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            dbManager.sqlDB!!.close()
        }
        dbManager.sqlDB!!.close()
    }

    /**
     * Update [etDate] value with the class date variable.
     */
    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
        etDate!!.setText(sdf.format(transaction.date.time))
    }

    /**
     * Create a payment method [Spinner] and an accompanying currency [EditText] with listeners.
     *
     * @param account The [Account] this spinner should select by default
     * @param accountIcons Account icons
     * @param accountBackgrounds Account backgrounds
     * @param firstPaymentMethod Hides remove button if this is the first payment method
     * @return The key to this payment method in the [paymentMethodIds] [HashMap]
     */
    private fun addPaymentMethod(
        account: Account,
        accountIcons: Array<Icon>,
        accountBackgrounds: Array<Icon>,
        firstPaymentMethod: Boolean = false
    ): Int {
        // Create a view ID for the amount, used later when saving
        val paymentMethodSpinnerId = View.generateViewId()
        val paymentMethodEditTextId = View.generateViewId()

        // Create a new payment method spinner
        val paymentMethodSpinner = Spinner(this)
        paymentMethodSpinner.id = paymentMethodSpinnerId
        paymentMethodSpinner.adapter =
            IconSpinner(applicationContext, accountIcons, accountBackgrounds, null, "icon")

        // Update payment methods hash map with new account
        paymentMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                paymentMethodIds[paymentMethodSpinner.hashCode()] = arrayListOf(
                    paymentMethodEditTextId,
                    paymentMethodSpinnerId,
                    accounts[position].accountID
                )
            }
        }

        for (accountIndex in 0 until accounts.size) {
            if (accounts[accountIndex].accountID == account.accountID) {
                paymentMethodSpinner.setSelection(accountIndex)
                break
            }
        }

        // Create a new currency input for this payment method
        val format = MainActivity.getCurrencyFormat(this)
        val etAmount = EditText(this)
        etAmount.id = paymentMethodEditTextId
        etAmount.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_CLASS_NUMBER
        etAmount.keyListener = DigitsKeyListener.getInstance("0123456789${format[2]}")

        // Validate the currency field
        val balanceValidator = CurrencyValidator(etAmount)
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Update balance before changes have been made (i.e user changed it)
                balanceValidator.beforeTextChangedListener(s)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                balanceValidator.onTextChangedListener(s, format[2])
            }
        })

        paymentMethodIds[paymentMethodSpinner.hashCode()] =
            arrayListOf(paymentMethodEditTextId, paymentMethodSpinnerId, account.accountID)

        // Create a new linear layout for this payment method container (Spinner - Amount)
        val llAccountContainer = LinearLayout(this)
        llAccountContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llAccountContainer.orientation = LinearLayout.VERTICAL

        // Create a new linear layout for this payment method amount (Symbol - Edit Text - Suffix)
        val llAccountRow = LinearLayout(this)
        llAccountRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llAccountRow.orientation = LinearLayout.HORIZONTAL

        // Create a text view for the symbol and suffix
        val tvSymbol = TextView(this)
        tvSymbol.text = format[0]
        tvSymbol.textSize = 18f
        tvSymbol.setPadding(16, 0, 0, 0)
        val tvSuffix = TextView(this)
        tvSuffix.text = format[3]
        tvSuffix.textSize = 18f

        // Create a remove button
        val buDeletePaymentMethod = Button(this)
        buDeletePaymentMethod.text = getString(R.string.transaction_remove_payment_method)
        if (firstPaymentMethod) {
            buDeletePaymentMethod.visibility = View.INVISIBLE
        }
        buDeletePaymentMethod.setPadding(16, 0, 16, 0)
        buDeletePaymentMethod.setOnClickListener {
            // Find the initial payment, remove it and hide - will be deleted on save
            paymentMethodIds.remove(paymentMethodSpinner.hashCode())
            llAccountContainer.visibility = View.GONE
        }

        // Add all elements to the row's view
        llAccountRow.addView(tvSymbol)
        llAccountRow.addView(etAmount)
        llAccountRow.addView(tvSuffix)
        llAccountRow.addView(buDeletePaymentMethod)

        // Setup layout parameters so they match the main amount layout (above)
        tvSymbol.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        etAmount.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            10000f
        )
        tvSuffix.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        // Add this row to the main linear layout containing the payment methods
        llAccountContainer.addView(paymentMethodSpinner)
        llAccountContainer.addView(llAccountRow)
        llAccountContainer.setPadding(0, 0, 0, 0)
        llAccounts.addView(llAccountContainer)

        return paymentMethodSpinner.hashCode()
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

    /**
     * An [onBackPressed] method that informs the calling activity that we have closed this activity
     * (by toolbar or device back button.
     */
    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }
}
