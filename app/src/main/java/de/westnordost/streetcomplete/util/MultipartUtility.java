package de.westnordost.streetcomplete.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import de.westnordost.streetcomplete.ApplicationConstants;

public class MultipartUtility {

	private final String boundary;
	private static final String LINE_FEED = "\r\n";
	private HttpURLConnection httpConnection;
	private String charset;
	private OutputStream outputStream;
	private PrintWriter writer;

	public MultipartUtility(String requestURL, String charset) throws IOException
	{
		this.charset = charset;

		boundary = "---" + System.currentTimeMillis() + "---";
		URL url = new URL(requestURL);
		httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod("POST");
		httpConnection.setUseCaches(false);
		httpConnection.setDoOutput(true);
		httpConnection.setDoInput(true);
		httpConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		httpConnection.setRequestProperty("User-Agent", ApplicationConstants.USER_AGENT);
		outputStream = httpConnection.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
	}

	public void addFormField(String name, String value)
	{
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	public void addFilePart(String fieldName, File uploadFile) throws IOException
	{
		String fileName = uploadFile.getName();
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
		writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		byte[] buffer = new byte[4096];
		int bytesRead;
		try (FileInputStream inputStream = new FileInputStream(uploadFile))
		{
			while ((bytesRead = inputStream.read(buffer)) != -1)
			{
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
		}
		writer.append(LINE_FEED);
		writer.flush();
	}

	public String finish() throws IOException
	{
		StringBuilder response = new StringBuilder();
		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();

		int status = httpConnection.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK)
		{
			try(InputStream is = httpConnection.getInputStream())
			{
				try(BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")))
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						response.append(line);
					}
					httpConnection.disconnect();
				}
			}
		}
		else
		{
			throw new IOException("Server returned non-OK status: " + status);
		}
		return response.toString();
	}
}
