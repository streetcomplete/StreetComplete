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
			TEXT = "text",
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

		String text = getIntent().getStringExtra(TEXT);
		textView.setText(Html.fromHtml(text));
	}
}
