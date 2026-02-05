package de.westnordost.streetcomplete.util.locale

import java.time.format.TextStyle

fun DateTimeTextSymbolStyle.toTextStyle() = when (this) {
    DateTimeTextSymbolStyle.Full -> TextStyle.FULL_STANDALONE
    DateTimeTextSymbolStyle.Short -> TextStyle.SHORT_STANDALONE
    DateTimeTextSymbolStyle.Narrow -> TextStyle.NARROW_STANDALONE
}
