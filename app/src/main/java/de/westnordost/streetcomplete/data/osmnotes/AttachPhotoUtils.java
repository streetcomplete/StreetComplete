package de.westnordost.streetcomplete.data.osmnotes;

import java.io.File;
import java.util.List;

import de.westnordost.streetcomplete.util.ImageUploader;

public class AttachPhotoUtils
{
	public static String uploadAndGetAttachedPhotosText(ImageUploader imageUploader, List<String> imagePaths)
	{
		if(imagePaths != null)
		{
			List<String> urls = imageUploader.upload(imagePaths);
			if (urls != null)
			{
				StringBuilder sb = new StringBuilder("\nAttached photo(s):");
				for(String link : urls)
				{
					sb.append("\n");
					sb.append(link);
				}
				return sb.toString();
			}
		}
		return "";
	}

	public static void deleteImages(List<String> imagePaths)
	{
		if(imagePaths != null)
		{
			for (String path : imagePaths)
			{
				File file = new File(path);
				if (file.exists())
				{
					file.delete();
				}
			}
		}
	}
}
