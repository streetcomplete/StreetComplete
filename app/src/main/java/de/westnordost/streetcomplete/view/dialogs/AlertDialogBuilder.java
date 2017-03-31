package de.westnordost.streetcomplete.view.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;

import de.westnordost.streetcomplete.R;

public class AlertDialogBuilder extends AlertDialog.Builder
{
	public AlertDialogBuilder(@NonNull Context context)
	{
		super(context, R.style.AppTheme_AlertDialog);
	}

	public AlertDialogBuilder(@NonNull Context context, @StyleRes int themeResId)
	{
		super(context, themeResId);
	}
}
