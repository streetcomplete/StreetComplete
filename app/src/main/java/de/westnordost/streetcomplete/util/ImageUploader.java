package de.westnordost.streetcomplete.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.ApplicationConstants;

public class ImageUploader
{
	private final String baseUrl;

	private static final String TAG = "ImageUploader";

	public ImageUploader(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public List<String> upload(List<String> imagePaths)
	{
		ArrayList<String> imageLinks = new ArrayList<>();

		for (String path : imagePaths)
		{
			File file = new File(path);
			if (file.exists())
			{
				try
				{
					HttpURLConnection httpConnection = createConnection(baseUrl+"upload.php");
					httpConnection.setRequestMethod("POST");
					httpConnection.setRequestProperty("Content-Type", URLConnection.guessContentTypeFromName(file.getPath()));
					httpConnection.setRequestProperty("Content-Transfer-Encoding", "binary");
					httpConnection.setRequestProperty("Content-Length", ""+file.length());

					try (OutputStream outputStream = httpConnection.getOutputStream())
					{
						writeToOutputStream(file, outputStream);
					}
					int status = httpConnection.getResponseCode();
					if (status == HttpURLConnection.HTTP_OK)
					{
						String response = StreamUtils.readToString(httpConnection.getInputStream());
						try
						{
							JSONObject jsonResponse = new JSONObject(response);
							String url = jsonResponse.getString("future_url");
							imageLinks.add(url);
						}
						catch (JSONException e)
						{
							imageLinks.add("(error: upload failed)");
							Log.e(TAG, "Upload Failed: Unexpected response \"" + response+"\"", e);
						}
					}
					else
					{
						imageLinks.add("(error: upload failed)");
						String error = StreamUtils.readToString(httpConnection.getErrorStream());
						Log.e(TAG, "Upload failed: Error code " + status + ", Message: \""+error+"\"");
					}

					httpConnection.disconnect();
				}
				// an error here should neither crash the app nor make the whole note upload
				// fail but should not be silently ignored -> middle ground: include error message
				// in note (comment) and log to console
				catch (IOException e)
				{
					imageLinks.add("(error: upload failed)");
					Log.e(TAG, "Upload failed", e);
				}
			}
		}

		return imageLinks;
	}

	public void activate(long noteId)
	{
		try
		{
			HttpURLConnection httpConnection = createConnection(baseUrl + "activate.php");
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Content-Type", "Content-Type: application/json");

			try (OutputStream outputStream = httpConnection.getOutputStream())
			{
				StreamUtils.writeStream("{\"osm_note_id\": " + noteId + "}", outputStream);
			}
			int status = httpConnection.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK)
			{
				String response = StreamUtils.readToString(httpConnection.getInputStream());
				Log.d(TAG, "Activation successful: \"" + response + "\"");
			} else
			{
				String error = StreamUtils.readToString(httpConnection.getErrorStream());
				Log.e(TAG, "Activation failed: Error code " + status + ", Message: \""+error+"\"");
			}
		}
		catch (IOException e)
		{
			Log.e(TAG, "Activation failed", e);
		}
	}

	private static HttpURLConnection createConnection(String url) throws IOException
	{
		URL uploadUrl = new URL(url);
		HttpURLConnection httpConnection = (HttpURLConnection) uploadUrl.openConnection();
		httpConnection.setUseCaches(false);
		httpConnection.setDoOutput(true);
		httpConnection.setDoInput(true);
		httpConnection.setRequestProperty("User-Agent", ApplicationConstants.USER_AGENT);
		return httpConnection;
	}

	private static void writeToOutputStream(File file, OutputStream outputStream) throws IOException
	{
		byte[] buffer = new byte[16384];
		int bytesRead;
		try (FileInputStream inputStream = new FileInputStream(file))
		{
			while ((bytesRead = inputStream.read(buffer)) != -1)
			{
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
		}
	}
}
