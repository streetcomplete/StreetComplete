package de.westnordost.streetcomplete.quests;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import de.westnordost.streetcomplete.R;


import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

public abstract class AbstractBottomSheetFragment extends Fragment
{
	private LinearLayout bottomSheet;
	private BottomSheetBehavior bottomSheetBehavior;
	private View closeButton;
	private final Handler mainHandler = new Handler(Looper.getMainLooper());

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		bottomSheet = view.findViewById(R.id.bottomSheet);
		bottomSheet.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
		{
			// not immediately because this is called during layout change (view.getTop() == 0)
			mainHandler.post(this::updateCloseButtonVisibility);
		});

		closeButton = view.findViewById(R.id.closeButton);
		closeButton.setOnClickListener(v -> getActivity().onBackPressed());

		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

		View titleSpeechBubble = view.findViewById(R.id.speechBubbleTitleContainer);
		titleSpeechBubble.setOnClickListener(v -> {
			if(bottomSheetBehavior.getState() == STATE_EXPANDED)
				bottomSheetBehavior.setState(STATE_COLLAPSED);
			else if(bottomSheetBehavior.getState() == STATE_COLLAPSED)
				bottomSheetBehavior.setState(STATE_EXPANDED);
		});

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
			bottomSheetBehavior.setState(STATE_EXPANDED);
		}

		if(savedInstanceState == null)
		{
			view.findViewById(R.id.speechBubbleTitleContainer).startAnimation(
				AnimationUtils.loadAnimation(getContext(), R.anim.inflate_title_bubble));

			view.findViewById(R.id.speechbubbleContentContainer).startAnimation(
				AnimationUtils.loadAnimation(getContext(), R.anim.inflate_answer_bubble));
		}
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		mainHandler.removeCallbacksAndMessages(null);
	}

	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// I need to do everything myself... (AppCompactActivity only does this after calling this
		// method. Genius!)
		getResources().updateConfiguration(newConfig, getResources().getDisplayMetrics());

		bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.quest_form_peekHeight));

		View bottomSheetContainer = getView().findViewById(R.id.bottomSheetContainer);
		bottomSheetContainer.setBackgroundResource(R.drawable.speechbubbles_gradient_background);
		ViewGroup.LayoutParams params = bottomSheetContainer.getLayoutParams();
		params.width = getResources().getDimensionPixelSize(R.dimen.quest_form_width);
		bottomSheetContainer.setLayoutParams(params);
	}

	private void updateCloseButtonVisibility()
	{
		// this is called asynchronously. It may happen that the activity is already gone when this
		// method is finally called
		if(getActivity() == null) return;

		int toolbarHeight = getActivity().findViewById(R.id.toolbar).getHeight();
		float speechBubbleTopMargin = getResources().getDimension(R.dimen.quest_form_speech_bubble_top_margin);
		boolean coversToolbar = bottomSheet.getTop() < speechBubbleTopMargin + toolbarHeight;
		closeButton.setVisibility(coversToolbar ? View.VISIBLE : View.INVISIBLE);
	}

	/** Request to close the form through user interaction (back button, clicked other quest,..),
	 *  requires user confirmation if any changes have been made */
	@UiThread public void onClickClose(final Runnable confirmed)
	{
		if (!isRejectingClose())
		{
			onDiscard();
			confirmed.run();
		}
		else
		{
			new AlertDialog.Builder(getActivity())
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

	/** @return whether this form should not be closeable without confirmation */
	public boolean isRejectingClose() { return false; }
}
