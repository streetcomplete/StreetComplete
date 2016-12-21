package de.westnordost.streetcomplete.about;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.westnordost.streetcomplete.R;

public class ShowHtmlActivity extends AppCompatActivity
{
	public static final String
			HTML_RESOURCE_ID = "html_raw_id",
			TITLE_STRING_RESOURCE_ID = "title_string_id";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_showhtml);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getIntent().getIntExtra(TITLE_STRING_RESOURCE_ID, -1));

		final TextView textView = (TextView) findViewById(R.id.text);
		textView.setMovementMethod(LinkMovementMethod.getInstance());

		new Thread() { @Override public void run()
		{
			int resourceId = getIntent().getIntExtra(HTML_RESOURCE_ID, -1);
			InputStream inputStream = getResources().openRawResource(resourceId);

			final String html;
			try { html = inputStreamIntoString(inputStream); } catch(IOException e)
			{
				throw new RuntimeException(e);
			}

			runOnUiThread(new Runnable() { @Override public void run()
			{
				textView.setText(Html.fromHtml(html));
			}});
		}}.start();
	}

	private String inputStreamIntoString(InputStream inputStream) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while((length = inputStream.read(buffer))!=-1)
			baos.write(buffer,0,length);

		return baos.toString("UTF-8");
	}
}
