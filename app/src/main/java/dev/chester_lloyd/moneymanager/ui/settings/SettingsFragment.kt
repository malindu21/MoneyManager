package dev.chester_lloyd.moneymanager.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.chester_lloyd.moneymanager.DBManager
import dev.chester_lloyd.moneymanager.MainActivity
import dev.chester_lloyd.moneymanager.MainActivity.Companion.FILE_SELECT_CODE
import dev.chester_lloyd.moneymanager.MainActivity.Companion.WRITE_EXTERNAL_STORAGE_REQUEST_CODE
import dev.chester_lloyd.moneymanager.MainActivity.Companion.getDateFormat
import dev.chester_lloyd.moneymanager.MainActivity.Companion.isPinSet
import dev.chester_lloyd.moneymanager.MainActivity.Companion.updateDateFormat
import dev.chester_lloyd.moneymanager.R
import dev.chester_lloyd.moneymanager.ui.PinCodeActivity
import dev.chester_lloyd.moneymanager.ui.dashboard.SetupApp
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * A [Fragment] subclass to show the settings screen.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private var dateFormat: String = ""

    /**
     * An [onCreateView] method that sets up the View, settings options and FAB
     *
     * @param inflater The LayoutInflater object.
     * @param container The parent view.
     * @param savedInstanceState Fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        val buCurrencyFormat: Button = root.findViewById(R.id.buCurrencyFormat)
        buCurrencyFormat.setOnClickListener {
            startActivity(Intent(context, SetupApp::class.java))
        }

        // Set up the date format spinner
        val spDateFormat: Spinner = root.findViewById(R.id.spDateFormat)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.settings_date_formats,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spDateFormat.adapter = adapter
        }

        val buUpdateDateFormat: Button = root.findViewById(R.id.buUpdateDateFormat)
        buUpdateDateFormat.setOnClickListener {
            updateDateFormat(requireContext(), dateFormat)
            buUpdateDateFormat.visibility = View.GONE
        }

        val buRemovePin: Button = root.findViewById(R.id.buRemovePin)
        buRemovePin.setOnClickListener {
            val pinIntent = Intent(context, PinCodeActivity::class.java)
            val pinBundle = Bundle()
            pinBundle.putString("journey", "remove")
            pinIntent.putExtras(pinBundle)
            startActivity(pinIntent)
        }

        val buExportDatabase: Button = root.findViewById(R.id.buExportDatabase)
        buExportDatabase.setOnClickListener {
            setupPermissions("export")
        }
        val buImportDatabase: Button = root.findViewById(R.id.buImportDatabase)
        buImportDatabase.setOnClickListener {
            setupPermissions("import")
        }

        return root
    }

    /**
     * An [onResume] method that shows the current currency format in a text view as a preview.
     */
    @SuppressLint("ResourceType")
    override fun onResume() {
        super.onResume()

        // Update currency preview
        val format = MainActivity.getCurrencyFormat(requireContext())
        val colourStr = resources.getString(R.color.colorPrimary)
        val colour = "#${colourStr.subSequence(3, colourStr.length)}"
        tvCurrencyFormat.text = Html.fromHtml(
            "<font color='$colour'>${format[0]}</font>3" +
                    "<font color='$colour'>${format[1]}</font>000" +
                    "<font color='$colour'>${format[2]}</font>50" +
                    "<font color='$colour'>${format[3]}</font>"
        )

        // Update spinner to saved date format
        val dateFormats = requireContext().resources.getStringArray(R.array.settings_date_formats)
        for (df in dateFormats.indices) {
            if (dateFormats[df] == getDateFormat(requireContext())) {
                spDateFormat.setSelection(df)
                break
            }
        }

        // Adds the date to the class variable for potential saving
        spDateFormat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                dateFormat = dateFormats[position]
                if (dateFormat != getDateFormat(requireContext())) {
                    buUpdateDateFormat.visibility = View.VISIBLE
                } else {
                    buUpdateDateFormat.visibility = View.GONE
                }
            }
        }

        // Update PIN status
        if (isPinSet(requireContext())) {
            tvPinStatus.text = this.resources.getText(R.string.settings_pin_set)
            buUpdatePin.text = this.resources.getText(R.string.settings_update_pin_button)
            buUpdatePin.setOnClickListener {
                val pinIntent = Intent(context, PinCodeActivity::class.java)
                val pinBundle = Bundle()
                pinBundle.putString("journey", "update")
                pinIntent.putExtras(pinBundle)
                startActivity(pinIntent)
            }
            buRemovePin.visibility = View.VISIBLE
        } else {
            tvPinStatus.text = this.resources.getText(R.string.settings_pin_unset)
            buUpdatePin.text = this.resources.getText(R.string.settings_set_pin_button)
            buUpdatePin.setOnClickListener {
                val pinIntent = Intent(context, PinCodeActivity::class.java)
                val pinBundle = Bundle()
                pinBundle.putString("journey", "new")
                pinIntent.putExtras(pinBundle)
                startActivity(pinIntent)
            }
            buRemovePin.visibility = View.GONE
        }
    }

    /**
     * An [onCreateOptionsMenu] method that adds the settings menu to the toolbar. This includes a
     * button to load the about page.
     *
     * @param menu The options menu to place items.
     * @param inflater The [MenuInflater].
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * An [onOptionsItemSelected] method that adds functionality when the menu buttons are clicked.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menuAbout -> {
            // About icon clicked
            val intent = Intent(context, About::class.java)
            startActivity(intent)
            true
        }
        else -> {
            // Unknown action (not about page) invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * A function that will check if we have the permissions to access the device's external storage
     * else it generates a dialog to gain the permissions.
     */
    private fun setupPermissions(action: String) {
        // Set up permissions
        val externalStoragePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (externalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, ask
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            // Permission has been granted
            when (action) {
                "export" -> {
                    // Export to a file
                    val dbManager = DBManager(requireContext())
                    dbManager.exportDB(requireContext())
                    dbManager.sqlDB!!.close()
                }
                "import" -> {
                    // Open the file manager
                    try {
                        val intent = Intent()
                            .setType("*/*")
                            .setAction(Intent.ACTION_GET_CONTENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                        intent.putExtra("browser_filter_extension_whitelist", "*.db")
                        intent.putExtra("browser_list_layout", "2")
                        startActivityForResult(
                            Intent.createChooser(intent, "Select a file"),
                            FILE_SELECT_CODE
                        )
                    } catch (ex: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.no_file_manager, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * An [onActivityResult] method that is used to retrieve the file URI selected to import.
     *
     * @param requestCode
     * @param resultCode
     * @param data Contains the URI of the selected file.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            // If the selection didn't work, don't do anything
            Toast.makeText(context, R.string.error_selecting_file, Toast.LENGTH_SHORT).show()
            return
        }

        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            //The uri with the location of the file
            val selectedFile = data?.data

            // We are going to overwrite the database, build an alert dialog to get user confirmation
            val alertDialog = AlertDialog.Builder(requireContext())

            alertDialog.setMessage(resources.getString(R.string.alert_message_import_database))
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                    // Import the file
                    val dbManager = DBManager(requireContext())
                    dbManager.importDB(requireContext(), selectedFile!!)
                    dbManager.sqlDB!!.close()
                }
                .setNegativeButton(resources.getString(R.string.no_cancel)) { dialog, _ ->
                    // Do nothing, close box
                    dialog.cancel()
                }

            val alert = alertDialog.create()
            alert.setTitle(resources.getString(R.string.settings_import_database_button))
            alert.show()
        }
    }
}