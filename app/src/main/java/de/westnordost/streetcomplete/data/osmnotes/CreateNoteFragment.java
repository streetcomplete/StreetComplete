package de.westnordost.streetcomplete.data.osmnotes;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractBottomSheetFragment;
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment;

public class CreateNoteFragment extends AbstractBottomSheetFragment
{
	private EditText noteInput;
	private View markerLayout;
	private View marker;

	private CreateNoteListener callbackListener;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_create_note, container, false);

		LinearLayout bottomSheet = view.findViewById(R.id.bottomSheet);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
		}

		markerLayout = view.findViewById(R.id.marker_layout_create_note);
		if(savedInstanceState == null)
		{
			markerLayout.startAnimation(createFallDownAnimation());
		}

		marker = view.findViewById(R.id.marker_create_note);

		TextView title = view.findViewById(R.id.title);
		title.setText(R.string.map_btn_create_note);

		ViewGroup buttonPanel = view.findViewById(R.id.buttonPanel);
		buttonPanel.removeAllViews();
		inflater.inflate(R.layout.quest_buttonpanel_ok_cancel, buttonPanel);

		ViewGroup content = view.findViewById(R.id.content);
		content.removeAllViews();
		inflater.inflate(R.layout.form_create_note, content);

		buttonPanel.findViewById(R.id.buttonCancel).setOnClickListener(v -> getActivity().onBackPressed());
		buttonPanel.findViewById(R.id.buttonOk).setOnClickListener(v -> onClickOk());

		noteInput = content.findViewById(R.id.noteInput);

		return view;
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

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState == null)
		{
			// TODO reenable when photos can be uploaded again somewhere #1161
			//getChildFragmentManager().beginTransaction().add(R.id.attachPhotoFragment, new AttachPhotoFragment()).commit();
		}
	}

	@Override public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		callbackListener = (CreateNoteListener) ctx;
	}

	private @Nullable AttachPhotoFragment getAttachPhotoFragment()
	{
		return (AttachPhotoFragment) getChildFragmentManager().findFragmentById(R.id.attachPhotoFragment);
	}

	private boolean closeKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm == null) return false;
		return imm.hideSoftInputFromWindow(noteInput.getWindowToken(), 0);
	}

	private void onClickOk()
	{
		String noteText = noteInput.getText().toString().trim();

		if(noteText.isEmpty())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		if(!closeKeyboard())
		{
			onClickOkAfterKeyboardClosed();
		}
	}

	private void onClickOkAfterKeyboardClosed()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();

		int[] point = new int[2];
		marker.getLocationInWindow(point);
		Point screenPos = new Point(point[0], point[1]);
		screenPos.offset(marker.getWidth()/2, marker.getHeight()/2);

		String noteText = noteInput.getText().toString().trim();
		callbackListener.onLeaveNote(noteText, f != null ? f.getImagePaths() : null, screenPos);

		markerLayout.setVisibility(View.INVISIBLE);
	}

	@Override protected void onDiscard()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();

		markerLayout.setVisibility(View.INVISIBLE);
	}

	@Override public boolean isRejectingClose()
	{
		return !noteInput.getText().toString().trim().isEmpty();
	}
}
