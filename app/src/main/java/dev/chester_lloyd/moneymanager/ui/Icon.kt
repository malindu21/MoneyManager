package dev.chester_lloyd.moneymanager.ui

/**
 * A [data class] that stores attributes for an icon or colour.
 *
 * @param id The ID of the Icon.
 * @param drawable The drawable [Int] of the resource (icon or background colour)
 * @param text The label corresponding to the icon.
 * @param colour An optional colour associated with the icon. Only used for colours.
 * @author Chester Lloyd
 * @since 1.0
 */
data class Icon(val id: Int, val drawable: Int, val text: String, val colour: Int?)