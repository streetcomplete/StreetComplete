package de.westnordost.streetcomplete.quests.note_discussion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.streetcomplete.util.BitmapUtil;
import de.westnordost.streetcomplete.util.TextChangedWatcher;
import de.westnordost.streetcomplete.view.ListAdapter;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class NoteDiscussionForm extends AbstractQuestAnswerFragment
{
	public static final String TEXT = "text";
	public static final String IMAGE_PATHS = "image_paths";

	private Bitmap anonAvatar;

	@Inject OsmNoteQuestDao noteDb;

	private EditText noteInput;
	private View buttonOk;

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

		View contentView = setContentView(R.layout.quest_note_discussion_content);

		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_notediscussion);
		buttonOk = buttonPanel.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(v -> onClickOk());
		Button buttonNo = buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(v -> skipQuest());

		noteInput = contentView.findViewById(R.id.noteInput);
		noteInput.addTextChangedListener(new TextChangedWatcher(this::updateOkButtonEnablement));

		buttonOtherAnswers.setVisibility(View.GONE);

		updateOkButtonEnablement();

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		anonAvatar = BitmapUtil.createBitmapFrom(getResources().getDrawable(R.drawable.ic_osm_anon_avatar));

		inflateNoteDiscussion(noteDb.get(getQuestId()).getNote().comments);

		if(savedInstanceState == null)
		{
			getChildFragmentManager().beginTransaction().add(R.id.attachPhotoFragment, new AttachPhotoFragment()).commit();
		}
	}

	private @Nullable AttachPhotoFragment getAttachPhotoFragment()
	{
		return (AttachPhotoFragment) getChildFragmentManager().findFragmentById(R.id.attachPhotoFragment);
	}

	private void inflateNoteDiscussion(List<NoteComment> comments)
	{
		LinearLayout layout = getView().findViewById(R.id.scrollViewChild);
		RecyclerView discussionView =(RecyclerView) getLayoutInflater().inflate(R.layout.quest_note_discussion_items, layout, false);

		discussionView.setNestedScrollingEnabled(false);
		discussionView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		discussionView.setAdapter(new NoteCommentListAdapter(comments));

		layout.addView(discussionView, 0);
	}

	private void onClickOk()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		Bundle answer = new Bundle();
		answer.putString(TEXT, getNoteText());
		if(f != null) answer.putStringArrayList(IMAGE_PATHS, f.getImagePaths());
		applyAnswer(answer);
	}

	@Override public void onDiscard()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		if(f != null) f.deleteImages();
	}

	private String getNoteText() { return noteInput.getText().toString().trim(); }

	@Override public boolean isRejectingClose()
	{
		AttachPhotoFragment f = getAttachPhotoFragment();
		boolean hasPhotos = f != null && !f.getImagePaths().isEmpty();
		return hasPhotos || !getNoteText().isEmpty();
	}

	private void updateOkButtonEnablement()
	{
		buttonOk.setEnabled(!getNoteText().isEmpty());
	}


	private class NoteCommentListAdapter extends ListAdapter<NoteComment>
	{
		public NoteCommentListAdapter(List<NoteComment> list) { super(list); }

		@NonNull @Override
		public ViewHolder<NoteComment> onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
		{
			return new NoteCommentViewHolder(
				getLayoutInflater().inflate(R.layout.quest_note_discussion_item, parent, false));
		}
	}

	private class NoteCommentViewHolder extends ListAdapter.ViewHolder<NoteComment>
	{
		private ViewGroup commentContainer;
		private ImageView commentAvatar;
		private TextView commentText;
		private TextView commentInfo;
		private TextView commentStatusText;

		public NoteCommentViewHolder(View itemView)
		{
			super(itemView);
			commentContainer = itemView.findViewById(R.id.comment);
			commentAvatar = itemView.findViewById(R.id.comment_avatar);
			commentText = itemView.findViewById(R.id.comment_text);
			commentInfo = itemView.findViewById(R.id.comment_info);
			commentStatusText = itemView.findViewById(R.id.comment_status_text);
		}

		@Override protected void onBind(NoteComment comment)
		{
			CharSequence dateDescription = DateUtils.getRelativeTimeSpanString(
				comment.date.getTime(), new Date().getTime(), MINUTE_IN_MILLIS);

			String userName = comment.user != null
				? comment.user.displayName : getString(R.string.quest_noteDiscussion_anonymous);

			int commentActionResourceId = getNoteCommentActionResourceId(comment.action);
			if(commentActionResourceId != 0)
			{
				commentStatusText.setVisibility(View.VISIBLE);
				commentStatusText.setText(getString(commentActionResourceId, userName, dateDescription));
			} else {
				commentStatusText.setVisibility(View.GONE);
			}

			if(comment.text != null && !comment.text.isEmpty()) {
				commentContainer.setVisibility(View.VISIBLE);
				commentText.setText(comment.text);
				commentInfo.setText(getString(R.string.quest_noteDiscussion_comment2, userName, dateDescription));

				Bitmap bitmap = anonAvatar;
				if(comment.user != null)
				{
					File avatarFile = new File(OsmModule.getAvatarsCacheDirectory(getContext()) + File.separator + comment.user.id);
					if(avatarFile.exists())
					{
						bitmap = BitmapFactory.decodeFile(avatarFile.getPath());
					}
				}
				RoundedBitmapDrawable avatarDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
				avatarDrawable.setCircular(true);
				commentAvatar.setImageDrawable(avatarDrawable);
			} else {
				commentContainer.setVisibility(View.GONE);
			}
		}

		private int getNoteCommentActionResourceId(NoteComment.Action action)
		{
			switch (action)
			{
				case CLOSED:		return R.string.quest_noteDiscussion_closed2;
				case REOPENED:		return R.string.quest_noteDiscussion_reopen2;
				case HIDDEN:		return R.string.quest_noteDiscussion_hide2;
				default:			return 0;
			}
		}
	}
}
