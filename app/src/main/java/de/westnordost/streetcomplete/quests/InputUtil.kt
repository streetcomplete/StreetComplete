package de.westnordost.streetcomplete.quests

import android.widget.EditText

object InputUtil {
    fun numberInputToStandardString(input: EditText?) = input?.text?.toString().orEmpty().trim().replace(",", ".")
}
