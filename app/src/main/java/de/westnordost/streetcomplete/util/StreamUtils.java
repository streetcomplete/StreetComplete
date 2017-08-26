package de.westnordost.streetcomplete.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class StreamUtils
{
	public static String readToString(InputStream is) throws IOException
	{
		try
		{
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) != -1)
			{
				result.write(buffer, 0, length);
			}
			return result.toString("UTF-8");
		}
		finally
		{
			if(is != null) is.close();
		}
	}

	public static void writeStream(String string, OutputStream os) throws IOException
	{
		Writer writer = null;
		try
		{
			writer = new OutputStreamWriter(os, "UTF-8");
			writer.write(string);
		}
		finally
		{
			if(writer != null) writer.close();
		}
	}
}
