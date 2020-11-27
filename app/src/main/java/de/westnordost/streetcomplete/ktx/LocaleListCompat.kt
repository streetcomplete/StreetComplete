package de.westnordost.streetcomplete.ktx

import androidx.core.os.LocaleListCompat
import java.util.Locale

fun LocaleListCompat.toTypedArray() = Array<Locale>(size()) { i -> this[i] }
