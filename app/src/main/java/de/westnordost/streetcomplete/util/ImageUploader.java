package de.westnordost.streetcomplete.util;

import java.util.List;

public interface ImageUploader
{
	/** Uploads images at the given file system paths path(s) and returns the URLs of the uploaded
	 *  images.
	 * @param imagePaths local file system paths to the images to upload
	 * @return URLs to uploaded images. null on failure */
	List<String> upload(List<String> imagePaths);
}
