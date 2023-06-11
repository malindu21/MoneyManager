package dev.chester_lloyd.moneymanager.ui

import android.widget.EditText
import kotlin.math.abs

/**
 * Manages any currency input.
 *
 * @param editText The edit text input that is to be validated.
 * @author Chester Lloyd
 * @since 1.0
 */
class CurrencyValidator(private val editText: EditText) {

    private var balance: String = ""

    /**
     * Keep a record of the balance before any changes are made.
     *
     * @param s The string from the edit text entry.
     */
    fun beforeTextChangedListener(s: CharSequence) {
        balance = s.toString()
    }

    /**
     * Correct the new balance to conform with correct currency rules.
     *
     * @param s The string from the edit text entry.
     */
    fun onTextChangedListener(s: CharSequence, decimal: String) {
        // Get old position of decimal point
        val oldDecimalPos = balance.indexOf('.')
        var newBalance = ""

        // Where to put the cursor if text is replaced
        var cursorPos = 0
        var decimalCount = 0

        val diff = balance.length - s.length

        // Check if balance contains multiple - or . or over 2dp
        for (i in s.indices) {
            if (s[i] == '-') {
                // Check if current character is a - sign
                if (i == 0) {
                    // Check if this was found at the start, if so add to output string
                    newBalance += s[i]
                } else {
                    // If not, update cursor position to here as this char was removed
                    cursorPos = i
                }
            } else if (s[i] == decimal[0]) {
                // Check if current character is the specified decimal sign

                if (decimalCount == 0) {
                    // Check if no decimal points have been added to the output yet
                    when {
                        i >= oldDecimalPos -> {
                            /*  We are adding the decimal at the position of the old one
                                (or the last in the input), so add it
                             */
                            decimalCount++
                            newBalance += s[i]
                        }
                        i == oldDecimalPos - diff -> {
                            // Some characters have been removed before it, so add this one
                            decimalCount++
                            newBalance += s[i]
                        }
                        else -> {
                            // Do not add this decimal point, update cursor position to here
                            cursorPos = i
                        }
                    }
                } else {
                    // More than 1 decimal point being added, update cursor position to here
                    cursorPos = i
                }
            } else {
                // This is an allowed digit, keep it
                newBalance += s[i]
            }
        }

        if (decimalCount == 1) {
            // Check if a decimal point is present first
            val splitBalance = newBalance.split(decimal)
            if (splitBalance[1].length > 2) {
                // If there are more than 2 numbers after dp, remove any past the 2
                newBalance =
                    splitBalance[0] + decimal + splitBalance[1].dropLast((splitBalance[1].length - 2))
                cursorPos = newBalance.length
            }
        }

        // Update balance and cursor position
        if (editText.text.toString() != newBalance) {
            editText.setText(newBalance)
            // Could try to paste in a load of junk data (not type character by character
            try {
                editText.setSelection(cursorPos)
            } catch (e: IndexOutOfBoundsException) {
                editText.setSelection(1)
            }
        }
    }

    /**
     * Gets the [balance] from the edit text input as a [Double].
     *
     * @return The [balance] without any currency symbols.
     */
    fun getBalance(decimal: String): Double {
        if (editText.text.isNotEmpty()) {
            return editText.text.toString().replace(decimal, ".").toDouble()
        }
        return 0.0
    }

    companion object {
        //  Companion object so basically a Java static class

        /**
         * Gets an amount as a [Double] and adds the currency decimal symbol to it. This also enforces
         * 2dp. This will trim off the negative sign.
         *
         * @param amount The amount to add symbols to.
         * @param decimal The decimal point symbol.
         * @return The [amount] with the currency symbols.
         */
        fun getEditTextAmount(amount: Double, decimal: String): String {
            val absAmount = abs(amount)
            var stringAmount = "0${decimal}00"
            if (absAmount != 0.0) {
                stringAmount = absAmount.toString().replace(".", decimal)

                val splitBalance = absAmount.toString().split(".")
                if (splitBalance.size == 2 && splitBalance[1].length == 1) {
                    stringAmount += "0"
                }
            }
            return stringAmount
        }

        /**
         * Gets an amount as a [Double] and adds the currency decimal symbol to it. This also enforces
         * 2dp. This will return a negative sign if the [amount] is negative.
         *
         * @param amount The amount to add symbols to.
         * @param decimal The decimal point symbol.
         * @return The [amount] with the currency symbols.
         */
        fun getEditTextAmountNeg(amount: Double, decimal: String): String {
            if (amount < 0) {
                return "-" + getEditTextAmount(amount, decimal)
            }
            return getEditTextAmount(amount, decimal)
        }
    }
}