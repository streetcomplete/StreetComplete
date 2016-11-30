package de.westnordost.streetcomplete.settings;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;


import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.oauth.OAuthComponent;
import de.westnordost.streetcomplete.oauth.OAuthWebViewDialogFragment;
import oauth.signpost.OAuthConsumer;

public class SettingsActivity extends AppCompatActivity implements OAuthWebViewDialogFragment.OAuthListener
{
	@Inject OAuthComponent oAuthComponent;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		setContentView(R.layout.activity_settings);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onOAuthAuthorized(OAuthConsumer consumer, List<String> permissions)
	{
		oAuthComponent.onOAuthAuthorized(consumer, permissions);
	}

	@Override
	public void onOAuthCancelled()
	{
		oAuthComponent.onOAuthCancelled();
	}
}