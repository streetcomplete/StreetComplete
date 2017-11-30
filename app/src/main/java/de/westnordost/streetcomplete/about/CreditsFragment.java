package de.westnordost.streetcomplete.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.westnordost.streetcomplete.R;

public class CreditsFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_credits, container, false);

		LinearLayout contributorCredits = view.findViewById(R.id.contributorCredits);
		for(String contributor : readContributors())
		{
			TextView textView = new TextView(getActivity());
			textView.setText(contributor);
			contributorCredits.addView(textView, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		LinearLayout translationCredits = view.findViewById(R.id.translationCredits);
		for(Map.Entry<String,String> translatorsByLanguage : readTranslators())
		{
			String language = translatorsByLanguage.getKey();
			String translators = translatorsByLanguage.getValue();
			View item = inflater.inflate(R.layout.row_credits_translators, translationCredits, false);
			((TextView) item.findViewById(R.id.language)).setText(language);
			((TextView) item.findViewById(R.id.contributors)).setText(translators);
			translationCredits.addView(item);
		}

		TextView translationsCreditsMore = view.findViewById(R.id.translationCreditsMore);
		translationsCreditsMore.setMovementMethod(LinkMovementMethod.getInstance());
		translationsCreditsMore.setText(Html.fromHtml(getString(R.string.credits_translations)));
		TextView contributorMore = view.findViewById(R.id.contributorMore);
		contributorMore.setMovementMethod(LinkMovementMethod.getInstance());
		contributorMore.setText(Html.fromHtml(getString(R.string.credits_contributors)));

		return view;
	}

	@Override public void onStart()
	{
		super.onStart();
		getActivity().setTitle(R.string.about_title_authors);
	}

	private List<String> readContributors()
	{
		try
		{
			InputStream is = getResources().openRawResource(R.raw.credits_contributors);
			YamlReader reader = new YamlReader(new InputStreamReader(is));
			List<String> result = new ArrayList<>((List) reader.read());
			result.add(getString(R.string.credits_and_more));
			return result;
		} catch (YamlException e)
		{
			throw new RuntimeException(e);
		}
	}

	private List<Map.Entry<String,String>> readTranslators()
	{
		try
		{
			InputStream is = getResources().openRawResource(R.raw.credits_translations);
			YamlReader reader = new YamlReader(new InputStreamReader(is));
			Map yml = (Map) reader.read();
			List<Map.Entry<String, String>> result = new ArrayList<>();
			for (Object e : yml.entrySet())
			{
				result.add((Map.Entry<String, String>) e);
			}
			Collections.sort(result, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));
			return result;
		} catch (YamlException e)
		{
			throw new RuntimeException(e);
		}
	}
}
