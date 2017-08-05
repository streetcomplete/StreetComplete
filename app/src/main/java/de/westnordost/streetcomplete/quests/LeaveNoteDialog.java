package de.westnordost.streetcomplete.quests;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.app.Activity.RESULT_OK;

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
		Button buttonUploadImage = (Button) view.findViewById(R.id.buttonUploadImage);
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

		noteInput = (EditText) view.findViewById(R.id.noteInput);

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

					//keep EXIF tags
					String keepExif = hyphens + boundary + crlf;
					keepExif += "Content-Disposition: form-data; name=\"keep-exif\"" + crlf;
					keepExif += crlf;
					keepExif += 1 + crlf;
					request_size += keepExif.length();

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
					request.writeBytes(keepExif);
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
			noteInput.setText(result + "\n");
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
