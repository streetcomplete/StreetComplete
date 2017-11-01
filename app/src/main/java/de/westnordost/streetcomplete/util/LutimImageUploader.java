package de.westnordost.streetcomplete.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LutimImageUploader implements ImageUploader
{
	private final String baseUrl;

	private Integer deleteAfterDays;
	private Boolean deleteOnFirstView;

	private static final String TAG = "LutimImageUploader";

	public LutimImageUploader(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	@Override public List<String> upload(List<String> imagePaths)
	{
		ArrayList<String> imageLinks = new ArrayList<>();

		for (String path : imagePaths)
		{
			File file = new File(path);
			if (file.exists())
			{
				try
				{
					MultipartUtility multipart = new MultipartUtility(baseUrl, "UTF-8");
					multipart.addFormField("format", "json");
					if (deleteAfterDays != null)
					{
						multipart.addFormField("delete-day", String.valueOf(deleteAfterDays));
					}
					if (deleteOnFirstView != null)
					{
						multipart.addFormField("first-view", deleteOnFirstView ? "1" : "0");
					}
					multipart.addFilePart("file", file);

					String response = multipart.finish();

					JSONObject jsonResponse = new JSONObject(response);

					if (jsonResponse.getBoolean("success"))
					{
						JSONObject msg = jsonResponse.getJSONObject("msg");
						String urlToImage = msg.getString("short");
						String finalLinkToImage = baseUrl + urlToImage;

						imageLinks.add(finalLinkToImage);
					}
				}
				catch (IOException e)
				{
					Log.e(TAG, "Unable to access the file", e);
				}
				catch (JSONException e)
				{
					Log.e(TAG, "Lutim seems to have a returned a bogus response", e);
				}
			}
		}

		return imageLinks;
	}

	public Integer getDeleteAfterDays()
	{
		return deleteAfterDays;
	}

	public void setDeleteAfterDays(Integer deleteAfterDays)
	{
		this.deleteAfterDays = deleteAfterDays;
	}

	public Boolean getDeleteOnFirstView()
	{
		return deleteOnFirstView;
	}

	public void setDeleteOnFirstView(Boolean deleteOnFirstView)
	{
		this.deleteOnFirstView = deleteOnFirstView;
	}
}
