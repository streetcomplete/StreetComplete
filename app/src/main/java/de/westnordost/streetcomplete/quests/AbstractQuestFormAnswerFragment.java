package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.util.DefaultAnimationListener;

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
		if(buttonOk.getVisibility() == View.GONE && isFormComplete())
		{
			buttonOk.clearAnimation();
			buttonOk.setVisibility(View.VISIBLE);
			Animation appear = AnimationUtils.loadAnimation(getContext(), R.anim.ok_button_appear);
			buttonOk.startAnimation(appear);
		}
		else if(buttonOk.getVisibility() == View.VISIBLE && !isFormComplete())
		{
			buttonOk.clearAnimation();
			Animation disappear = AnimationUtils.loadAnimation(getContext(), R.anim.ok_button_disappear);
			disappear.setAnimationListener(new DefaultAnimationListener()
			{
				@Override public void onAnimationEnd(Animation animation)
				{
					buttonOk.setVisibility(View.GONE);
				}
			});
			buttonOk.startAnimation(disappear);
		}
	}
	protected abstract void onClickOk();

	public abstract boolean isFormComplete();

	@Override public boolean isRejectingClose()
	{
		return isFormComplete();
	}
}
