package dev.chester_lloyd.moneymanager.ui.accounts

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.chester_lloyd.moneymanager.Account
import dev.chester_lloyd.moneymanager.R
import dev.chester_lloyd.moneymanager.DBManager
import dev.chester_lloyd.moneymanager.ui.ListViewManager
import kotlinx.android.synthetic.main.fragment_accounts.*
import java.util.ArrayList

/**
 * A [Fragment] subclass to show a ListView of accounts.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class AccountsFragment : Fragment() {

    private lateinit var accountsViewModel: AccountsViewModel
    private var listAccounts = ArrayList<Account>()

    /**
     * An [onCreateView] method that sets up the View and FAB
     *
     * @param inflater The LayoutInflater object
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

        accountsViewModel = ViewModelProvider(this)[AccountsViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_accounts, container, false)

        // Launch new account activity with FAB
        val fab: FloatingActionButton = root.findViewById(R.id.fab1)
        fab.setOnClickListener {
            val intent = Intent(context, AddAccount::class.java)
            startActivity(intent)
        }

        return root
    }

    /**
     * An [onResume] method that adds all active accounts to a ListView
     */
    override fun onResume() {
        super.onResume()
        val dbManager = DBManager(requireContext())

        // Get accounts as an array list from database
        listAccounts = dbManager.selectAccounts("active", null)
        dbManager.sqlDB!!.close()

        // Pass this to the list view adaptor and populate
        this.lvAccounts.adapter = ListViewManager(
            listAccounts.toTypedArray(),
            layoutInflater,
            requireContext(),
            "categories"
        )

        // Show no accounts text
        if (listAccounts.isEmpty()) {
            this.tvNoAccounts.visibility = View.VISIBLE
        } else {
            this.tvNoAccounts.visibility = View.INVISIBLE
        }
    }

    /**
     * An [onCreateOptionsMenu] method that adds the accounts menu to the toolbar. This includes a
     * transfer funds button.
     *
     * @param menu The options menu to place items.
     * @param inflater The [MenuInflater].
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.accounts, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * An [onOptionsItemSelected] method that adds functionality when the menu buttons are clicked.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menuTransfer -> {
            // Transfer icon clicked
            if (listAccounts.size >= 2) {
                // Go to transfer funds page
                val intent = Intent(context, TransferFunds::class.java)
                startActivity(intent)
            } else {
                // Not enough accounts to make a transfer
                Toast.makeText(
                    this.context,
                    getString(R.string.transfer_min_accounts),
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        }
        else -> {
            // Unknown action (not transfer funds) invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}