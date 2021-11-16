package de.westnordost.streetcomplete.ktx

import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.N)
fun LocaleList.toList(): List<Locale> = (0 until size()).map { i -> this[i] }

@RequiresApi(Build.VERSION_CODES.N)
fun LocaleList.toTypedArray() = Array<Locale>(size()) { i -> this[i] }
