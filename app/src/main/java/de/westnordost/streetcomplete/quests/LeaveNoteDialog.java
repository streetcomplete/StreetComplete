package de.westnordost.streetcomplete.quests;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;

public class LeaveNoteDialog extends DialogFragment
{
	public static final String ARG_QUEST_TITLE = "questTitle";

	private EditText noteInput;
	private Button buttonOk;

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
		View view = inflater.inflate(R.layout.leave_note, container, false);

		Button buttonCancel = view.findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickCancel();
			}
		});
		buttonOk = view.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickOk();
			}
		});

		noteInput = view.findViewById(R.id.noteInput);

		return view;
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

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		questAnswerComponent.onAttach((OsmQuestAnswerListener) activity);
	}

	private void onClickOk()
	{
		String inputText = noteInput.getText().toString().trim();
		if(inputText.isEmpty())
		{
			noteInput.setError(getResources().getString(R.string.quest_generic_error_field_empty));
			return;
		}

		questAnswerComponent.onLeaveNote(questTitle, inputText);
		dismiss();
	}

	private void onClickCancel()
	{
		questAnswerComponent.onSkippedQuest();
		dismiss();
	}
}
