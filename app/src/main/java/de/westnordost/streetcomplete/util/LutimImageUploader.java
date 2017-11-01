package de.westnordost.streetcomplete.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LutimImageUploader implements ImageUploader
{
	private final String baseUrl;

	private Integer deleteAfterDays;
	private Boolean keepExif;
	private Boolean deleteOnFirstView;

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
					if(keepExif != null)
					{
						multipart.addFormField("keep-exif", keepExif ? "1" : "0");
					}
					if(deleteAfterDays != null)
					{
						multipart.addFormField("delete-day", String.valueOf(deleteAfterDays));
					}
					if(deleteOnFirstView != null)
					{
						multipart.addFormField("first-view", deleteOnFirstView ? "1" : "0");
					}
					multipart.addFilePart("file", file);

					String response = multipart.finish();

					JSONObject jsonResponse = new JSONObject(response);

					if(jsonResponse.getBoolean("success"))
					{
						JSONObject msg = jsonResponse.getJSONObject("msg");
						String urlToImage = msg.getString("short");
						String finalLinkToImage = baseUrl + urlToImage;

						imageLinks.add(finalLinkToImage);
					}
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

	public Integer getDeleteAfterDays()
	{
		return deleteAfterDays;
	}

	public void setDeleteAfterDays(Integer deleteAfterDays)
	{
		this.deleteAfterDays = deleteAfterDays;
	}

	public Boolean getKeepExif()
	{
		return keepExif;
	}

	public void setKeepExif(Boolean keepExif)
	{
		this.keepExif = keepExif;
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
