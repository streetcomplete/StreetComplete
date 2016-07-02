package de.westnordost.osmagent.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import de.westnordost.osmagent.R;

/**
 * Preference that shows a simple number picker
 */
public class NumberPickerPreference extends DialogPreference
		implements NumberPicker.OnValueChangeListener
{
	private static final int DEFAULT_MIN_VALUE = 1;
	private static final int DEFAULT_MAX_VALUE = 100;

	private static final int DEFAULT_VALUE = 1;

	private int value;
	private int minValue;
	private int maxValue;

	public NumberPickerPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs)
	{
		setDialogLayoutResource(R.layout.numberpicker_preference);

		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.NumberPickerPreference);

		minValue = a.getInt(R.styleable.NumberPickerPreference_minValue, DEFAULT_MIN_VALUE);
		maxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAX_VALUE);

		a.recycle();
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		NumberPicker picker = (NumberPicker) view.findViewById(R.id.number_picker);

		picker.setMinValue(minValue);
		picker.setMaxValue(maxValue);
		picker.setValue(value);
		picker.setWrapSelectorWheel(false);
		picker.setOnValueChangedListener(this);
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal)
	{
		value = picker.getValue();
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
	{
		if (restorePersistedValue) value = getPersistedInt(DEFAULT_VALUE);
		else                       value = (Integer) defaultValue;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getInteger(index, DEFAULT_VALUE);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		if(positiveResult)
		{
			if(persistInt(value)) notifyChanged();
		}
	}

	@Override
	public CharSequence getSummary()
	{
		return String.format(super.getSummary().toString(), value);
	}
}
