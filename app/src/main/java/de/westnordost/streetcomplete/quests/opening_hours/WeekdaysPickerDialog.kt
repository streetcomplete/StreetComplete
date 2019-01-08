package de.westnordost.streetcomplete.quests.opening_hours

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays

object WeekdaysPickerDialog {

    fun show(context: Context, weekdays: Weekdays, callback: (Weekdays) -> Unit): AlertDialog {
        val selection = weekdays.selection

        val dlg = AlertDialog.Builder(context)
            .setTitle(R.string.quest_openingHours_chooseWeekdaysTitle)
            .setMultiChoiceItems(Weekdays.getNames(context.resources), selection) { dialog, _, _ ->
                updateDialogOkButtonEnablement(dialog as AlertDialog, selection)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> callback(Weekdays(selection)) }
            .show()

        updateDialogOkButtonEnablement(dlg, selection)
        return dlg
    }

    private fun updateDialogOkButtonEnablement(dlg: AlertDialog, selection: BooleanArray) {
        dlg.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = selection.any { true }
    }
}
