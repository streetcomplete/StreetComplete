package de.westnordost.streetcomplete.quests.localized_name;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
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

public class AddLocalizedNameAdapter extends RecyclerView.Adapter
{
	private ArrayList<LocalizedName> data;

	private final Context context;
	private final List<String> languages;
	private final AbbreviationsByLocale abbreviationsByLocale;
	private final List<Map<String, String>> localizedNameSuggestions;
	private final Button addLanguageButton;

	public interface OnNameChangedListener
	{
		void onNameChanged(LocalizedName name);
	}
	private final List<OnNameChangedListener> listeners = new ArrayList<>();

	public AddLocalizedNameAdapter(ArrayList<LocalizedName> data, Context context, List<String> languages,
								   @Nullable AbbreviationsByLocale abbreviationsByLocale,
								   @Nullable List<Map<String, String>> localizedNameSuggestions, Button addLanguageButton)
	{
		if(data.isEmpty())
		{
			data.add(new LocalizedName(languages.get(0), ""));
		}
		this.data = data;
		this.context = context;
		this.languages = languages;
		this.abbreviationsByLocale = abbreviationsByLocale;
		this.localizedNameSuggestions = localizedNameSuggestions;
		putDefaultLocalizedNameSuggestion();
		this.addLanguageButton = addLanguageButton;
		this.addLanguageButton.setOnClickListener(v -> {
			showLanguageSelectMenu(v, getNotAddedLanguages(), this::add);
		});

		updateAddLanguageButtonVisibility();
	}

	public void addOnNameChangedListener(OnNameChangedListener listener)
	{
		listeners.add(listener);
	}

	/* Names are usually specified without language information (name=My Street). To provide
	 * meaningful name suggestions per language, it must then be determined in which language this
	 * name tag is. */
	private void putDefaultLocalizedNameSuggestion()
	{
		String defaultLanguage = languages.get(0);
		if(localizedNameSuggestions != null) {
			for (Map<String, String> names : localizedNameSuggestions) {
				if (names.containsKey("")) {
					// name=A -> name=A, name:de=A (in Germany)
					if (!names.containsKey(defaultLanguage)) {
						String defaultName = names.get("");
						names.put(defaultLanguage, defaultName);
					}
				}
			}
		}
	}

	private static ArrayList<LocalizedName> toLocalizedNameList(Map<String, String> nameByLanguageMap)
	{
		ArrayList<LocalizedName> result = new ArrayList<>();
		String defaultName = nameByLanguageMap.get("");
		for (Map.Entry<String, String> entry : nameByLanguageMap.entrySet())
		{
			// put default name first
			// (i.e. name=A, name:en=B, name:de=A -> name:de goes first and name is not shown)
			LocalizedName localizedName = new LocalizedName(entry.getKey(), entry.getValue());
			if(localizedName.name.equals(defaultName))
			{
				if(!localizedName.languageCode.isEmpty()) result.add(0, localizedName);
			}
			else
			{
				result.add(localizedName);
			}
		}
		// this is for the case: name=A, name:de=B, name:en=C -> name goes first
		if(!result.get(0).name.equals(defaultName))
		{
			result.add(0, new LocalizedName("",defaultName));
		}
		return result;
	}

	@Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new ViewHolder(inflater.inflate(R.layout.quest_localizedname_row, parent, false));
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
		data.add(new LocalizedName(languageCode, ""));
		notifyItemInserted(insertIndex);

		updateAddLanguageButtonVisibility();
	}

	private List<String> getNotAddedLanguages()
	{
		List<String> result = new ArrayList<>(languages);
		for (LocalizedName localizedName : data)
		{
			result.remove(localizedName.languageCode);
		}
		return result;
	}

	public ArrayList<LocalizedName> getData()
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

		m.setOnMenuItemClickListener(item ->
		{
			callback.onLanguageSelected(languageList.get(item.getItemId()));
			return true;
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

	private interface OnLocalizedNameSuggestionSelected
	{
		void onLocalizedNameSuggestionSelected(Map<String, String> selection);
	}

	private void showNameSuggestionsMenu(View v,
										 final Map<String, Map<String, String>> localizedNameSuggestionsMap,
										 final OnLocalizedNameSuggestionSelected callback)
	{
		PopupMenu m = new PopupMenu(context, v);

		int i = 0;
		for (Map.Entry<String, Map<String, String>> entry : localizedNameSuggestionsMap.entrySet())
		{
			m.getMenu().add(NONE,i++,NONE, entry.getKey());
		}

		m.setOnMenuItemClickListener(item ->
		{
			Map<String, String> selected = localizedNameSuggestionsMap.get(item.getTitle().toString());
			callback.onLocalizedNameSuggestionSelected(selected);
			return true;
		});
		m.show();
	}

	private Map<String,Map<String,String>> getLocalizedNameSuggestionsByLanguageCode(String languageCode)
	{
		final Map<String,Map<String,String>> localizedNameSuggestionsMap = new HashMap<>();
		if(localizedNameSuggestions != null) {
			for (Map<String, String> localizedNameSuggestion : localizedNameSuggestions) {
				String name = localizedNameSuggestion.get(languageCode);
				if (name == null) continue;

				// "unspecified language" suggestions
				if (languageCode.isEmpty()) {
					int defaultNameOccurances = 0;
					for (String other : localizedNameSuggestion.values()) {
						if (name.equals(other)) defaultNameOccurances++;
					}
					// name=A, name:de=A -> do not consider "A" for "unspecified language" suggestion
					if (defaultNameOccurances >= 2) continue;
					// only for name=A, name:de=B, name:en=C,...
				}
				localizedNameSuggestionsMap.put(name, localizedNameSuggestion);
			}
		}
		return localizedNameSuggestionsMap;
	}

	private class ViewHolder extends RecyclerView.ViewHolder
	{
		private final AutoCorrectAbbreviationsEditText nameInput;
		private final TextView languageButton;
		private final TextView deleteButton;
		private final View nameSuggestionsButton;

		private LocalizedName localizedName;

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
					localizedName.name = name;
					if(name.isEmpty())
					{
						boolean hasSuggestions = !getLocalizedNameSuggestionsByLanguageCode(localizedName.languageCode).isEmpty();
						nameSuggestionsButton.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
					}
					else
					{
						nameSuggestionsButton.setVisibility(View.GONE);
					}
					for (OnNameChangedListener listener : listeners)
					{
						listener.onNameChanged(localizedName);
					}
				}
			});

			deleteButton.setOnClickListener(v ->
			{
				// clearing focus is very necessary, otherwise crash
				nameInput.clearFocus();
				remove(getAdapterPosition());
			});
		}

		public void update(final int index, LocalizedName rn)
		{
			this.localizedName = rn;

			final boolean isFirst = index == 0;

			deleteButton.setVisibility(isFirst ? View.INVISIBLE : View.VISIBLE);
			languageButton.setVisibility(languages.size() > 1 ? View.VISIBLE : View.INVISIBLE);

			nameInput.setText(localizedName.name);
			nameInput.requestFocus();
			languageButton.setText(localizedName.languageCode);

			// first entry is bold (the first entry is supposed to be the "default language", I
			// hope that comes across to the users like this. Otherwise, a text hint is necessary)
			languageButton.setTypeface(null, isFirst ? Typeface.BOLD : Typeface.NORMAL);
			nameInput.setTypeface(null, isFirst ? Typeface.BOLD : Typeface.NORMAL);

			languageButton.setOnClickListener((View v) ->
			{
				List<String> notAddedLanguages = getNotAddedLanguages();
				// in first entry user may select "unspecified language" to cover cases where
				// the default name is no specific language. I.e. see
				// https://wiki.openstreetmap.org/wiki/Multilingual_names#Sardegna_.28Sardinia.29
				if(isFirst)
				{
					notAddedLanguages.add(0,"");
				}

				showLanguageSelectMenu(v, notAddedLanguages, languageCode ->
				{
					localizedName.languageCode = languageCode;
					languageButton.setText(languageCode);
					updateAddLanguageButtonVisibility();
					updateNameSuggestions();
				});
			});

			updateNameSuggestions();

			// load abbreviations from file in separate thread
			new AsyncTask<Void, Void, Abbreviations>()
			{
				@Override protected Abbreviations doInBackground(Void... params)
				{
					return abbreviationsByLocale != null ? abbreviationsByLocale.get(new Locale(localizedName.languageCode)) : null;
				}

				@Override protected void onPostExecute(Abbreviations abbreviations)
				{
					nameInput.setAbbreviations(abbreviations);
				}
			}.execute();
		}

		private void updateNameSuggestions()
		{
			final Map<String, Map<String, String>> localizedNameSuggestionsMap =
					getLocalizedNameSuggestionsByLanguageCode(localizedName.languageCode);

			boolean nameInputNotEmpty = !nameInput.getText().toString().isEmpty();
			nameSuggestionsButton.setVisibility(
					localizedNameSuggestionsMap.isEmpty() || nameInputNotEmpty ? View.GONE : View.VISIBLE);
			nameSuggestionsButton.setOnClickListener(v ->
			{
				showNameSuggestionsMenu(v, localizedNameSuggestionsMap, selection ->
				{
					data = toLocalizedNameList(selection);
					notifyDataSetChanged();
					updateAddLanguageButtonVisibility();
				});
			});
		}
	}
}
