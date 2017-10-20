package de.westnordost.streetcomplete.quests.note_discussion;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.util.InlineAsyncTask;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.streetcomplete.view.NoteImageAdapter;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.FileProvider.getUriForFile;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class NoteDiscussionForm extends AbstractQuestAnswerFragment
{
	private static final String TAG = "NoteDiscussionForm";
	public static final String TEXT = "text";
	public static final String IMAGE_PATHS = "image_paths";

	private ArrayList<String> imagePaths = new ArrayList<>();
	private ArrayList<Bitmap> imageBitmaps = new ArrayList<>();

	File photoFile = null;

	static final int REQUEST_TAKE_PHOTO = 1;

	GridView gridView;

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

		View buttonPanel = setButtonsView(R.layout.quest_notediscussion_buttonbar);
		Button buttonOk = buttonPanel.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickOk();
			}
		});
		Button buttonNo = buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				skipQuest();
			}
		});

		Button takePhoto = view.findViewById(R.id.buttonTakeImage);
		takePhoto.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				takePhoto();
			}
		});

		if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			takePhoto.setVisibility(View.GONE);
		}

		final NoteImageAdapter noteImageAdapter = new NoteImageAdapter(getActivity(), imageBitmaps);
		gridView = view.findViewById(R.id.gridView);
		gridView.setAdapter(noteImageAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
			{
				new AlertDialogBuilder(getActivity())
						.setMessage(R.string.quest_leave_new_note_photo_delete_title)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
						{
							@Override public void onClick(DialogInterface dialog, int which)
							{
								imageBitmaps.remove(position);
								imagePaths.remove(position);
								noteImageAdapter.notifyDataSetChanged();
							}
						})
						.show();
			}
		});

		noteInput = contentView.findViewById(R.id.noteInput);
		noteDiscussion = contentView.findViewById(R.id.noteDiscussion);

		buttonOtherAnswers.setVisibility(View.GONE);

		new InlineAsyncTask<Note>()
		{
			@Override protected Note doInBackground() throws Exception
			{
				return noteDb.get(getQuestId()).getNote();
			}

			@Override public void onSuccess(Note result)
			{
				if(getActivity() != null && result != null)
				{
					inflateNoteDiscussion(result);
				}
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
		}
		throw new RuntimeException();
	}

	private void onClickOk()
	{
		String noteText = noteInput.getText().toString().trim();
		if(noteText.isEmpty())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		Bundle answer = new Bundle();
		answer.putString(TEXT, noteText);
		answer.putStringArrayList(IMAGE_PATHS, imagePaths);
		applyImmediateAnswer(answer);
	}

	private void takePhoto()
	{
		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			try {
				photoFile = createImageFile();
			} catch (IOException e) {
			}
			if (photoFile != null) {
				if (Build.VERSION.SDK_INT > 21) {
					takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(getActivity(), "de.westnordost.streetcomplete.fileprovider", photoFile));
				} else {
					takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				}
				startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			imageBitmaps.add(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
			imagePaths.add(photoFile.toString());
		}
	}

	private File createImageFile() throws IOException
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";

		File file = new File(getActivity().getFilesDir() + File.separator + "images" + File.separator + imageFileName + ".jpg");
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		fos.close();

		return file;
	}

	@Override public boolean hasChanges()
	{
		return !noteInput.getText().toString().trim().isEmpty();
	}

}
