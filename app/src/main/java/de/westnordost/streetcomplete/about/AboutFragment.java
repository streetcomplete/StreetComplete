package de.westnordost.streetcomplete.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.R;

public class AboutFragment extends PreferenceFragment
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.about);

		getPreferenceScreen().findPreference("version").setSummary(ApplicationConstants.VERSION);

		getPreferenceScreen().findPreference("license").setOnPreferenceClickListener(
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

		getPreferenceScreen().findPreference("authors").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				showHtmlRes(R.string.about_title_authors, R.raw.authors);
				return true;
			}
		});

		getPreferenceScreen().findPreference("privacy").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				showHtmlRes(R.string.about_title_privacy_statement, R.raw.privacy);
				return true;
			}
		});

		getPreferenceScreen().findPreference("repository").setOnPreferenceClickListener(
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

		getPreferenceScreen().findPreference("report_error").setOnPreferenceClickListener(
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

		getPreferenceScreen().findPreference("email_feedback").setOnPreferenceClickListener(
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

	private void showHtmlRes(int titleResourceId, int htmlResourceId)
	{
		Intent intent = new Intent(getActivity(), ShowHtmlActivity.class);
		intent.putExtra(ShowHtmlActivity.TITLE_STRING_RESOURCE_ID, titleResourceId);
		intent.putExtra(ShowHtmlActivity.HTML_RESOURCE_ID, htmlResourceId);
		startActivity(intent);
	}
}
