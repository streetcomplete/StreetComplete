package de.westnordost.osmagent.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmagent.Injector;
import de.westnordost.osmagent.R;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;

public class NoteDiscussionForm extends AbstractQuestAnswerFragment
{
	public static final String TEXT = "text";

	@Inject OsmNoteQuestDao noteDb;

	private EditText noteInput;
	private LinearLayout noteDiscussion;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Injector.instance.getApplicationComponent().inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_noteDiscussion_title);
		View contentView = setContentView(R.layout.quest_note_discussion);

		noteInput = (EditText) contentView.findViewById(R.id.noteInput);

		// TODO: not on main thread!
		Note note = noteDb.get(getQuestId()).getNote();
		for(NoteComment noteComment : note.comments)
		{
			LinearLayout discussionItem = new LinearLayout(getActivity());

			String userName = noteComment.isAnonymous() ? "Anonymous" : noteComment.user.displayName;
			String actionName = getActionText(noteComment.action);
			String date = DateFormat.getDateTimeInstance().format(noteComment.date);

			TextView authorView = new TextView(getActivity());
			authorView.setText(actionName + userName + " " + date + ":");
			discussionItem.addView(authorView);

			TextView textView = new TextView(getActivity());
			textView.setText(noteComment.text);
			discussionItem.addView(textView);

			noteDiscussion.addView(discussionItem);
		}


		return view;
	}

	private static String getActionText(NoteComment.Action action)
	{
		// TODO localization
		switch(action)
		{
			case OPENED:	return "Created by ";
			case COMMENTED:	return "";
			case CLOSED:	return "Resolved by ";
			case REOPENED:	return "Reopened by ";
		}
		return null;
	}

	@Override protected void onClickOk()
	{
		String noteText = noteInput.getText().toString().trim();
		if(noteText.isEmpty())
		{
			noteInput.setError(getResources().getString(R.string.quest_generic_error_field_empty));
			return;
		}

		Bundle answer = new Bundle();
		answer.putString(TEXT, noteText);
		applyAnswer(answer);
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = new ArrayList<>();
		answers.add(R.string.quest_noteDiscussion_no);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(itemResourceId == R.string.quest_noteDiscussion_no)
		{
			skipQuest();
			return true;
		}
		return false;
	}

	@Override public boolean hasChanges()
	{
		return !noteInput.getText().toString().trim().isEmpty();
	}
}
