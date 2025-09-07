package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.ui.unit.LayoutDirection

val LayoutDirection.dir: Int get() = when (this) {
    LayoutDirection.Ltr -> 1
    LayoutDirection.Rtl -> -1
}
