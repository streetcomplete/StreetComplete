package de.westnordost.streetcomplete.data.osmnotes;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

	private CreateNoteListener callbackListener;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_quest_answer, container, false);

		LinearLayout bottomSheet = view.findViewById(R.id.bottomSheet);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
		}

		TextView title = view.findViewById(R.id.title);
		title.setText(R.string.action_note);

		ViewGroup buttonPanel = view.findViewById(R.id.buttonPanel);
		buttonPanel.removeAllViews();
		inflater.inflate(R.layout.quest_buttonpanel_ok_cancel, buttonPanel);

		ViewGroup content = view.findViewById(R.id.content);
		content.removeAllViews();
		inflater.inflate(R.layout.create_note, content);

		buttonPanel.findViewById(R.id.buttonCancel).setOnClickListener(v -> getActivity().onBackPressed());
		buttonPanel.findViewById(R.id.buttonOk).setOnClickListener(v -> onClickOk());

		noteInput = content.findViewById(R.id.noteInput);

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState == null)
		{
			getChildFragmentManager().beginTransaction().add(R.id.attachPhotoFragment, new AttachPhotoFragment()).commit();
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

	private void onClickOk()
	{
		String noteText = noteInput.getText().toString().trim();

		if(noteText.isEmpty())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}
		AttachPhotoFragment f = getAttachPhotoFragment();

		callbackListener.onLeaveNote(noteText, f != null ? f.getImagePaths() : null);
	}

	@Override protected void onDiscard()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();
	}

	@Override public boolean hasChanges()
	{
		return !noteInput.getText().toString().trim().isEmpty();
	}
}
