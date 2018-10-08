package de.westnordost.streetcomplete.view.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import de.westnordost.streetcomplete.R;

public class RangePickerDialog extends AlertDialog implements DialogInterface.OnClickListener
{
	private final NumberPicker startPicker, endPicker;
	private final OnRangeChangeListener listener;

	public interface OnRangeChangeListener
	{
		void onRangeChange(int startIndex, int endIndex);
	}

	public RangePickerDialog(Context context, OnRangeChangeListener listener, String[] values,
							 Integer startIndex, Integer endIndex, CharSequence title)
	{
		super(context, R.style.Theme_Bubble_Dialog);

		this.listener = listener;

		final LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.dialog_range_picker, null);
		setView(view);
		setTitle(title);
		setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
		setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

		startPicker = view.findViewById(R.id.number_picker_start);
		startPicker.setWrapSelectorWheel(false);
		startPicker.setDisplayedValues(values);
		startPicker.setMinValue(0);
		startPicker.setMaxValue(values.length - 1);
		startPicker.setValue(startIndex != null ? startIndex : 0);

		endPicker = view.findViewById(R.id.number_picker_end);
		endPicker.setWrapSelectorWheel(false);
		endPicker.setDisplayedValues(values);
		endPicker.setMinValue(0);
		endPicker.setMaxValue(values.length - 1);
		endPicker.setValue(endIndex != null ? endIndex : values.length - 1);

		// do not allow keyboard input
		disableEditTextsFocus(startPicker);
		disableEditTextsFocus(endPicker);
	}

	private void disableEditTextsFocus(ViewGroup np) {
		int count = np.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = np.getChildAt(i);
			if (child instanceof ViewGroup) {
				disableEditTextsFocus((ViewGroup) child);
			} else if (child instanceof EditText) {
				child.setFocusable(false);
			}
		}
	}

	@Override public void onClick(DialogInterface dialog, int which)
	{
		switch (which)
		{
			case BUTTON_POSITIVE:
				if (listener != null)
				{
					listener.onRangeChange(startPicker.getValue(), endPicker.getValue());
				}
				dismiss();
				break;
			case BUTTON_NEGATIVE:
				cancel();
				break;
		}
	}
}
