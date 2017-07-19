package de.westnordost.streetcomplete.about;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.ListAdapter;

public class CreditsFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.credits, container, false);

		RecyclerView contributorCredits = (RecyclerView) view.findViewById(R.id.contributorCredits);
		contributorCredits.setNestedScrollingEnabled(false);
		contributorCredits.setLayoutManager(
				new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		contributorCredits.setAdapter(new ListAdapter<String>(readContributors())
		{
			@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
			{
				return new ViewHolder(LayoutInflater.from(getActivity()).inflate(
						R.layout.credits_contributors_row, parent, false))
				{
					@Override protected void update(String with)
					{
						((TextView) itemView).setText(with);
					}
				};
			}
		});

		RecyclerView translationCredits = (RecyclerView) view.findViewById(R.id.translationCredits);
		translationCredits.setNestedScrollingEnabled(false);
		translationCredits.setLayoutManager(
				new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		translationCredits.setAdapter(new ListAdapter<Map.Entry<String,String>>(readTranslators())
		{
			@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
			{
				return new ViewHolder(LayoutInflater.from(getActivity()).inflate(
						R.layout.credits_translators_row, parent, false))
				{
					@Override protected void update(Map.Entry<String, String> with)
					{
						((TextView) itemView.findViewById(R.id.language)).setText(with.getKey());
						((TextView) itemView.findViewById(R.id.contributors)).setText(with.getValue());
					}
				};
			}
		});

		TextView translationsCreditsMore = (TextView) view.findViewById(R.id.translationCreditsMore);
		translationsCreditsMore.setMovementMethod(LinkMovementMethod.getInstance());
		translationsCreditsMore.setText(Html.fromHtml(getString(R.string.credits_translations)));
		TextView contributorMore = (TextView) view.findViewById(R.id.contributorMore);
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
			Collections.sort(result, new Comparator<Map.Entry<String, String>>()
			{
				@Override
				public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2)
				{
					return o1.getKey().compareTo(o2.getKey());
				}
			});
			return result;
		} catch (YamlException e)
		{
			throw new RuntimeException(e);
		}
	}
}
