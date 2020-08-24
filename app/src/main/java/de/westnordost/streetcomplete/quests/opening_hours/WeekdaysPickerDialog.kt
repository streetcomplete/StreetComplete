package de.westnordost.streetcomplete.quests.opening_hours

import android.content.Context
import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays

object WeekdaysPickerDialog {

    fun show(context: Context, weekdays: Weekdays?, callback: (Weekdays) -> Unit): AlertDialog {
        val selection = weekdays?.selection ?: BooleanArray(Weekdays.OSM_ABBR_WEEKDAYS.size)

        return AlertDialog.Builder(context)
            .setTitle(R.string.quest_openingHours_chooseWeekdaysTitle)
            .setMultiChoiceItems(Weekdays.getNames(context.resources), selection) { _, _, _ -> }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> callback(Weekdays(selection)) }
            .show()
    }
}
