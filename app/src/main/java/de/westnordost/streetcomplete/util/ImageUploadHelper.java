package de.westnordost.streetcomplete.util;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.westnordost.streetcomplete.ApplicationConstants;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageUploadHelper extends AsyncTask<String, String, ArrayList<String>>
{

	public interface ImageUploadListener {
		void onImageUploaded(String result);
		void onUploadFailed();
	}

	private ArrayList<String> imagePaths;
	private static final MediaType MEDIA_TYPE = MediaType.parse("image/*");
	private ArrayList<String> imageLinks = new ArrayList<>();
	private ImageUploadListener callbackListener;

	public ImageUploadHelper (ArrayList<String> ImagePaths, ImageUploadListener listener){
		this.imagePaths = ImagePaths;
		this.callbackListener = listener;
	}

	@Override
	protected ArrayList<String> doInBackground(String... params) {
		try
		{
			for (int i = 0; imagePaths.size() > i; i++)
			{
				if (new File(imagePaths.get(i)).exists())
				{
					OkHttpClient client = new OkHttpClient();

					RequestBody requestBody = new MultipartBody.Builder()
							.setType(MultipartBody.FORM)
							.addFormDataPart("format", "json")
							.addFormDataPart("keep-exif", "1")
							.addFormDataPart("file", new File(imagePaths.get(i)).getName(),
									RequestBody.create(MEDIA_TYPE, new File(imagePaths.get(i))))
							.build();

					Request request = new Request.Builder()
							.addHeader("User-Agent", ApplicationConstants.USER_AGENT)
							.url("https://images.mondedie.fr/")
							.post(requestBody)
							.build();

					Response response = client.newCall(request).execute();

					if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

					File file = new File(imagePaths.get(i));
					file.delete();

					String body = response.body().string();

					JSONObject msg = new JSONObject(body).getJSONObject("msg");
					String lutimUrl = msg.getString("short");
					String finalLinkToImage = "https://images.mondedie.fr/" + lutimUrl;

					Log.d("lut.im response body", body);
					Log.d("lut.im link to image", finalLinkToImage);

					imageLinks.add(finalLinkToImage);
				}
			}

		} catch (IOException e)
		{
			Log.e("IOException", e.toString());
			return null;
		} catch (JSONException e)
		{
			Log.e("JSONException", e.toString());
			return null;
		}

		return imageLinks;
	}

	protected void onPostExecute(ArrayList<String> result) {
		String linkText = "Attached photo(s):\n";
		if (result != null)
		{
			for (int i = 0; result.size() > i; i++)
			{
				linkText += result.get(i);
				linkText += "\n";
			}
			callbackListener.onImageUploaded(linkText);
		} else
		{
			callbackListener.onUploadFailed();
		}
	}
}
