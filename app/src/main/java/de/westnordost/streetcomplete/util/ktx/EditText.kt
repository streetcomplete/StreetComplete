package de.westnordost.streetcomplete.util.ktx

import android.widget.EditText

val EditText.numberOrNull: Double? get() =
    text.toString().trim().replace(",", ".").toDoubleOrNull()

val EditText.intOrNull: Int? get() =
    text.toString().trim().toIntOrNull()
