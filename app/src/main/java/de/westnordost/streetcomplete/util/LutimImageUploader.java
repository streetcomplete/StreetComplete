package de.westnordost.streetcomplete.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LutimImageUploader implements ImageUploader
{
	private final String baseUrl;

	public LutimImageUploader(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	@Override public List<String> upload(List<String> imagePaths)
	{
		ArrayList<String> imageLinks = new ArrayList<>();
		try
		{
			for (String path : imagePaths)
			{
				File file = new File(path);
				if (file.exists())
				{
					MultipartUtility multipart = new MultipartUtility(baseUrl, "UTF-8");
					multipart.addFormField("format", "json");
					multipart.addFormField("keep-exif", "1");
					multipart.addFilePart("file", file);

					String response = multipart.finish();

					JSONObject msg = new JSONObject(response).getJSONObject("msg");
					String urlToImage = msg.getString("short");
					String finalLinkToImage = baseUrl + urlToImage;

					imageLinks.add(finalLinkToImage);
				}
			}
		}
		catch (IOException e)
		{
			return null;
		}
		catch (JSONException e)
		{
			return null;
		}
		return imageLinks;
	}
}
