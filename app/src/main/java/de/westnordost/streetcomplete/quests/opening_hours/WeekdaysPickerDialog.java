package de.westnordost.streetcomplete.quests.opening_hours;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays;


public class WeekdaysPickerDialog
{
	public static AlertDialog show(
		Context context, final Weekdays weekdays, final OnWeekdaysPickedListener callback)
	{
		final boolean[] selection = weekdays.getSelection();

		AlertDialog dlg = new AlertDialog.Builder(context)
			.setTitle(R.string.quest_openingHours_chooseWeekdaysTitle)
			.setMultiChoiceItems(Weekdays.getNames(context.getResources()), selection,
				(dialog, which, isChecked) -> updateDialogOkButtonEnablement((AlertDialog) dialog, selection))
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok,
				(dialog, which) -> callback.onWeekdaysPicked(new Weekdays(selection))).show();

		updateDialogOkButtonEnablement(dlg, selection);
		return dlg;
	}

	private static void updateDialogOkButtonEnablement(AlertDialog dlg, boolean[] selection)
	{
		boolean isAnyChecked = false;
		for(boolean b : selection) isAnyChecked |= b;
		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isAnyChecked);
	}

	public interface OnWeekdaysPickedListener
	{
		void onWeekdaysPicked(Weekdays selected);
	}
}
