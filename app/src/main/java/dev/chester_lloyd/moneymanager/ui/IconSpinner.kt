package dev.chester_lloyd.moneymanager.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import dev.chester_lloyd.moneymanager.R

/**
 * A [BaseAdapter] subclass that creates a spinner that contains an icon and a label.
 *
 * @param context Context.
 * @param icons An [Array] of [Icon] objects that will be used as the icon in each row.
 * @param backgrounds An [Array] of [Icon] objects that will be used as background colour of the
 * icon in each row.
 * @param spinnerType The type of spinner, colour or icon.
 * @author Chester Lloyd
 * @since 1.0
 * @suppress NAME_SHADOWING
 */
@Suppress("NAME_SHADOWING")
class IconSpinner (
    context: Context,
    private var icons: Array<Icon>?,
    private var backgrounds: Array<Icon>?,
    private var balances: Array<String>?,
    private var spinnerType: String
) : BaseAdapter() {

    private var inflter: LayoutInflater = LayoutInflater.from(context)

    /**
     * Returns number of items within the spinner.
     *
     * @return The number of items within the spinner.
     */
    override fun getCount(): Int {
        if (icons != null) {
            return icons!!.size
        }
        return backgrounds!!.size
    }

    /**
     * Get the item in the spinner at a given [position] in the spinner.
     *
     * @param position Position of item in the spinner.
     * @return null.
     */
    override fun getItem(position: Int): Any? {
        return null
    }

    /**
     * Get the row ID associated with the specified [position] in the spinner.
     *
     * @param position The position of the item in the spinner whose row ID we want.
     * @return 0
     */
    override fun getItemId(position: Int): Long {
        return 0
    }

    /**
     * Creates a new item within the spinner
     *
     * @param position The Position of row in the ListView.
     * @param view A View object
     * @param viewGroup The parent's ViewGroup
     * @return A View for a row in the ListView.
     * @suppress InflateParams as the layout is inflating without a Parent
     */
    @SuppressLint("InflateParams")
    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        val view: View?

        when (spinnerType) {
            "icon" -> {
                // If we are adding icons to the spinner
                view = inflter.inflate(R.layout.spinner_icon, null)
                val icon = view.findViewById(R.id.ivAccountIcon) as ImageView
                val names = view.findViewById(R.id.tvIconName) as TextView

                icon.setImageResource(icons!![position].drawable)
                if (backgrounds != null) {
                    icon.setBackgroundResource(backgrounds!![position].drawable)
                }
                names.text = icons!![position].text
            }
            "colour" -> {
                // If we are adding anything else, i.e. colours
                view = inflter.inflate(R.layout.spinner_colour, null)
                val colour = view.findViewById(R.id.ivAccountColour) as ImageView
                val names = view.findViewById(R.id.tvColourName) as TextView
                //colour.setBackgroundColor(Color.rgb(200,83,81))
                colour.setBackgroundResource(backgrounds!![position].drawable)

                names.text = backgrounds!![position].text
            }
            "account" -> {
                // If we are adding accounts (with the balance)
                view = inflter.inflate(R.layout.account, null)
                val icon = view.findViewById(R.id.ivIcon) as ImageView
                val names = view.findViewById(R.id.tvName) as TextView
                val balance = view.findViewById(R.id.tvBalance) as TextView

                icon.setImageResource(icons!![position].drawable)
                icon.setBackgroundResource(backgrounds!![position].drawable)
                names.text = icons!![position].text
                balance.text = balances!![position]
            }
            else -> {
                view = inflter.inflate(R.layout.spinner_colour, null)
            }
        }
        return view
    }
}


