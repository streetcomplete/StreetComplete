package de.westnordost.streetcomplete.view.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;

/** A dialog in which you can select one value from a range of values */
public class ValuePickerDialog extends AlertDialog implements DialogInterface.OnClickListener
{
	private final NumberPicker numberPicker;
	private final NumberPicker.OnValueChangeListener listener;
	private final int selectedIndex;

	public ValuePickerDialog(Context context, NumberPicker.OnValueChangeListener listener,
							 String[] values, int selectedIndex, int minIndex, int maxIndex,
							 CharSequence title)
	{
		super(context, R.style.Theme_Bubble_Dialog);

		this.listener = listener;
		this.selectedIndex = selectedIndex;

		final LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.dialog_number_picker, null);
		setView(view);
		setTitle(title);
		setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
		setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

		numberPicker = view.findViewById(R.id.number_picker);

		String[] selectableValues = Arrays.copyOfRange(values, minIndex, maxIndex+1);
		numberPicker.setDisplayedValues(selectableValues);
		numberPicker.setMinValue(minIndex);
		numberPicker.setMaxValue(maxIndex);
		numberPicker.setValue(selectedIndex);
		numberPicker.setWrapSelectorWheel(false);
		// do not allow keyboard input
		EditText input = findInput(numberPicker);
		if(input != null) input.setFocusable(false);
	}

	private EditText findInput(ViewGroup np) {
		int count = np.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = np.getChildAt(i);
			if (child instanceof ViewGroup) {
				findInput((ViewGroup) child);
			} else if (child instanceof EditText) {
				return (EditText) child;
			}
		}
		return null;
	}

	@Override public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case BUTTON_POSITIVE:
				if (listener != null) {
					listener.onValueChange(numberPicker, selectedIndex, numberPicker.getValue());
				}
				break;
			case BUTTON_NEGATIVE:
				cancel();
				break;
		}
	}
}
