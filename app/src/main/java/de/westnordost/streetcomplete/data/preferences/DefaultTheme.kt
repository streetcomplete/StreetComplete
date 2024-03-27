package de.westnordost.streetcomplete.data.preferences

import android.os.Build

fun getDefaultTheme(): Theme =
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) Theme.AUTO else Theme.SYSTEM
