package de.westnordost.streetcomplete.util.locale

// Android has no per-user date-format override equivalent to 
// iOS's Language & Region → Date Format setting.
// See https://issuetracker.google.com/issues/151098294
actual fun systemDefaultDateFormatElements(): DateFormatElements? = null
