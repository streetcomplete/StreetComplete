package de.westnordost.streetcomplete.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LutimImageUploader
{

	public interface ImageUploadListener {
		void onImageUploaded(String result);
		void onUploadFailed();
	}

	private ImageUploadListener listener;

	public LutimImageUploader(ImageUploadListener listener){
		this.listener = listener;
	}

	public void upload(String baseUrl, ArrayList<String> imagePaths)
	{
		ArrayList<String> imageLinks = new ArrayList<>();
		try
		{
			for (int i = 0; imagePaths.size() > i; i++)
			{
				if (new File(imagePaths.get(i)).exists())
				{
					MultipartUtility multipart = new MultipartUtility(baseUrl, "UTF-8");
					multipart.addFormField("format", "json");
					multipart.addFormField("keep-exif", "1");
					multipart.addFilePart("file", new File(imagePaths.get(i)));

					String response = multipart.finish();

					File file = new File(imagePaths.get(i));
					file.delete();

					JSONObject msg = new JSONObject(response).getJSONObject("msg");
					String urlToImage = msg.getString("short");
					String finalLinkToImage = baseUrl + urlToImage;

					imageLinks.add(finalLinkToImage);
				}
			}

		} catch (IOException e)
		{
			listener.onUploadFailed();
			throw new RuntimeException(e);
		} catch (JSONException e)
		{
			listener.onUploadFailed();
		}
		listener.onImageUploaded(getLinkText(imageLinks));
	}

	private String getLinkText(ArrayList<String> imageLinks)
	{
		String linkText = "Attached photo(s):\n";
		if (imageLinks != null)
		{
			for (int i = 0; imageLinks.size() > i; i++)
			{
				linkText += imageLinks.get(i);
				linkText += "\n";
			}
			return linkText;
		} else
		{
			return "";
		}
	}
}
