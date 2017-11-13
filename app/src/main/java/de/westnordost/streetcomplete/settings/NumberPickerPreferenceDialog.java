package de.westnordost.streetcomplete.settings;

import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.NumberPicker;

import de.westnordost.streetcomplete.R;

public class NumberPickerPreferenceDialog extends PreferenceDialogFragmentCompat
{
	private NumberPicker picker;

	@Override protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		picker = view.findViewById(R.id.number_picker);
		picker.setMinValue(getNumberPickerPreference().getMinValue());
		picker.setMaxValue(getNumberPickerPreference().getMaxValue());
		picker.setValue(getNumberPickerPreference().getValue());
		picker.setWrapSelectorWheel(false);
	}

	@Override public void onDialogClosed(boolean positiveResult)
	{
		// hackfix: The Android number picker accepts input via soft keyboard (which makes sense
		// from a UX viewpoint) but is not designed for that. By default, it does not apply the
		// input there. See http://stackoverflow.com/questions/18944997/numberpicker-doesnt-work-with-keyboard
		// A workaround is to clear the focus before saving.
		picker.clearFocus();

		if(positiveResult)
		{
			getNumberPickerPreference().setValue(picker.getValue());
		}
	}

	private NumberPickerPreference getNumberPickerPreference()
	{
		return (NumberPickerPreference) getPreference();
	}
}
