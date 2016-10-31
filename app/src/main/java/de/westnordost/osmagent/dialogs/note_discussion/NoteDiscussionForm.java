package de.westnordost.osmagent.dialogs.note_discussion;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

import de.westnordost.osmagent.Injector;
import de.westnordost.osmagent.R;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.osmagent.dialogs.AbstractQuestAnswerFragment;
import de.westnordost.osmagent.util.InlineAsyncTask;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class NoteDiscussionForm extends AbstractQuestAnswerFragment
{
	private static final String TAG = "NoteDiscussionForm";
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_noteDiscussion_title);
		View contentView = setContentView(R.layout.quest_note_discussion);

		noteInput = (EditText) contentView.findViewById(R.id.noteInput);
		noteDiscussion = (LinearLayout) contentView.findViewById(R.id.noteDiscussion);

		new InlineAsyncTask<Note>()
		{
			@Override protected Note doInBackground() throws Exception
			{
				return noteDb.get(getQuestId()).getNote();
			}

			@Override public void onSuccess(Note result)
			{
				inflateNoteDiscussion(result);
			}

			@Override public void onError(Exception e)
			{
				Log.e(TAG, "Error fetching note quest " + getQuestId() + " from DB.", e);
			}
		}.execute();

		return view;
	}

	private void inflateNoteDiscussion(Note note)
	{
		for(NoteComment noteComment : note.comments)
		{
			CharSequence userName;
			if (noteComment.isAnonymous())
			{
				userName = getResources().getString(R.string.quest_noteDiscussion_anonymous);
			} else
			{
				userName = noteComment.user.displayName;
			}

			CharSequence dateDescription = DateUtils.getRelativeTimeSpanString(
					noteComment.date.getTime(), new Date().getTime(), MINUTE_IN_MILLIS);

			CharSequence commenter = String.format(
					getResources().getString(getNoteCommentActionResourceId(noteComment.action)),
					userName, dateDescription);

			if(noteComment == note.comments.get(0))
			{
				TextView noteText = (TextView) getView().findViewById(R.id.noteText);
				noteText.setText(noteComment.text);
				TextView noteAuthor = (TextView) getView().findViewById(R.id.noteAuthor);
				noteAuthor.setText(commenter);

			}
			else
			{
				ViewGroup discussionItem = (ViewGroup) LayoutInflater.from(getActivity()).inflate(
						R.layout.quest_note_discussion_item, noteDiscussion, false);

				TextView commentInfo = (TextView) discussionItem.findViewById(R.id.comment_info);
				commentInfo.setText(commenter);

				TextView commentText = (TextView) discussionItem.findViewById(R.id.comment_text);
				commentText.setText(noteComment.text);

				noteDiscussion.addView(discussionItem);
			}
		}
	}

	private int getNoteCommentActionResourceId(NoteComment.Action action)
	{
		switch (action)
		{
			case OPENED:		return R.string.quest_noteDiscussion_create;
			case COMMENTED:		return R.string.quest_noteDiscussion_comment;
			case CLOSED:		return R.string.quest_noteDiscussion_closed;
			case REOPENED:		return R.string.quest_noteDiscussion_reopen;
		}
		throw new RuntimeException();
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
