package dev.chester_lloyd.moneymanager.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.chester_lloyd.moneymanager.*
import dev.chester_lloyd.moneymanager.ui.accounts.AddAccount
import dev.chester_lloyd.moneymanager.ui.categories.AddCategory
import kotlinx.android.synthetic.main.activity_add_transaction_check_requirements.*

/**
 * An [AppCompatActivity] subclass to redirect to add an [Account] and/or [Category] prior
 * to adding a [Transaction].
 *
 * @author Chester Lloyd
 * @since 1.1
 */
class AddTransactionCheckRequirements : AppCompatActivity() {

    private var noAccounts = false
    private var noCategories = false

    /**
     * An [onCreate] method that sets up the supportActionBar, buttons, FAB and view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction_check_requirements)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.button_add_transaction)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Launch new account activity with button
        buAddAccount.setOnClickListener {
            val intent = Intent(applicationContext, AddAccount::class.java)
            startActivity(intent)
        }

        // Launch new category activity with button
        buAddCategory.setOnClickListener {
            val intent = Intent(applicationContext, AddCategory::class.java)
            startActivity(intent)
        }

        // Launch new transaction activity with button
        buAddTransaction.setOnClickListener {
            val intent = Intent(applicationContext, AddTransaction::class.java)
            this.startActivityForResult(intent, 0)
        }
    }

    /**
     * An [onResume] method that decides which buttons and text to provide
     */
    override fun onResume() {
        super.onResume()

        // Get accounts as an array list from database and and verify if we have at least one
        val dbManager = DBManager(this)
        val listAccounts = dbManager.selectAccounts("active", null)
        val listCategories = dbManager.selectCategories()
        dbManager.sqlDB!!.close()

        when {
            listAccounts.isEmpty() -> {
                this.noAccounts = true
                llNoAccounts.visibility = View.VISIBLE
                llNoCategories.visibility = View.GONE
                llAddTransaction.visibility = View.GONE
            }
            listCategories.isEmpty() -> {
                this.noCategories = true
                llNoAccounts.visibility = View.GONE
                llNoCategories.visibility = View.VISIBLE
                llAddTransaction.visibility = View.GONE
            }
            else -> {
                llNoAccounts.visibility = View.GONE
                llNoCategories.visibility = View.GONE
                llAddTransaction.visibility = View.VISIBLE
            }
        }
        if (!noAccounts && !noCategories) {
            val intent = Intent(applicationContext, AddTransaction::class.java)
            this.startActivityForResult(intent, 0)
        }
    }

    /**
     * An [onActivityResult] method that will close this activity if the user has added a transaction
     * or pressed a back button.
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            this.finish()
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
