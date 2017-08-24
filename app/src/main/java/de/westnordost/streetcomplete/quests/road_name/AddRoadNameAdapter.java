package de.westnordost.streetcomplete.quests.road_name;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.Abbreviations;
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale;
import de.westnordost.streetcomplete.util.DefaultTextWatcher;

import static android.view.Menu.NONE;

public class AddRoadNameAdapter extends RecyclerView.Adapter
{
	private ArrayList<RoadName> data;

	private final Context context;
	private final List<String> languages;
	private final AbbreviationsByLocale abbreviationsByLocale;
	private final List<Map<String, String>> roadNameSuggestions;
	private final Button addLanguageButton;

	public AddRoadNameAdapter(ArrayList<RoadName> data, Context context, List<String> languages,
							  AbbreviationsByLocale abbreviationsByLocale,
							  List<Map<String, String>> roadNameSuggestions, Button addLanguageButton)
	{
		if(data.isEmpty())
		{
			data.add(new RoadName(languages.get(0), ""));
		}
		this.data = data;
		this.context = context;
		this.languages = languages;
		this.abbreviationsByLocale = abbreviationsByLocale;
		this.roadNameSuggestions = roadNameSuggestions;
		putDefaultRoadNameSuggestion();
		this.addLanguageButton = addLanguageButton;

		this.addLanguageButton.setOnClickListener(new View.OnClickListener()
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

		updateAddLanguageButtonVisibility();
	}

	/* Road names are usually specified without language information (name=My Street). To provide
	 * meaningful name suggestions per language, it must then be determined in which language this
	 * name tag is. */
	private void putDefaultRoadNameSuggestion()
	{
		String defaultLanguage = languages.get(0);
		for (Map<String, String> roadNames : roadNameSuggestions)
		{
			if(roadNames.containsKey(""))
			{
				// name=A -> name=A, name:de=A (in Germany)
				if(!roadNames.containsKey(defaultLanguage))
				{
					String defaultName = roadNames.get("");
					roadNames.put(defaultLanguage, defaultName);
				}
			}
		}
	}

	private static ArrayList<RoadName> toRoadNameList(Map<String, String> nameByLanguageMap)
	{
		ArrayList<RoadName> result = new ArrayList<>();
		String defaultName = nameByLanguageMap.get("");
		for (Map.Entry<String, String> entry : nameByLanguageMap.entrySet())
		{
			// put default name first
			// (i.e. name=A, name:en=B, name:de=A -> name:de goes first and name is not shown)
			RoadName roadName = new RoadName(entry.getKey(), entry.getValue());
			if(roadName.name.equals(defaultName))
			{
				if(!roadName.languageCode.isEmpty()) result.add(0, roadName);
			}
			else
			{
				result.add(roadName);
			}
		}
		// this is for the case: name=A, name:de=B, name:en=C -> name goes first
		if(!result.get(0).name.equals(defaultName))
		{
			result.add(0, new RoadName("",defaultName));
		}
		return result;
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
		if(index < 1) return;
		data.remove(index);
		notifyItemRemoved(index);

		updateAddLanguageButtonVisibility();
	}

	private void add(@NonNull String languageCode)
	{
		int insertIndex = getItemCount();
		data.add(new RoadName(languageCode, ""));
		notifyItemInserted(insertIndex);

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
			m.getMenu().add(NONE,i++,NONE, getLanguageMenuItemTitle(languageCode));
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

	private String getLanguageMenuItemTitle(String languageCode)
	{
		if(languageCode.isEmpty()) return context.getString(R.string.quest_streetName_menuItem_nolanguage);

		Locale locale = new Locale(languageCode);

		String displayLanguage = locale.getDisplayLanguage();
		String nativeDisplayLanguage = locale.getDisplayLanguage(locale);
		if(displayLanguage.equals(nativeDisplayLanguage))
		{
			return String.format(
					context.getString(R.string.quest_streetName_menuItem_language_simple),
					languageCode, displayLanguage);
		}
		else
		{
			return String.format(
					context.getString(R.string.quest_streetName_menuItem_language_native),
					languageCode, nativeDisplayLanguage, displayLanguage);
		}
	}

	private interface OnRoadNameSuggestionSelected
	{
		void onRoadNameSuggestionSelected(Map<String, String> selection);
	}

	private void showNameSuggestionsMenu(View v,
										 final Map<String, Map<String, String>> roadNameSuggestionsMap,
										 final OnRoadNameSuggestionSelected callback)
	{
		PopupMenu m = new PopupMenu(context, v);

		int i = 0;
		for (Map.Entry<String, Map<String, String>> entry : roadNameSuggestionsMap.entrySet())
		{
			m.getMenu().add(NONE,i++,NONE, entry.getKey());
		}

		m.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override public boolean onMenuItemClick(MenuItem item)
			{

				Map<String, String> selected = roadNameSuggestionsMap.get(item.getTitle().toString());
				callback.onRoadNameSuggestionSelected(selected);
				return true;
			}
		});
		m.show();
	}

	private Map<String,Map<String,String>> getRoadNameSuggestionsByLanguageCode(String languageCode)
	{
		final Map<String,Map<String,String>> roadNameSuggestionsMap = new HashMap<>();
		for (Map<String,String> roadNameSuggestion : roadNameSuggestions)
		{
			String name = roadNameSuggestion.get(languageCode);
			if(name == null) continue;

			// "unspecified language" suggestions
			if(languageCode.isEmpty())
			{
				int defaultNameOccurances = 0;
				for(String other : roadNameSuggestion.values())
				{
					if (name.equals(other)) defaultNameOccurances++;
				}
				// name=A, name:de=A -> do not consider "A" for "unspecified language" suggestion
				if (defaultNameOccurances >= 2) continue;
				// only for name=A, name:de=B, name:en=C,...
			}
			roadNameSuggestionsMap.put(name, roadNameSuggestion);
		}
		return roadNameSuggestionsMap;
	}

	private class ViewHolder extends RecyclerView.ViewHolder
	{
		private final AutoCorrectAbbreviationsEditText nameInput;
		private final TextView languageButton;
		private final TextView deleteButton;
		private final View nameSuggestionsButton;

		private RoadName roadName;

		public ViewHolder(View itemView)
		{
			super(itemView);

			nameInput = itemView.findViewById(R.id.nameInput);
			languageButton = itemView.findViewById(R.id.languageButton);
			deleteButton = itemView.findViewById(R.id.deleteButton);
			nameSuggestionsButton = itemView.findViewById(R.id.nameSuggestions);

			nameInput.addTextChangedListener(new DefaultTextWatcher()
			{
				@Override public void afterTextChanged(Editable s)
				{
					String name = s.toString();
					roadName.name = name;
					if(name.isEmpty())
					{
						boolean hasSuggestions = !getRoadNameSuggestionsByLanguageCode(roadName.languageCode).isEmpty();
						nameSuggestionsButton.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
					}
					else
					{
						nameSuggestionsButton.setVisibility(View.GONE);
					}
				}
			});

			deleteButton.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					// clearing focus is very necessary, otherwise crash
					nameInput.clearFocus();
					remove(getAdapterPosition());
				}
			});
		}

		public void update(final int index, RoadName rn)
		{
			this.roadName = rn;

			final boolean isFirst = index == 0;

			deleteButton.setVisibility(isFirst ? View.INVISIBLE : View.VISIBLE);
			languageButton.setVisibility(languages.size() > 1 ? View.VISIBLE : View.INVISIBLE);

			nameInput.setText(roadName.name);
			nameInput.requestFocus();
			languageButton.setText(roadName.languageCode);

			// first entry is bold (the first entry is supposed to be the "default language", I
			// hope that comes across to the users like this. Otherwise, a text hint is necessary)
			languageButton.setTypeface(null, isFirst ? Typeface.BOLD : Typeface.NORMAL);
			nameInput.setTypeface(null, isFirst ? Typeface.BOLD : Typeface.NORMAL);

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
						notAddedLanguages.add(0,"");
					}

					showLanguageSelectMenu(v, notAddedLanguages, new OnLanguageSelected()
					{
						@Override public void onLanguageSelected(String languageCode)
						{
							roadName.languageCode = languageCode;
							languageButton.setText(languageCode);
							updateAddLanguageButtonVisibility();
							updateNameSuggestions();
						}
					});
				}
			});

			updateNameSuggestions();

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

		private void updateNameSuggestions()
		{
			final Map<String, Map<String, String>> roadNameSuggestionsMap =
					getRoadNameSuggestionsByLanguageCode(roadName.languageCode);

			boolean nameInputNotEmpty = !nameInput.getText().toString().isEmpty();
			nameSuggestionsButton.setVisibility(
					roadNameSuggestionsMap.isEmpty() || nameInputNotEmpty ? View.GONE : View.VISIBLE);
			nameSuggestionsButton.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					showNameSuggestionsMenu(v, roadNameSuggestionsMap, new OnRoadNameSuggestionSelected()
					{
						@Override public void onRoadNameSuggestionSelected(Map<String, String> selection)
						{
							data = toRoadNameList(selection);
							notifyDataSetChanged();
							updateAddLanguageButtonVisibility();
						}
					});
				}
			});
		}
	}
}
