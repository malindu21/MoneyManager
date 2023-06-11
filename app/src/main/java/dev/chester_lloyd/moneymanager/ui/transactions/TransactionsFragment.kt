package dev.chester_lloyd.moneymanager.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import dev.chester_lloyd.moneymanager.*
import kotlin.collections.ArrayList

/**
 * A [Fragment] subclass to show a tabbed layout containing ListViews of transactions.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class TransactionsFragment : Fragment() {

    private lateinit var transactionsViewModel: TransactionsViewModel
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var selectedTab: Int = 0
    private var categories = ArrayList<Category>()

    /**
     * An [onCreateView] method that sets up the View, tabs and FAB
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
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_transactions, container, false)

        // Get all categories from the database (Add an all one too)
        val dbManager = DBManager(requireContext())
        categories.add(Category(0, "All", 0, 0))
        categories.addAll(dbManager.selectCategories())
        dbManager.sqlDB!!.close()

        // Set up tabs
        tabLayout = root.findViewById(R.id.tabs) as TabLayout
        viewPager = root.findViewById(R.id.viewpager) as ViewPager
        viewPager!!.adapter = MyTabsAdapter(childFragmentManager)
        tabLayout!!.post { tabLayout!!.setupWithViewPager(viewPager) }
        onChangeListener()

        // Launch new transaction activity with fab
        val fab: FloatingActionButton = root.findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(context, AddTransactionCheckRequirements::class.java)
            intent.putExtra("tabID", selectedTab)
            startActivity(intent)
        }

        return root
    }

    /**
     * When the selected tab changes, update the [selectedTab] variable.
     */
    private fun onChangeListener() {
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab.position + 1
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    /**
     * An inner class that loads a [Fragment] containing a ListView of transactions that have been
     * saved under the selected tab's category.
     *
     * @param fm A [FragmentManager].
     */
    private inner class MyTabsAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val tabs = categories.size

        /**
         * Get the [Fragment] to be placed within this page that loads the corresponding ListView
         * of transactions.
         *
         * @param position Position of the tab in the tabbed layout.
         * @return A [Fragment], passing it the ID of the selected tab.
         */
        override fun getItem(position: Int): Fragment {
            var fragment: Fragment? = null
            when (position) {
                position -> fragment = TransactionTabFragment(categories[position].categoryID)
            }
            return fragment!!
        }

        /**
         * Returns number of tabs.
         *
         * @return The number of tabs.
         */
        override fun getCount(): Int {
            return tabs
        }

        /**
         * Get the title of the tab.
         *
         * @param position Position of the tab in the tabbed layout.
         * @return The category's name, or null
         */
        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                position -> return categories[position].name
            }
            return null
        }
    }
}