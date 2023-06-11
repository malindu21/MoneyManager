package dev.chester_lloyd.moneymanager.ui.categories

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import dev.chester_lloyd.moneymanager.R
import dev.chester_lloyd.moneymanager.DBManager
import dev.chester_lloyd.moneymanager.Category
import dev.chester_lloyd.moneymanager.MainActivity
import dev.chester_lloyd.moneymanager.ui.IconManager
import dev.chester_lloyd.moneymanager.ui.IconSpinner
import kotlinx.android.synthetic.main.activity_add_account.*

/**
 * An [AppCompatActivity] subclass to add or edit a [Category].
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class AddCategory : AppCompatActivity() {

    /**
     * An [onCreate] method that sets up the supportActionBar, icon spinners, FAB and view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.hideInMultitasking(window, applicationContext)
        setContentView(R.layout.activity_add_category)

        // Setup toolbar name and show a back button
        this.supportActionBar?.title = getString(R.string.button_add_category)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val iconManager = IconManager(this)

        // Set up the category icon spinner
        val iconSpinner = findViewById<Spinner>(R.id.spIcon)
        iconSpinner.adapter = IconSpinner(
            applicationContext,
            iconManager.categoryIcons, null, null, "icon"
        )

        // Set up the category color spinner
        val colourSpinner = findViewById<Spinner>(R.id.spColour)
        colourSpinner.adapter = IconSpinner(
            applicationContext,
            null, iconManager.colourIcons, null, "colour"
        )

        val category = Category()
        category.categoryID = intent.getIntExtra("categoryID", 0)

        // If the category ID > 0 (not a new one) then auto fill these fields with the saved values
        if (category.categoryID > 0) {
            this.supportActionBar?.title = getString(R.string.edit_category)
            tvDesc.setText(R.string.text_edit_category_desc)
            etName.setText(intent.getStringExtra("name"))

            spIcon.setSelection(
                iconManager.getIconPositionID(
                    iconManager.categoryIcons,
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

        // Save or update the category on FAB click
        fabAddAccount.setOnClickListener {
            category.name = etName.text.toString()

            if (category.name == "") {
                // Category name is empty, show an error
                Toast.makeText(this, R.string.category_validation_name, Toast.LENGTH_SHORT)
                    .show()
            } else {
                // All data has been filled out, start saving
                // Get instance of the database manager class
                val dbManager = DBManager(this)

                if (category.categoryID == 0) {
                    // Insert this new category into the categories table
                    val id = dbManager.insertCategory(category)
                    if (id > 0) {
                        // Category saved to database, return to previous categories fragment
                        Toast.makeText(this, R.string.category_insert_success, Toast.LENGTH_LONG)
                            .show()
                        this.finish()
                    } else {
                        // Failed to save, show this error
                        Toast.makeText(this, R.string.category_insert_fail, Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    // Update this category in the database
                    val selectionArgs = arrayOf(category.categoryID.toString())
                    val id = dbManager.updateCategory(category, "ID=?", selectionArgs)
                    if (id > 0) {
                        // Category updated in the database, return to previous categories fragment
                        Toast.makeText(this, R.string.category_update_success, Toast.LENGTH_LONG)
                            .show()
                        this.finish()
                    } else {
                        // Failed to save, show this error
                        Toast.makeText(this, R.string.category_update_fail, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                dbManager.sqlDB!!.close()
            }
        }

        // Add selected icon to category object
        spIcon?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category.icon = iconManager.categoryIcons[position].id
            }
        }

        // Add selected colour to category object
        spColour?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category.colour = iconManager.colourIcons[position].id
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
