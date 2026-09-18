package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.toNSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual class CurrencyFormatter actual constructor(locale: Locale?) {

    private val formatter = NSNumberFormatter().also {
        (locale ?: appFormattingLocale)?.let { selected -> it.locale = selected.toNSLocale() }
        it.numberStyle = NSNumberFormatterCurrencyStyle
    }

    actual fun format(value: Double): String =
        formatter.stringFromNumber(NSNumber(value)) ?: ""

    actual val currencyCode: String? get() = formatter.currencyCode
}
