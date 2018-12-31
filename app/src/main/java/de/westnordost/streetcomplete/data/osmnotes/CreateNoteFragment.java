package de.westnordost.streetcomplete.data.osmnotes;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;

import de.westnordost.streetcomplete.R;

public class CreateNoteFragment extends AbstractCreateNoteFragment
{
	private EditText noteInput;
	private ViewGroup markerLayout;
	private ViewGroup centeredMarkerLayout;
	private View marker;

	private CreateNoteListener callbackListener;

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		markerLayout = view.findViewById(R.id.marker_layout_create_note);
		if(savedInstanceState == null)
		{
			markerLayout.startAnimation(createFallDownAnimation());
		}
		centeredMarkerLayout = view.findViewById(R.id.centered_marker_layout_create_note);

		marker = view.findViewById(R.id.marker_create_note);

		noteInput = view.findViewById(R.id.noteInput);

		setTitle(R.string.map_btn_create_note);
		setDescription(R.string.create_new_note_description);

		return view;
	}

	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		centeredMarkerLayout.setPadding(
			getResources().getDimensionPixelSize(R.dimen.quest_form_leftOffset),
			getResources().getDimensionPixelSize(R.dimen.quest_form_topOffset),
			getResources().getDimensionPixelSize(R.dimen.quest_form_rightOffset),
			getResources().getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
		);
	}

	private Animation createFallDownAnimation()
	{
		AnimationSet a = new AnimationSet(false);
		a.setStartOffset(200);

		TranslateAnimation ta = new TranslateAnimation(0,0,0,0,1,-0.2f,0,0);
		ta.setInterpolator(new BounceInterpolator());
		ta.setDuration(400);
		a.addAnimation(ta);

		AlphaAnimation aa = new AlphaAnimation(0,1);
		aa.setInterpolator(new AccelerateInterpolator());
		aa.setDuration(200);
		a.addAnimation(aa);

		return a;
	}

	@Override public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		callbackListener = (CreateNoteListener) ctx;
	}

	@Override protected void onDiscard()
	{
		markerLayout.setVisibility(View.INVISIBLE);
	}

	@Override protected void onLeaveNote(String text, @Nullable List<String> imagePaths)
	{
		if(closeKeyboard()) return;

		int[] point = new int[2];
		marker.getLocationInWindow(point);
		Point screenPos = new Point(point[0], point[1]);
		screenPos.offset(marker.getWidth()/2, marker.getHeight()/2);

		callbackListener.onLeaveNote(text, imagePaths, screenPos);

		markerLayout.setVisibility(View.INVISIBLE);
	}

	private boolean closeKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm == null) return false;
		return imm.hideSoftInputFromWindow(noteInput.getWindowToken(), 0);
	}

	@Override protected int getLayoutResId() { return R.layout.fragment_create_note; }
}
