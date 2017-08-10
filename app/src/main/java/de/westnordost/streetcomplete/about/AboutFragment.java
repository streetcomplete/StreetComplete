package de.westnordost.streetcomplete.about;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.BuildConfig;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.R;

public class AboutFragment extends PreferenceFragment
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.about);

		getPreferenceScreen().findPreference(Prefs.VERSION).setSummary(BuildConfig.VERSION_NAME);

		getPreferenceScreen().findPreference(Prefs.LICENSE).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
		{
			@Override public boolean onPreferenceClick(Preference preference)
			{
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://www.gnu.org/licenses/gpl-3.0.html"));
				startActivity(browserIntent);
				return true;
			}
		});

		getPreferenceScreen().findPreference(Prefs.AUTHORS).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				getFragmentManager()
						.beginTransaction()
						.replace(R.id.fragment_container, new CreditsFragment())
						.addToBackStack(null)
						.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_right)
						.commit();

				return true;
			}
		});

		getPreferenceScreen().findPreference(Prefs.PRIVACY).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Fragment f = ShowHtmlFragment.create(
						getResources().getString(R.string.privacy_html),
						R.string.about_title_privacy_statement);
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.fragment_container,f)
						.addToBackStack(null)
						.commit();

				return true;
			}
		});

		getPreferenceScreen().findPreference(Prefs.REPOSITORY).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
				{
					@Override public boolean onPreferenceClick(Preference preference)
					{
						Intent browserIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse("https://github.com/westnordost/StreetComplete/"));
						startActivity(browserIntent);
						return true;
					}
				});

		getPreferenceScreen().findPreference(Prefs.REPORT_ERROR).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
		{
			@Override public boolean onPreferenceClick(Preference preference)
			{
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("https://github.com/westnordost/StreetComplete/issues/"));
				startActivity(browserIntent);
				return true;
			}
		});

		getPreferenceScreen().findPreference(Prefs.EMAIL_FEEDBACK).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
		{
			@Override public boolean onPreferenceClick(Preference preference)
			{
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.parse("mailto:"));
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"osm@westnordost.de"});
				intent.putExtra(Intent.EXTRA_SUBJECT, ApplicationConstants.USER_AGENT + " Feedback");
				if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
					startActivity(intent);
					return true;
				}
				return false;
			}
		});
	}

	@Override public void onStart()
	{
		super.onStart();
		getActivity().setTitle(R.string.action_about);
	}
}
