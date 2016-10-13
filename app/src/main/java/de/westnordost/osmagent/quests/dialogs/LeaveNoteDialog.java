package de.westnordost.osmagent.quests.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import de.westnordost.osmagent.R;

public class LeaveNoteDialog extends AbstractQuestDialog
{
	private EditText noteInput;
	private Button buttonOk;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.quest_leave_note, container, false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

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

	private void onClickOk()
	{
		String noteText = noteInput.getText().toString().trim();
		if(noteText.isEmpty())
		{
			noteInput.setError(getResources().getString(R.string.quest_generic_error_field_empty));
			return;
		}

		callbackListener.onLeaveNote(questId, noteText);

		dismiss();
	}

	private void onClickCancel()
	{
		callbackListener.onSkippedQuest(questId);

		dismiss();
	}
}
