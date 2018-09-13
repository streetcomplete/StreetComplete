package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;

/** Abstract base class for dialogs in which the user answers a quest with a form he has to fill
 *  out */
public abstract class AbstractQuestFormAnswerFragment extends AbstractQuestAnswerFragment
{
	private Button buttonOk;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		buttonOk = view.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(v ->
		{
			if(!isFormComplete())
			{
				Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			} else
			{
				onClickOk();
			}
		});
		return view;
	}

	protected void checkIsFormComplete()
	{
		if(isFormComplete())
		{
			buttonOk.setVisibility(View.VISIBLE);
			buttonOk.animate()
				.alpha(1).scaleX(1).scaleY(1)
				.setDuration(100)
				.setInterpolator(new DecelerateInterpolator())
				.withEndAction(null);
		}
		else
		{
			buttonOk.animate()
				.alpha(0).scaleX(0.5f).scaleY(0.5f)
				.setDuration(100)
				.setInterpolator(new AccelerateInterpolator())
				.withEndAction(() -> buttonOk.setVisibility(View.GONE));
		}
	}
	protected abstract void onClickOk();

	public abstract boolean isFormComplete();

	@Override public boolean isRejectingClose()
	{
		return isFormComplete();
	}
}
