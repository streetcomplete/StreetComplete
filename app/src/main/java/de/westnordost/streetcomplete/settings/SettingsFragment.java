package de.westnordost.streetcomplete.settings;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.oauth.OAuth;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.oauth.OAuthWebViewDialogFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
	@Inject SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		addPreferencesFromResource(R.xml.preferences);

		Preference oauth = getPreferenceScreen().findPreference("oauth");
		oauth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				final FragmentManager fm = getFragmentManager();

				OAuthWebViewDialogFragment dlg = OAuthWebViewDialogFragment.create(
						OAuth.createConsumer(), OAuth.createProvider()
				);

				dlg.show(fm, OAuthWebViewDialogFragment.TAG);
				return true;
			}
		});
	}

	@Override
	public void onStart()
	{
		super.onStart();
		updateOsmAuthSummary();
	}

	private void updateOsmAuthSummary()
	{
		Preference oauth = getPreferenceScreen().findPreference("oauth");
		if (OAuth.isAuthorized(prefs))
		{
			oauth.setSummary(R.string.pref_title_authorized_summary);
		}
		else
		{
			oauth.setSummary(R.string.pref_title_not_authorized_summary);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if(key.equals(Prefs.OAUTH_ACCESS_TOKEN_SECRET))
		{
			updateOsmAuthSummary();
		}
	}
}
