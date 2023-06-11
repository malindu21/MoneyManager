package dev.chester_lloyd.moneymanager.ui.monthlySummary

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import dev.chester_lloyd.moneymanager.MainActivity.Companion.stringBalance
import dev.chester_lloyd.moneymanager.PieManager
import dev.chester_lloyd.moneymanager.R
import dev.chester_lloyd.moneymanager.TableManager
import kotlinx.android.synthetic.main.fragment_monthly_summary_tab.*
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.view.PieChartView
import java.text.SimpleDateFormat
import java.util.*

/**
 * A [Fragment] subclass to show a tabbed layout containing pie charts and a summary table.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class MonthlySummaryTabFragment(private val tab: Int) : Fragment() {

    /**
     * An [onCreateView] method that sets up the View
     *
     * @param inflater The LayoutInflater object
     * @param container The parent view.
     * @param savedInstanceState Fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_monthly_summary_tab, container, false)
    }

    /**
     * An [onResume] method that creates a pie chart per month that there exists a transaction and
     * a table for the summary tab.
     */
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onResume() {
        super.onResume()

        // Get direction based on tab ID
        var direction = "in"
        if (tab == 1) {
            direction = "out"
        }

        // Clear views when coming back to a tab
        llCharts.removeAllViews()

        val pieManager = PieManager(requireContext())
        val dates = pieManager.getAllMonths()

        if (tab == 0 || tab == 1) {
            // Get width of display so we know how large to make things
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val xdpi = displayMetrics.xdpi
            val density = displayMetrics.density

            // For every month in the database, make a pie chart for it
            for (date in dates.indices) {
                // Get the month and year from the date
                val month = SimpleDateFormat("MM").format(dates[date].time)
                val year = dates[date].get(Calendar.YEAR).toString()

                // Set up the text to show the month and year of the pie
                val tvDate = TextView(context)
                tvDate.text = "${SimpleDateFormat("MMMM").format(dates[date].time)}, $year"
                tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28F)
                var top = (30 * density + 0.5f).toInt()
                if (date == 0) {
                    top = 0
                }
                tvDate.setPadding(0, top, 0, (15 * density + 0.5f).toInt())
                llCharts.addView(tvDate)

                val categoryMonthData = pieManager.categoryMonth(month, direction)

                if (categoryMonthData.isEmpty()) {
                    // Show no transaction this month text
                    val tvNoTransactions = TextView(context)
                    tvNoTransactions.text = getString(R.string.no_transactions_this_month)
                    tvNoTransactions.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F)
                    tvNoTransactions.setPadding(0, 0, 0, (15 * density + 0.5f).toInt())
                    llCharts.addView(tvNoTransactions)
                } else {
                    // Set up the pie chart
                    val pieChart = PieChartView(context)
                    pieChart.pieChartData = PieChartData(pieManager.categoryMonth(month, direction))
                        .setHasLabels(true)
//                      .setOnValueTouchListener(new ValueTouchListener());

                    /*  A bit of maths involved here
                     *  px * 160 / dpi = dp
                     *
                     *  Using this formula, calculate what 1 dp would be in pixels, then take of 16dp
                     *  for the left and right hand side margins
                     */
                    val dp = width * 160 / xdpi
                    val margin = width - (width / dp * 16 * 2).toInt()
                    pieChart.layoutParams = ViewGroup.LayoutParams(margin, margin)
                    llCharts.addView(pieChart)
                }
            }
            pieManager.sqlDB!!.close()

            // Show no transactions overall
            val tvNoTransactions = TextView(context)
            tvNoTransactions.text = resources.getText(R.string.no_transactions)

            if (dates.isEmpty()) {
                llCharts.addView(tvNoTransactions)
            }
        } else {
            // Make the summary table
            val tableManager = TableManager(requireContext())
            val summaryTable = tableManager.createTable()

            tableManager.addTableRow(summaryTable, arrayListOf("", "Income", "Expenses"), true, requireActivity())

            // For every month in the database, make a row for it
            for ((count, date) in dates.indices.withIndex()) {
                // Get the month and year from the date
                val month = SimpleDateFormat("MM").format(dates[date].time)
                val year = dates[date].get(Calendar.YEAR).toString()

                // Add year row
                if (count == 0) {
                    // Current year
                    tableManager.addTableRow(summaryTable, arrayListOf(year, "", ""), false, requireActivity())
                } else if (month == "12") {
                    // Previous years
                    tableManager.addTableRow(summaryTable, arrayListOf(year, "", ""), false, requireActivity())
                }

                val monthTotalIn = stringBalance(requireContext(), tableManager.transactionTotal(month, year, "in"), false)
                val monthTotalOut = stringBalance(requireContext(), tableManager.transactionTotal(month, year, "out"), false)

                tableManager.addTableRow(summaryTable, arrayListOf(
                    SimpleDateFormat("MMMM").format(dates[date].time),
                    monthTotalIn,
                    monthTotalOut
                ), false, requireActivity())
            }

            llCharts.addView(summaryTable)
        }
    }
}
