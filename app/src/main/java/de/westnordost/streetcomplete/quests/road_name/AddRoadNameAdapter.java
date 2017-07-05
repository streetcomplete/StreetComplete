package de.westnordost.streetcomplete.quests.road_name;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.Abbreviations;
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale;

import static android.view.Menu.NONE;

public class AddRoadNameAdapter extends RecyclerView.Adapter
{
	private ArrayList<RoadName> data;

	private final Context context;
	private final List<String> languages;
	private final AbbreviationsByLocale abbreviationsByLocale;
	private final Button addLanguageButton;

	public AddRoadNameAdapter(ArrayList<RoadName> data, Context context, List<String> languages,
							  AbbreviationsByLocale abbreviationsByLocale, Button addLanguageButton)
	{
		this.data = data;
		this.context = context;
		this.languages = languages;
		this.abbreviationsByLocale = abbreviationsByLocale;
		this.addLanguageButton = addLanguageButton;

		addLanguageButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				showLanguageSelectMenu(v, getNotAddedLanguages(), new OnLanguageSelected()
				{
					@Override public void onLanguageSelected(String languageCode)
					{
						add(languageCode);
					}
				});
			}
		});

		if(data.isEmpty())
		{
			add(languages.get(0));
		}
		updateAddLanguageButtonVisibility();
	}

	@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new ViewHolder(inflater.inflate(R.layout.quest_roadname_row, parent, false));
	}

	@Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
	{
		((ViewHolder) holder).update(position, data.get(position));
	}

	@Override public int getItemCount()
	{
		return data.size();
	}

	private void updateAddLanguageButtonVisibility()
	{
		addLanguageButton.setVisibility(getNotAddedLanguages().isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void remove(int index)
	{
		data.remove(index);
		notifyItemRemoved(index);
	}

	private void add(String languageCode)
	{
		RoadName entry = new RoadName();
		entry.languageCode = languageCode;
		entry.name = "";
		data.add(entry);
		notifyItemInserted(data.size()-1);
		updateAddLanguageButtonVisibility();
	}

	private List<String> getNotAddedLanguages()
	{
		List<String> result = new ArrayList<>(languages);
		for (RoadName roadName : data)
		{
			result.remove(roadName.languageCode);
		}
		return result;
	}

	public ArrayList<RoadName> getData()
	{
		return data;
	}

	private interface OnLanguageSelected
	{
		void onLanguageSelected(String languageCode);
	}

	private void showLanguageSelectMenu(
			View v, final List<String> languageList, final OnLanguageSelected callback)
	{
		if(languageList.isEmpty()) return;

		PopupMenu m = new PopupMenu(context, v);
		int i = 0;
		for (String languageCode : languageList)
		{
			String displayLanguage = new Locale(languageCode).getDisplayLanguage();
			m.getMenu().add(NONE,i++,NONE, languageCode + " - " + displayLanguage);
		}

		m.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override public boolean onMenuItemClick(MenuItem item)
			{
				callback.onLanguageSelected(languageList.get(item.getItemId()));
				return true;
			}
		});
		m.show();
	}

	private class ViewHolder extends RecyclerView.ViewHolder
	{
		private final AutoCorrectAbbreviationsEditText nameInput;
		private final TextView languageButton;
		private final TextView deleteButton;

		private RoadName roadName;

		public ViewHolder(View itemView)
		{
			super(itemView);

			nameInput = (AutoCorrectAbbreviationsEditText) itemView.findViewById(R.id.nameInput);
			languageButton = (TextView) itemView.findViewById(R.id.languageButton);
			deleteButton = (TextView) itemView.findViewById(R.id.deleteButton);

			nameInput.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}

				@Override public void afterTextChanged(Editable s)
				{
					// TODO
					String name = s.toString();

					boolean isScriptOrNumbers = name.matches("[\\p{InThai}\\d\\W]*");
					if(!isScriptOrNumbers)
					{
						nameInput.setError("TEST TEST");
					}

					roadName.name = name;
				}
			});
		}

		public void update(final int index, RoadName rn)
		{
			final boolean isFirst = index == 0;

			deleteButton.setVisibility(isFirst ? View.INVISIBLE : View.VISIBLE);
			languageButton.setVisibility(languages.size() > 1 ? View.VISIBLE : View.INVISIBLE);

			this.roadName = rn;

			nameInput.setText(roadName.name);
			languageButton.setText(roadName.languageCode);

			// first entry is bold (the first entry is supposed to be the "default language", I
			// hope that comes across to the users like this. Otherwise, a text hint is necessary)
			languageButton.setTypeface(null, isFirst ? Typeface.BOLD : Typeface.NORMAL);
			nameInput.setTypeface(null, isFirst ? Typeface.BOLD : Typeface.NORMAL);

			deleteButton.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					remove(index);
				}
			});

			languageButton.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					List<String> notAddedLanguages = getNotAddedLanguages();
					// in first entry user may select "unspecified language" to cover cases where
					// the default name is no specific language. I.e. see
					// https://wiki.openstreetmap.org/wiki/Multilingual_names#Sardegna_.28Sardinia.29
					if(isFirst)
					{
						notAddedLanguages.add(null);
					}

					showLanguageSelectMenu(v, getNotAddedLanguages(), new OnLanguageSelected()
					{
						@Override public void onLanguageSelected(String languageCode)
						{
							roadName.languageCode = languageCode;
							languageButton.setText(languageCode);
						}
					});
				}
			});

			// load abbreviations from file in separate thread
			new AsyncTask<Void, Void, Abbreviations>()
			{
				@Override protected Abbreviations doInBackground(Void... params)
				{
					return abbreviationsByLocale.get(new Locale(roadName.languageCode));
				}

				@Override protected void onPostExecute(Abbreviations abbreviations)
				{
					nameInput.setAbbreviations(abbreviations);
				}
			}.execute();
		}
	}
}
