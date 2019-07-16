package de.westnordost.streetcomplete.ktx

import android.widget.EditText

val EditText.numberOrNull get() = text.toString().trim().replace(",", ".").toDoubleOrNull()
