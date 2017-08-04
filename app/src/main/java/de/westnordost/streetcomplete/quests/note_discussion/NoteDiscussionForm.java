package de.westnordost.streetcomplete.quests.note_discussion;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.util.InlineAsyncTask;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.app.Activity.RESULT_OK;
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

		View buttonPanel = setButtonsView(R.layout.quest_notediscussion_buttonbar);
		Button buttonOk = (Button) buttonPanel.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickOk();
			}
		});
		Button buttonNo = (Button) buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				skipQuest();
			}
		});
		Button buttonUploadImage = (Button) buttonPanel.findViewById(R.id.buttonUploadImage);
		buttonUploadImage.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				if (Build.VERSION.SDK_INT <19){
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), 1);
				} else {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
					startActivityForResult(intent, 2);
				}

			}
		});

		noteInput = (EditText) contentView.findViewById(R.id.noteInput);
		noteDiscussion = (LinearLayout) contentView.findViewById(R.id.noteDiscussion);

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

	@Override
	@SuppressLint("NewApi")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && data != null && data.getData() != null) {

			Uri uri = null;

			if (requestCode == 1)
			{
				uri = data.getData();
			}
			else if (requestCode == 2)
			{
				uri = data.getData();
				final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
				// Check for the freshest data.
				getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
			}

			final Uri finalUri = uri;

			new AlertDialogBuilder(getContext())
					.setMessage(R.string.confirmation_publish_image)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							new ImageUploadHelper(getContext()).execute(finalUri);
						}
					})
					.setNegativeButton(android.R.string.cancel, null)
					.show();

		}
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
		applyImmediateAnswer(answer);
	}

	@Override public boolean hasChanges()
	{
		return !noteInput.getText().toString().trim().isEmpty();
	}

	private class ImageUploadHelper extends AsyncTask<Uri, String, String>
	{

		private Context mContext;

		ImageUploadHelper (Context context){
			mContext = context;
		}

		private ProgressDialog LoadingDialog;

		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			LoadingDialog = new ProgressDialog(getContext());
			LoadingDialog.setMessage(getResources().getString(R.string.publishing_image));
			LoadingDialog.setIndeterminate(true);
			LoadingDialog.setCancelable(false);
			LoadingDialog.show();
		}

		@Override
		protected String doInBackground(Uri... imageUri) {
			if (!isConnectedToInternet(mContext)) {
				new AlertDialogBuilder(mContext)
						.setMessage(R.string.connection_error)
						.setPositiveButton(android.R.string.ok, null)
						.setNegativeButton(android.R.string.cancel, null)
						.show();
				return null;
			}

			URL url = null;
			String urlToImage = "https://lut.im/";
			try {
				url = new URL("https://lut.im/");
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

			HttpURLConnection conn = null;
			InputStream stream = null;
			DataOutputStream request = null;
			try {
				if (isConnectedToInternet(mContext)) {

					String crlf = "\r\n";
					String hyphens = "--";
					String boundary = "------------------------dd8a045fcc22b35c";
					//check if there is a HTTP 301 Error
					if (url != null) {
						conn = (HttpURLConnection) url.openConnection();
					}
					String location = conn.getHeaderField("Location");
					if (location != null) {
						//if there is follow the new destination
						url = new URL(location);
					}
					conn = (HttpURLConnection) url.openConnection();
					//prepare the connection for upload
					conn.setRequestMethod("POST");
					conn.setUseCaches(false);
					conn.setDoInput(true);
					conn.setDoOutput(true);

					conn.setRequestProperty("User-Agent", "StreetComplete");

					conn.setRequestProperty("Expect", "100-continue");
					conn.setRequestProperty("Accept", "*/*");
					conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

					int request_size = 0;

					//ask for JSON answer
					String answer = hyphens + boundary + crlf;
					answer += "Content-Disposition: form-data; name=\"format\"" + crlf;
					answer += crlf;
					answer += "json" + crlf;
					request_size += answer.length();

					//ask for storage duration
					String duration = hyphens + boundary + crlf;
					duration += "Content-Disposition: form-data; name=\"delete-day\"" + crlf;
					duration += crlf;
					duration += 0 + crlf;
					request_size += duration.length();

					String[] proj = {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
					Cursor cursor = mContext.getContentResolver().query(imageUri[0], proj, null, null, null);
					String fileName = null;
					long size = 0;
					if (cursor != null && cursor.moveToFirst()) {
						fileName = cursor.getString(0);
						size = cursor.getLong(1);
						cursor.close();
					}

					//setup filename and say that octets follow
					String outputInformations = hyphens + boundary + crlf;
					outputInformations += "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + crlf;
					outputInformations += "Content-Type: application/octet-stream" + crlf;
					outputInformations += crlf;
					request_size += outputInformations.length();

					request_size += size;

					//finish the format http post packet
					String endHttp = crlf;
					endHttp += hyphens + boundary + hyphens + crlf;
					request_size += endHttp.length();

					conn.setFixedLengthStreamingMode(request_size);

					//write data
					request = new DataOutputStream(conn.getOutputStream());
					request.writeBytes(answer);
					request.writeBytes(duration);
					request.writeBytes(outputInformations);
					request.flush();
					InputStream streamIn = null;
					try {
						streamIn = mContext.getContentResolver().openInputStream(imageUri[0]);
					} catch (Exception e) {
						e.printStackTrace();
						LoadingDialog.dismiss();
					}
					//read data from the file and write it
					if (streamIn != null) {
						int readed = 0;
						int blockSize = 1024;
						byte[] buffer = new byte[blockSize];
						while (readed != -1) {
							try {
								readed = streamIn.read(buffer);
								if (readed != -1) {
									request.write(buffer, 0, readed);
								}
							} catch (IOException e) {
								e.printStackTrace();
								readed = -1;
								LoadingDialog.dismiss();
							}
						}
					}
					request.writeBytes(endHttp);
					request.flush();

					//get answer
					stream = conn.getInputStream();
				}
			} catch (IOException e1) {
				if (conn != null) {
					stream = conn.getErrorStream();
				} else {
					e1.printStackTrace();
				}
			}

			if (stream != null) {
				//prepare JSON reading
				InputStreamReader isr = new InputStreamReader(stream);
				BufferedReader br = new BufferedReader(isr);
				boolean isReading = true;
				String JSONData;
				String jsonStr = "";
				//get all data in a String
				do {
					try {
						JSONData = br.readLine();
						if (JSONData != null)
							jsonStr += JSONData;
						else
							isReading = false;
					} catch (IOException e) {
						e.printStackTrace();
						isReading = false;
					}
				} while (isReading);
				//parse JSON answer
				try {
					if (request != null)
						request.close();
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					// Parse the JSON to a JSONObject
					JSONObject rootObject = new JSONObject(jsonStr);
					// Get msg (root) element
					// is there an error?
					if (rootObject.has("msg")) {
						//retrieve useful data
						JSONObject msg = rootObject.getJSONObject("msg");
						String hashOutput = msg.getString("short");
						urlToImage += hashOutput;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return urlToImage;
		}

		protected void onPostExecute(String result) {
			noteInput.setText(result);
			LoadingDialog.dismiss();
		}

		private boolean isConnectedToInternet(Context context) {
			//verify the connectivity
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (networkInfo != null) {
				NetworkInfo.State networkState = networkInfo.getState();
				if (networkState.equals(NetworkInfo.State.CONNECTED)) {
					return true;
				}
			}
			return false;
		}
	}
}