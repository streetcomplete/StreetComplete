package de.westnordost.streetcomplete.util.locale

import java.text.NumberFormat
import java.util.Locale

actual class CurrencyFormatter actual constructor(locale: androidx.compose.ui.text.intl.Locale?) {

    private val formatter =
        if (locale == null) NumberFormat.getCurrencyInstance()
        else NumberFormat.getCurrencyInstance(locale.platformLocale)

    actual fun format(value: Double): String =
        formatter.format(value)

    actual val currencyCode: String? get() = formatter.currency?.currencyCode
}
