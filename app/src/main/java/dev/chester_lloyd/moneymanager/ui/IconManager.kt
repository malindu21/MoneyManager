package dev.chester_lloyd.moneymanager.ui

import android.content.Context
import android.graphics.Color
import dev.chester_lloyd.moneymanager.R

/**
 * A class that handles icons and colours. This will maintain an order of icons and colours and
 * keep its own ID for each that can be safely store in the database.
 *
 * @param context Context.
 * @author Chester Lloyd
 * @since 1.0
 */
class IconManager (context: Context) {

    // Setup the arrays that store the labels
    private var colourNames = arrayOf<String>()
    private var accountNames = arrayOf<String>()
    private var categoryNames = arrayOf<String>()

    // Setup the arrays that store the icons
    var colourIcons = arrayOf<Icon>()
    var accountIcons = arrayOf<Icon>()
    var categoryIcons = arrayOf<Icon>()

    init {
        colourNames = context.resources.getStringArray(R.array.colour_names)
        colourIcons = arrayOf(
            Icon(3, R.drawable.bg_colour_red, colourNames[0], Color.argb(150, 229, 115, 115)),
            Icon(4, R.drawable.bg_colour_orange, colourNames[1], Color.argb(150, 255, 183, 77)),
            Icon(5, R.drawable.bg_colour_yellow, colourNames[2], Color.argb(150, 255, 227, 77)),
            Icon(0, R.drawable.bg_colour_green, colourNames[3], Color.argb(150, 129, 199, 132)),
            Icon(1, R.drawable.bg_colour_blue, colourNames[4], Color.argb(150, 102, 139, 197)),
            Icon(6, R.drawable.bg_colour_indigo, colourNames[5], Color.argb(150, 102, 107, 197)),
            Icon(7, R.drawable.bg_colour_violet, colourNames[6], Color.argb(150, 129, 102, 197)),
            Icon(8, R.drawable.bg_colour_blue_grey, colourNames[7], Color.argb(150, 144, 164, 174)),
            Icon(2, R.drawable.bg_colour_paypal, colourNames[8], Color.argb(150, 250, 50, 50)))
        accountNames = context.resources.getStringArray(R.array.account_names)
        accountIcons = arrayOf(
            Icon(0, R.drawable.ic_account_bank, accountNames[0], null),
            Icon(1, R.drawable.ic_account_business, accountNames[1], null),
            Icon(2, R.drawable.ic_account_cash, accountNames[2], null),
            Icon(3, R.drawable.ic_account_credit_card, accountNames[3], null),
            Icon(4, R.drawable.ic_account_dollar, accountNames[4], null),
            Icon(5, R.drawable.ic_account_gift_card, accountNames[5], null),
            Icon(6, R.drawable.ic_account_joint, accountNames[6], null),
            Icon(7, R.drawable.ic_account_membership_card, accountNames[7], null),
            Icon(8, R.drawable.ic_account_paypal, accountNames[8], null),
            Icon(9, R.drawable.ic_account_travel_card, accountNames[9], null),
            Icon(10, R.drawable.ic_account_wallet, accountNames[10], null))
        categoryNames = context.resources.getStringArray(R.array.category_names)
        categoryIcons = arrayOf(
            Icon(0, R.drawable.ic_category_bar, categoryNames[0], null),
            Icon(1, R.drawable.ic_category_bills_lightbulb, categoryNames[1], null),
            Icon(2, R.drawable.ic_category_bills_lightbulb_2, categoryNames[2], null),
            Icon(3, R.drawable.ic_category_bills_power, categoryNames[3], null),
            Icon(4, R.drawable.ic_category_business, categoryNames[4], null),
            Icon(5, R.drawable.ic_category_cake, categoryNames[5], null),
            Icon(6, R.drawable.ic_category_camera, categoryNames[6], null),
            Icon(7, R.drawable.ic_category_computer_cloud, categoryNames[7], null),
            Icon(8, R.drawable.ic_category_computer_laptop, categoryNames[8], null),
            Icon(9, R.drawable.ic_category_computer_phone, categoryNames[9], null),
            Icon(10, R.drawable.ic_category_computer_servers, categoryNames[10], null),
            Icon(11, R.drawable.ic_category_computer_storage, categoryNames[11], null),
            Icon(12, R.drawable.ic_category_fridge, categoryNames[12], null),
            Icon(13, R.drawable.ic_category_people_child, categoryNames[13], null),
            Icon(14, R.drawable.ic_category_people_people, categoryNames[14], null),
            Icon(15, R.drawable.ic_category_people_person, categoryNames[15], null),
            Icon(16, R.drawable.ic_category_places_cafe, categoryNames[16], null),
            Icon(17, R.drawable.ic_category_places_cinema, categoryNames[17], null),
            Icon(18, R.drawable.ic_category_places_dining, categoryNames[18], null),
            Icon(19, R.drawable.ic_category_places_event, categoryNames[19], null),
            Icon(20, R.drawable.ic_category_places_florist, categoryNames[20], null),
            Icon(21, R.drawable.ic_category_places_home, categoryNames[21], null),
            Icon(22, R.drawable.ic_category_places_hotel, categoryNames[22], null),
            Icon(23, R.drawable.ic_category_places_mail, categoryNames[23], null),
            Icon(24, R.drawable.ic_category_places_pharmacy, categoryNames[24], null),
            Icon(25, R.drawable.ic_category_places_pizza, categoryNames[25], null),
            Icon(26, R.drawable.ic_category_places_receipt, categoryNames[26], null),
            Icon(27, R.drawable.ic_category_places_restaurant, categoryNames[27], null),
            Icon(28, R.drawable.ic_category_places_school, categoryNames[28], null),
            Icon(29, R.drawable.ic_category_shopping_basket, categoryNames[29], null),
            Icon(30, R.drawable.ic_category_shopping_cart, categoryNames[30], null),
            Icon(31, R.drawable.ic_category_shopping_estore, categoryNames[31], null),
            Icon(32, R.drawable.ic_category_shopping_store, categoryNames[32], null),
            Icon(33, R.drawable.ic_category_sports_golf, categoryNames[33], null),
            Icon(34, R.drawable.ic_category_sports_swim, categoryNames[34], null),
            Icon(35, R.drawable.ic_category_stationary, categoryNames[35], null),
            Icon(36, R.drawable.ic_category_stationary_printer, categoryNames[36], null),
            Icon(37, R.drawable.ic_category_subscription_dvr, categoryNames[37], null),
            Icon(38, R.drawable.ic_category_subscription_movie, categoryNames[38], null),
            Icon(39, R.drawable.ic_category_subscription_music, categoryNames[39], null),
            Icon(40, R.drawable.ic_category_subscription_ondemand, categoryNames[40], null),
            Icon(41, R.drawable.ic_category_subscription_radio, categoryNames[41], null),
            Icon(42, R.drawable.ic_category_subscription_subscriptions, categoryNames[42], null),
            Icon(43, R.drawable.ic_category_subscriptions_book, categoryNames[43], null),
            Icon(44, R.drawable.ic_category_ticket, categoryNames[44], null),
            Icon(45, R.drawable.ic_category_transport_bus, categoryNames[45], null),
            Icon(46, R.drawable.ic_category_transport_car, categoryNames[46], null),
            Icon(47, R.drawable.ic_category_transport_flight, categoryNames[47], null),
            Icon(48, R.drawable.ic_category_transport_motorbike, categoryNames[48], null),
            Icon(49, R.drawable.ic_category_transport_station_ev, categoryNames[49], null),
            Icon(50, R.drawable.ic_category_transport_station_gas, categoryNames[50], null),
            Icon(51, R.drawable.ic_category_transport_subway, categoryNames[51], null),
            Icon(52, R.drawable.ic_category_transport_taxi, categoryNames[52], null),
            Icon(53, R.drawable.ic_category_transport_train, categoryNames[53], null),
            Icon(54, R.drawable.ic_category_work, categoryNames[54], null),
            Icon(55, R.drawable.ic_transfer, categoryNames[55], null))
    }

    /**
     * Get the [Icon] in a specified [Array] given its ID. Most useful in finding the [Icon] using
     * the ID given by the database.
     *
     * @param iconArray An [Array] of [Icon] objects that is stored within this class.
     * @return The [Icon] object at this position the array.
     */
    fun getIconByID(iconArray: Array<Icon>, id: Int): Icon {
        for (icon in iconArray.indices) {
            if (iconArray[icon].id == id) {
                return iconArray[icon]
            }
        }
        return colourIcons[0]
    }

    /**
     * Get the position of the [Icon] at a specified [Array] given its ID. Most useful when setting
     * the icon position in a spinner.
     *
     * @param iconArray An [Array] of [Icon] objects that is stored within this class.
     * @param id The ID of the icon, [Icon.id]
     * @return The position of the [Icon] in the given array. 0 if it does not exist.
     */
    fun getIconPositionID(iconArray: Array<Icon>, id: Int): Int {
        for (icon in iconArray.indices) {
            if (iconArray[icon].id == id) {
                return icon
            }
        }
        return 0
    }
}