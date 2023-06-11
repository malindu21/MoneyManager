package dev.chester_lloyd.moneymanager.ui.categories

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.chester_lloyd.moneymanager.R
import dev.chester_lloyd.moneymanager.DBManager
import dev.chester_lloyd.moneymanager.ui.ListViewManager
import kotlinx.android.synthetic.main.fragment_categories.*

/**
 * A [Fragment] subclass to show a ListView of categories.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class CategoriesFragment : Fragment() {

    private lateinit var categoriesViewModel: CategoriesViewModel

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
        categoriesViewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_categories, container, false)

        // Launch new category activity with FAB
        val fab: FloatingActionButton = root.findViewById(R.id.fab1)
        fab.setOnClickListener {
            val intent = Intent(context, AddCategory::class.java)
            startActivity(intent)
        }

        return root
    }

    /**
     * An [onResume] method that adds all of the categories to a ListView
     */
    override fun onResume() {
        super.onResume()
        val dbManager = DBManager(requireContext())

        // Get categories as an array list from database
        val listCategories = dbManager.selectCategories()
        dbManager.sqlDB!!.close()

        // Pass this to the list view adaptor and populate
        this.lvCategories.adapter = ListViewManager(
            listCategories.toTypedArray(),
            layoutInflater,
            requireContext(),
            "categories"
        )

        // Show no categories text
        if (listCategories.isEmpty()) {
            this.tvNoCategories.visibility = View.VISIBLE
        } else {
            this.tvNoCategories.visibility = View.INVISIBLE
        }
    }
}