package dev.chester_lloyd.moneymanager

/**
 * A [Class] that stores all the information about an account that the user has saved.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class Account {
    var accountID: Int = 0
    var name: String? = null
    var balance: Double = 0.00
    var icon: Int = 0
    var colour: Int = 0
    var default: Boolean = false

    /**
     * Creates a complete [Account] when all necessary fields have been provided.
     *
     * @param accountID The ID of the account.
     * @param name The name of the account.
     * @param balance The amount of money saved in the account.
     * @param icon The ID of the icon associated with the account.
     * @param colour The ID of the colour associated with the account.
     * @param default Whether this account is the default used for transactions.
     */
    constructor(accountID: Int, name: String, balance: Double, icon: Int, colour: Int, default: Boolean) {
        this.accountID = accountID
        this.name = name
        this.balance = balance
        this.icon = icon
        this.colour = colour
        this.default = default
    }

    /**
     * Creates an empty [Account] and sets its ID to 0.
     */
    constructor() {
        this.accountID = 0
    }

    /**
     * Gets the [Account] as a [String] representation.
     *
     * @return The [Account] in the form of a [String].
     */
    override fun toString(): String {
        return String.format(
            "ID: %d, Name: %s, Balance: %.2f, Icon: %d, Colour: %d",
            accountID,
            name,
            balance,
            icon,
            colour
        )
    }
}