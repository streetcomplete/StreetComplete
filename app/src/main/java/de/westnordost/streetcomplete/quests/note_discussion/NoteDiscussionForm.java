package de.westnordost.streetcomplete.quests.note_discussion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class NoteDiscussionForm extends AbstractQuestFormAnswerFragment
{
	public static final String TEXT = "text";
	public static final String IMAGE_PATHS = "image_paths";

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

		View contentView = setContentView(R.layout.quest_note_discussion);

		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_notediscussion);
		Button buttonOk = buttonPanel.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(v -> onClickOk());
		Button buttonNo = buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(v -> skipQuest());

		noteInput = contentView.findViewById(R.id.noteInput);
		noteDiscussion = contentView.findViewById(R.id.noteDiscussion);

		buttonOtherAnswers.setVisibility(View.GONE);

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		inflateNoteDiscussion(noteDb.get(getQuestId()).getNote());

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
				TextView noteText = getView().findViewById(R.id.noteText);
				noteText.setText(noteComment.text);
				TextView noteAuthor = getView().findViewById(R.id.noteAuthor);
				noteAuthor.setText(commenter);
			}
			else
			{
				ViewGroup discussionItem = (ViewGroup) LayoutInflater.from(getActivity()).inflate(
						R.layout.quest_note_discussion_item, noteDiscussion, false);

				TextView commentInfo = discussionItem.findViewById(R.id.comment_info);
				commentInfo.setText(commenter);

				TextView commentText = discussionItem.findViewById(R.id.comment_text);
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
			case HIDDEN:		return R.string.quest_noteDiscussion_hide;
		}
		throw new RuntimeException();
	}

	protected void onClickOk()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		Bundle answer = new Bundle();
		answer.putString(TEXT, getNoteInput());
		answer.putStringArrayList(IMAGE_PATHS, f != null ? f.getImagePaths() : null);
		applyAnswer(answer);
	}

	@Override public void onDiscard()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();
	}

	@Override public boolean isFormComplete() { return !getNoteInput().isEmpty(); }

	private String getNoteInput() { return noteInput.getText().toString().trim(); }

	@Override public boolean isRejectingClose()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		boolean hasPhotos = f != null && !f.getImagePaths().isEmpty();
		return hasPhotos || !getNoteInput().isEmpty();
	}
}
