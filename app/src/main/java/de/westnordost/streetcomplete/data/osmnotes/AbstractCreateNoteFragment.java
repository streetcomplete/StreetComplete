package de.westnordost.streetcomplete.data.osmnotes;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractBottomSheetFragment;
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment;
import de.westnordost.streetcomplete.util.TextChangedWatcher;

public abstract class AbstractCreateNoteFragment extends AbstractBottomSheetFragment
{
	private EditText noteInput;
	private View buttonOk;
	private TextView textTitle;
	private TextView textDescription;

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = inflater.inflate(getLayoutResId(), container, false);

		LinearLayout bottomSheet = view.findViewById(R.id.bottomSheet);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
		}

		textTitle = view.findViewById(R.id.title);

		ViewGroup content = view.findViewById(R.id.content);
		content.removeAllViews();
		inflater.inflate(R.layout.form_leave_note, content);

		textDescription = content.findViewById(R.id.description);

		noteInput = content.findViewById(R.id.noteInput);
		noteInput.addTextChangedListener(new TextChangedWatcher(this::updateOkButtonEnablement));

		ViewGroup buttonPanel = view.findViewById(R.id.buttonPanel);
		buttonPanel.removeAllViews();
		inflater.inflate(R.layout.quest_buttonpanel_ok_cancel, buttonPanel);

		buttonPanel.findViewById(R.id.buttonCancel).setOnClickListener(v -> getActivity().onBackPressed());
		buttonOk = buttonPanel.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(v -> onClickOk());

		updateOkButtonEnablement();

		return view;
	}

	public void setTitle(@StringRes int resId) { textTitle.setText(resId); }
	public void setDescription(@StringRes int resId) { textDescription.setText(resId); }

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState == null)
		{
			getChildFragmentManager().beginTransaction().add(R.id.attachPhotoFragment, new AttachPhotoFragment()).commit();
		}
	}

	private @Nullable AttachPhotoFragment getAttachPhotoFragment()
	{
		return (AttachPhotoFragment) getChildFragmentManager().findFragmentById(R.id.attachPhotoFragment);
	}

	private void onClickOk()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		onLeaveNote(getNoteText(), f != null ? f.getImagePaths() : null);
	}

	@Override protected void onDiscard()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();
	}

	@Override public boolean isRejectingClose()
	{
		return !getNoteText().isEmpty();
	}

	private String getNoteText()
	{
		return noteInput.getText().toString().trim();
	}

	private void updateOkButtonEnablement()
	{
		buttonOk.setEnabled(!getNoteText().isEmpty());
	}

	protected abstract void onLeaveNote(String text, @Nullable ArrayList<String> imagePaths);
	@LayoutRes protected abstract int getLayoutResId();
}
