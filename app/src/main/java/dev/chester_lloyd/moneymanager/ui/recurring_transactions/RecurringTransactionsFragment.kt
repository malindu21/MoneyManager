package dev.chester_lloyd.moneymanager.ui.recurring_transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.chester_lloyd.moneymanager.R
import dev.chester_lloyd.moneymanager.DBManager
import dev.chester_lloyd.moneymanager.ui.ListViewManager
import kotlinx.android.synthetic.main.fragment_recurring_transactions.*

/**
 * A [Fragment] subclass to show a ListView of recurring transactions.
 *
 * @author Chester Lloyd
 * @since 1.5
 */
class RecurringTransactionsFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_recurring_transactions, container, false)
    }

    /**
     * An [onResume] method that adds all of the recurring transactions to a ListView
     */
    override fun onResume() {
        super.onResume()
        val dbManager = DBManager(requireContext())

        // Get recurring transactions as an array list from database
        val listRecurringTransactions = dbManager.selectRecurringTransactions(null, null, null)
        dbManager.sqlDB!!.close()

        // Pass this to the list view adaptor and populate
        this.lvRecurringTransactions.adapter = ListViewManager(
            listRecurringTransactions.toTypedArray(),
            layoutInflater,
            requireContext(),
            "recurring transactions"
        )

        // Show no recurring transactions text
        if (listRecurringTransactions.isEmpty()) {
            this.tvNoRecurringTransactions.visibility = View.VISIBLE
        } else {
            this.tvNoRecurringTransactions.visibility = View.INVISIBLE
        }
    }
}