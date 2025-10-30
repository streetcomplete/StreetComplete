package de.westnordost.streetcomplete.util.ktx

import android.content.res.Resources
import androidx.core.util.TypedValueCompat

/** return the number of pixels for the given density independent pixels */
fun Resources.dpToPx(dp: Number): Float = TypedValueCompat.dpToPx(dp.toFloat(), displayMetrics)
