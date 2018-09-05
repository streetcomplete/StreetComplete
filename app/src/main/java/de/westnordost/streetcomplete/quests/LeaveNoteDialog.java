package de.westnordost.streetcomplete.quests;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment;

public class LeaveNoteDialog extends DialogFragment
{
	public static final String ARG_QUEST_TITLE = "questTitle";

	private EditText noteInput;

	private QuestAnswerComponent questAnswerComponent;

	private String questTitle;

	public LeaveNoteDialog()
	{
		super();
		questAnswerComponent = new QuestAnswerComponent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_leave_note, container, false);

		Button buttonCancel = view.findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(v -> onClickCancel());
		Button buttonOk = view.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(v -> onClickOk());

		noteInput = view.findViewById(R.id.noteInput);

		return view;
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

	private @Nullable AttachPhotoFragment getAttachPhotoFragment()
	{
		return (AttachPhotoFragment) getChildFragmentManager().findFragmentById(R.id.attachPhotoFragment);
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		setStyle(STYLE_NO_TITLE,R.style.AppTheme_AlertDialog);
		questAnswerComponent.onCreate(getArguments());
		questTitle = getArguments().getString(ARG_QUEST_TITLE);
	}

	@Override
	public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) ctx);
	}

	private void onClickOk()
	{
		String inputText = noteInput.getText().toString().trim();
		if(inputText.isEmpty())
		{
			noteInput.setError(getResources().getString(R.string.quest_generic_error_field_empty));
			return;
		}
		AttachPhotoFragment f = getAttachPhotoFragment();
		questAnswerComponent.onLeaveNote(questTitle, inputText, f != null ? f.getImagePaths() : null);
		dismiss();
	}

	private void onClickCancel()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();
		questAnswerComponent.onSkippedQuest();
		dismiss();
	}
}
