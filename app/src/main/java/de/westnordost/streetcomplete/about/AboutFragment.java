package de.westnordost.streetcomplete.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.BuildConfig;
import de.westnordost.streetcomplete.R;

public class AboutFragment extends PreferenceFragment
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.about);

		getPreferenceScreen().findPreference("version").setSummary(BuildConfig.VERSION_NAME);

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
				String translationCredits;
				try {
					translationCredits = creditTranslationsAsHtmlString();
				}
				catch (YamlException e)
				{
					throw new RuntimeException(e);
				}
				showHtmlRes(R.string.about_title_authors,
						String.format(getResources().getString(R.string.authors_html), translationCredits));
				return true;
			}
		});

		getPreferenceScreen().findPreference("privacy").setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				showHtmlRes(R.string.about_title_privacy_statement, getResources().getString(R.string.privacy_html));
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

	private String creditTranslationsAsHtmlString() throws YamlException
	{
		InputStream is = getResources().openRawResource(R.raw.credits_translations);
		YamlReader reader = new YamlReader(new InputStreamReader(is));
		List list = (List) reader.read();
		StringBuilder htmlString = new StringBuilder();
		for (Object o : list)
		{
			Map m = (Map) o;
			Map.Entry pair = (Map.Entry) m.entrySet().iterator().next();
			htmlString.append(pair.getKey()+"<br/>		"+pair.getValue()+"<br/>");
		}
		return htmlString.toString();
	}

	private void showHtmlRes(int titleResourceId, String htmlText)
	{
		Intent intent = new Intent(getActivity(), ShowHtmlActivity.class);
		intent.putExtra(ShowHtmlActivity.TITLE_STRING_RESOURCE_ID, titleResourceId);
		intent.putExtra(ShowHtmlActivity.TEXT, htmlText);
		startActivity(intent);
	}
}
