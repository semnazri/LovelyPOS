// MODIFIED — Milestone 6
package com.bahri.lovelypos.util

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {
    fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }
    fun formatRupiahWithoutDecimal(amount: Long): String {
        val formatted = formatRupiah(amount)
        return formatted.removeSuffix(",00")
    }
}
