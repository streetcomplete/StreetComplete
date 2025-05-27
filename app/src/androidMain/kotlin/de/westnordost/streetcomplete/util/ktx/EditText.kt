package de.westnordost.streetcomplete.util.ktx

import android.widget.EditText

val EditText.numberOrNull: Double? get() =
    text.toString().trim().replace(",", ".").toDoubleOrNull()

val EditText.intOrNull: Int? get() =
    text.toString().trim().toIntOrNull()

val EditText.nonBlankTextOrNull: String? get() =
    text.toString().trim().ifBlank { null }

val EditText.nonBlankHintOrNull: String? get() =
    hint?.trim()?.toString()?.ifBlank { null }
