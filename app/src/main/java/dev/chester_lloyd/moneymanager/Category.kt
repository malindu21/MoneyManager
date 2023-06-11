package dev.chester_lloyd.moneymanager

/**
 * A [Class] that stores all the information about a category that the user has saved.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class Category {
    var categoryID: Int = 0
    var name: String = ""
    var icon: Int = 0
    var colour: Int = 0

    /**
     * Creates a complete [Category] when all necessary fields have been provided.
     *
     * @param categoryID The ID of the category.
     * @param name The name of the category.
     * @param icon The ID of the icon associated with the category.
     * @param colour The ID of the colour associated with the category.
     */
    constructor(categoryID: Int, name: String, icon: Int, colour: Int) {
        this.categoryID = categoryID
        this.name = name
        this.icon = icon
        this.colour = colour
    }

    /**
     * Creates an empty [Category] and sets its ID to 0.
     */
    constructor() {
        this.categoryID = 0
    }
}