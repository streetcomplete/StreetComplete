package de.westnordost.streetcomplete.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.BuildConfig;
import de.westnordost.streetcomplete.FragmentContainerActivity;
import de.westnordost.streetcomplete.R;

public class AboutFragment extends PreferenceFragmentCompat
{


	@Override public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		addPreferencesFromResource(R.xml.about);

		findPreference("version").setSummary(BuildConfig.VERSION_NAME);

		findPreference("license").setOnPreferenceClickListener( preference ->
		{
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://www.gnu.org/licenses/gpl-3.0.html"));
			startActivity(browserIntent);
			return true;
		});

		findPreference("authors").setOnPreferenceClickListener(preference ->
		{
			getFragmentActivity().setCurrentFragment(new CreditsFragment());
			return true;
		});

		findPreference("privacy").setOnPreferenceClickListener(preference ->
		{
			Fragment f = ShowHtmlFragment.create(
					getResources().getString(R.string.privacy_html) +
					getString(R.string.privacy_html_tileserver) +
					getString(R.string.privacy_html_third_party_quest_sources) +
					getString(R.string.privacy_html_image_upload),
					R.string.about_title_privacy_statement);
			getFragmentActivity().setCurrentFragment(f);
			return true;
		});

		findPreference("repository").setOnPreferenceClickListener(preference ->
		{
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://github.com/westnordost/StreetComplete/"));
			startActivity(browserIntent);
			return true;
		});

		findPreference("report_error").setOnPreferenceClickListener(preference ->
		{
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://github.com/westnordost/StreetComplete/issues/"));
			startActivity(browserIntent);
			return true;
		});

		findPreference("email_feedback").setOnPreferenceClickListener(preference ->
		{
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:"));
			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"osm@westnordost.de"});
			intent.putExtra(Intent.EXTRA_SUBJECT, ApplicationConstants.USER_AGENT + " Feedback");
			if (intent.resolveActivity(getActivity().getPackageManager()) != null)
			{
				startActivity(intent);
				return true;
			}
			return false;
		});
	}

	@Override public void onStart()
	{
		super.onStart();
		getActivity().setTitle(R.string.action_about);
	}

	private FragmentContainerActivity getFragmentActivity()
	{
		return (FragmentContainerActivity) getActivity();
	}
}
