package de.westnordost.streetcomplete.settings;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.oauth.OsmOAuthDialogFragment;

public class SettingsActivity extends AppCompatActivity implements OsmOAuthDialogFragment.Listener
{
	public static final String EXTRA_LAUNCH_AUTH = "de.westnordost.streetcomplete.settings.launch_auth";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		setContentView(R.layout.activity_settings);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if(getIntent().getBooleanExtra(EXTRA_LAUNCH_AUTH, false))
		{
			new OsmOAuthDialogFragment().show(getFragmentManager(), OsmOAuthDialogFragment.TAG);
		}
	}

	@Override public void onOAuthAuthorized()
	{
	}

	@Override protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		OsmOAuthDialogFragment oauthFragment = (OsmOAuthDialogFragment) getFragmentManager()
				.findFragmentByTag(OsmOAuthDialogFragment.TAG);
		if(oauthFragment != null)
		{
			oauthFragment.onNewIntent(intent);
		}
	}
}