package de.westnordost.osmagent.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import de.westnordost.osmagent.R;

public class LeaveNoteDialog extends DialogFragment
{
	private EditText noteInput;
	private Button buttonOk;

	private QuestAnswerComponent questAnswerComponent;

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

		Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickCancel();
			}
		});
		buttonOk = (Button) view.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickOk();
			}
		});

		noteInput = (EditText) view.findViewById(R.id.noteInput);

		return view;
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);
		questAnswerComponent.onCreate(getArguments());
	}

	@Override
	public void onAttach(Context ctx)
	{
		super.onAttach(ctx);
		questAnswerComponent.onAttach(ctx);
	}

	private void onClickOk()
	{
		String noteText = noteInput.getText().toString().trim();
		if(noteText.isEmpty())
		{
			noteInput.setError(getResources().getString(R.string.quest_generic_error_field_empty));
			return;
		}

		questAnswerComponent.onLeaveNote(noteText);
		dismiss();
	}

	private void onClickCancel()
	{
		questAnswerComponent.onSkippedQuest();
		dismiss();
	}
}
