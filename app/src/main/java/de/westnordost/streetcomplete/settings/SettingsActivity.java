package de.westnordost.streetcomplete.settings;

import android.os.Bundle;

import de.westnordost.streetcomplete.FragmentContainerActivity;
import de.westnordost.streetcomplete.oauth.OsmOAuthDialogFragment;

public class SettingsActivity extends FragmentContainerActivity
{
	public static final String EXTRA_LAUNCH_AUTH = "de.westnordost.streetcomplete.settings.launch_auth";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(getIntent().getBooleanExtra(EXTRA_LAUNCH_AUTH, false))
		{
			new OsmOAuthDialogFragment().show(getSupportFragmentManager(), OsmOAuthDialogFragment.TAG);
		}
		getIntent().putExtra(EXTRA_FRAGMENT_CLASS, SettingsFragment.class.getName());
	}
}