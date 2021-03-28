package de.westnordost.streetcomplete.quests.opening_hours

import android.content.Context
import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.opening_hours.model.Months

object MonthsPickerDialog {

    fun show(context: Context, months: Months?, callback: (Months) -> Unit): AlertDialog {
        val selection = months?.selection ?: BooleanArray(Months.MONTHS_COUNT)

        return AlertDialog.Builder(context)
            .setTitle(R.string.quest_openingHours_chooseMonthsTitle)
            .setMultiChoiceItems(Months.getNames(), selection)  { _, _, _ -> }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> callback(Months(selection)) }
            .show()
    }

}
