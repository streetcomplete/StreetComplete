package de.westnordost.streetcomplete.quests;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.util.DpUtil;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public abstract class AbstractBottomSheetFragment extends Fragment
{
	private LinearLayout bottomSheet;
	private View buttonClose;

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		bottomSheet = view.findViewById(R.id.bottomSheet);
		bottomSheet.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
		{
			// not immediately because this is called during layout change (view.getTop() == 0)
			final Handler handler = new Handler();
			handler.post(this::updateCloseButtonVisibility);
		});

		buttonClose = view.findViewById(R.id.close_btn);
		buttonClose.setOnClickListener(v -> getActivity().onBackPressed());

		BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
		{
			@Override public void onStateChanged(@NonNull View bottomSheet, int newState) { }

			@Override public void onSlide(@NonNull View bottomSheet, float slideOffset)
			{
				updateCloseButtonVisibility();
			}
		});

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		} else
		{
			ObjectAnimator overshootBottomSheet = ObjectAnimator.ofInt(
				bottomSheetBehavior, "peekHeight", (int) DpUtil.toPx(296, getContext()));
			overshootBottomSheet.setInterpolator(new DecelerateInterpolator());
			overshootBottomSheet.setRepeatMode(ValueAnimator.REVERSE);
			overshootBottomSheet.setRepeatCount(1);
			overshootBottomSheet.setStartDelay(200);
			overshootBottomSheet.setDuration(100);
			overshootBottomSheet.start();
		}

		Animation inflateTitleBubble = AnimationUtils.loadAnimation(getContext(), R.anim.inflate_title_bubble);
		inflateTitleBubble.setInterpolator(new DecelerateInterpolator());
		inflateTitleBubble.setDuration(300);
		view.findViewById(R.id.titleSpeechBubble).startAnimation(inflateTitleBubble);

		Animation inflateAnswerBubbleTop = AnimationUtils.loadAnimation(getContext(), R.anim.inflate_answer_bubble_top);
		inflateAnswerBubbleTop.setInterpolator(new DecelerateInterpolator());
		inflateAnswerBubbleTop.setDuration(300);
		view.findViewById(R.id.speechbubbleContent).startAnimation(inflateAnswerBubbleTop);

		Animation inflateAnswerBubbleBottom = AnimationUtils.loadAnimation(getContext(), R.anim.inflate_answer_bubble_bottom);
		inflateAnswerBubbleBottom.setInterpolator(new DecelerateInterpolator());
		inflateAnswerBubbleBottom.setDuration(300);
		view.findViewById(R.id.buttonPanelContainer).startAnimation(inflateAnswerBubbleBottom);
	}

	private void updateCloseButtonVisibility()
	{
		// this is called asynchronously. It may happen that the activity is already gone when this
		// method is finally called
		if(getActivity() == null) return;

		//int toolbarHeight = getActivity().findViewById(R.id.toolbar).getHeight();
		boolean coversToolbarCompletely = bottomSheet.getTop() < -getResources().getDimension(R.dimen.quest_form_speech_bubble_top_margin);
		buttonClose.setVisibility(coversToolbarCompletely ? View.VISIBLE : View.INVISIBLE);
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
