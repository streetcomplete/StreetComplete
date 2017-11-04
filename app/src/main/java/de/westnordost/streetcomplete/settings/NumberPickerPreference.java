package de.westnordost.streetcomplete.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.AttributeSet;

import de.westnordost.streetcomplete.R;

/**
 * Preference that shows a simple number picker
 */
public class NumberPickerPreference extends DialogPreferenceCompat
{
	private static final int DEFAULT_MIN_VALUE = 1;
	private static final int DEFAULT_MAX_VALUE = 100;

	private static final int DEFAULT_VALUE = 1;

	private int value;
	private int minValue;
	private int maxValue;

	public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public NumberPickerPreference(Context context)
	{
		super(context);
		init(context, null);
	}

	@Override public PreferenceDialogFragmentCompat createDialog()
	{
		return new NumberPickerPreferenceDialog();
	}

	public NumberPickerPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs)
	{
		setDialogLayoutResource(R.layout.dialog_number_picker_preference);

		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.NumberPickerPreference);

		minValue = a.getInt(R.styleable.NumberPickerPreference_minValue, DEFAULT_MIN_VALUE);
		maxValue = a.getInt(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAX_VALUE);

		a.recycle();
	}

	@Override protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
	{
		if (restorePersistedValue) value = getPersistedInt(DEFAULT_VALUE);
		else                       value = (Integer) defaultValue;
	}

	@Override protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getInteger(index, DEFAULT_VALUE);
	}

	@Override public CharSequence getSummary()
	{
		return String.format(super.getSummary().toString(), value);
	}

	public int getMinValue()
	{
		return minValue;
	}

	public int getMaxValue()
	{
		return maxValue;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
		persistInt(value);
		notifyChanged();
	}
}
