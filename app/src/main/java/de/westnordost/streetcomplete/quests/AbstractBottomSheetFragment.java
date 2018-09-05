package de.westnordost.streetcomplete.quests;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public abstract class AbstractBottomSheetFragment extends Fragment
{
	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		LinearLayout bottomSheet = view.findViewById(R.id.bottomSheet);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
		}
	}

	/** Request to close the form through user interaction (back button, clicked other quest,..),
	 *  requires user confirmation if any changes have been made */
	@UiThread public void onClickClose(final Runnable confirmed)
	{
		if (!hasChanges())
		{
			onDiscard();
			confirmed.run();
		}
		else
		{
			new AlertDialogBuilder(getActivity())
					.setMessage(R.string.confirmation_discard_title)
					.setPositiveButton(R.string.confirmation_discard_positive, (dialog, which) ->
					{
						onDiscard();
						confirmed.run();
					})
					.setNegativeButton(R.string.confirmation_discard_negative, null)
					.show();
		}
	}

	protected void onDiscard() {}

	public abstract boolean hasChanges();
}
