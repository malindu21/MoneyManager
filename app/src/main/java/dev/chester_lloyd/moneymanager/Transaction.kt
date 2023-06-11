package dev.chester_lloyd.moneymanager

import java.util.*

/**
 * A [Class] that stores all the information about a transaction that the user has saved.
 *
 * @author Chester Lloyd
 * @since 1.0
 */
class Transaction {
    var transactionID: Int = 0
    var category = Category()
    var merchant: String? = null
    var details: String? = null
    var amount: Double = 0.00
    var date: Calendar = Calendar.getInstance()
    var transferTransactionID: Int = 0

    /**
     * Creates a complete [Transaction] when all necessary fields have been provided.
     *
     * @param transactionID The ID of the transaction.
     * @param category The [Category] that this transaction is listed under.
     * @param merchant The store/person who handled the transaction.
     * @param details Optional additional details associated about this transaction.
     * @param date The date that the transaction took place.
     * @param amount The total amount that the transaction is worth.
     * @param transferTransactionID The transaction ID of the other transaction when this is
     * involved in a transfer from one account to another.
     */
    constructor(
        transactionID: Int, category: Category, merchant: String, details: String?,
        date: Calendar, amount: Double, transferTransactionID: Int
    ) {
        this.transactionID = transactionID
        this.category = category
        this.merchant = merchant
        this.details = details
        this.date = date
        this.amount = amount
        this.transferTransactionID = transferTransactionID
    }

    /**
     * Creates an empty [Transaction] and sets its ID to 0.
     */
    constructor() {
        this.transactionID = 0
    }
}