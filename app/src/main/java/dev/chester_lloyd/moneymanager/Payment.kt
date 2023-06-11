package dev.chester_lloyd.moneymanager

/**
 * A [Class] that stores all the information about a payment that the user has saved.
 * @param transaction The [Transaction] that the transaction was paid to or by.
 * @param account The [Account] that the payment was paid to or by.
 * @param amount The amount of money this payment is worth.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
data class Payment(var transaction: Transaction, var account: Account, var amount: Double)