package de.westnordost.streetcomplete.settings;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.AttributeSet;

public abstract class DialogPreferenceCompat extends DialogPreference
{
	public DialogPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public DialogPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public DialogPreferenceCompat(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public DialogPreferenceCompat(Context context)
	{
		super(context);
	}

	public abstract PreferenceDialogFragmentCompat createDialog();
}
