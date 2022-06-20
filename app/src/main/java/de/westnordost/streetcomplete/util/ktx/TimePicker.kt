package de.westnordost.streetcomplete.util.ktx

import android.os.Build
import android.widget.TimePicker

private val atLeastM
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

@Suppress("DEPRECATION") // Can be removed when minSdk >= 23
var TimePicker.hourCompat: Int
    get() = if (atLeastM) hour else currentHour
    set(value) = if (atLeastM) hour = value else currentHour = value

@Suppress("DEPRECATION") // Can be removed when minSdk >= 23
var TimePicker.minuteCompat: Int
    get() = if (atLeastM) minute else currentMinute
    set(value) = if (atLeastM) minute = value else currentMinute = value

fun TimePicker.updateTime(hourOfDay: Int, minuteOfHour: Int) {
    hourCompat = hourOfDay
    minuteCompat = minuteOfHour
}
