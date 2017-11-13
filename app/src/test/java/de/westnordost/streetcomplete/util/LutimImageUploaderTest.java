package de.westnordost.streetcomplete.util;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.ApplicationConstants;

public class LutimImageUploaderTest extends TestCase
{
	public void testUploadTwoImagesAndGetUrls() throws URISyntaxException, IOException
	{
		LutimImageUploader imageUploader = new LutimImageUploader(ApplicationConstants.LUTIM_INSTANCE);
		imageUploader.setDeleteAfterDays(1);
		imageUploader.setDeleteOnFirstView(true);

		List<String> filenames = new ArrayList<>(2);
		filenames.add(getClass().getResource("/hai_phong_street.jpg").getFile());
		filenames.add(getClass().getResource("/mandalay_market.jpg").getFile());
		List<String> urls = imageUploader.upload(filenames);

		assertEquals(2, urls.size());

		HttpURLConnection connection;
		connection = (HttpURLConnection) new URL(urls.get(0)).openConnection();
		assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

		connection = (HttpURLConnection) new URL(urls.get(1)).openConnection();
		assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
	}
}