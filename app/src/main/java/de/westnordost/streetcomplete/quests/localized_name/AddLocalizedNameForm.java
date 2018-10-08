package de.westnordost.streetcomplete.quests.localized_name;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher;
import de.westnordost.streetcomplete.util.Serializer;


public abstract class AddLocalizedNameForm extends AbstractQuestFormAnswerFragment
{
	protected static final String LOCALIZED_NAMES_DATA = "localized_names_data";

	public static final String NO_NAME = "no_name";
	static final String
		NAMES = "names",
		LANGUAGE_CODES = "language_codes";

	@Inject Serializer serializer;

	protected AddLocalizedNameAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		return view;
	}

	protected void initLocalizedNameAdapter(View contentView, Bundle savedInstanceState)
	{
		ArrayList<LocalizedName> data;
		if(savedInstanceState != null)
		{
			data = serializer.toObject(savedInstanceState.getByteArray(LOCALIZED_NAMES_DATA),ArrayList.class);
		}
		else
		{
			data = new ArrayList<>();
		}

		Button addLanguageButton = contentView.findViewById(R.id.btn_add);

		adapter = setupNameAdapter(data, addLanguageButton);
		adapter.addOnNameChangedListener(name -> checkIsFormComplete());
		adapter.registerAdapterDataObserver(new AdapterDataChangedWatcher(this::checkIsFormComplete));
		RecyclerView recyclerView = contentView.findViewById(R.id.roadnames);
		recyclerView.setLayoutManager(
				new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		recyclerView.setAdapter(adapter);
		recyclerView.setNestedScrollingEnabled(false);
		checkIsFormComplete();
	}

	protected AddLocalizedNameAdapter setupNameAdapter(ArrayList<LocalizedName> data, Button addLanguageButton) {
		return new AddLocalizedNameAdapter(
			data, getActivity(), getPossibleStreetsignLanguages(),
			null, null, addLanguageButton);
	}

	protected List<String> getPossibleStreetsignLanguages()
	{
		List<String> possibleStreetsignLanguages = new ArrayList<>();
		possibleStreetsignLanguages.addAll(getCountryInfo().getOfficialLanguages());
		possibleStreetsignLanguages.addAll(getCountryInfo().getAdditionalStreetsignLanguages());
		// removes duplicates
		return new ArrayList<>(new LinkedHashSet<>(possibleStreetsignLanguages));
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putByteArray(LOCALIZED_NAMES_DATA, serializer.toBytes(adapter.getData()));
	}

	protected void applyNameAnswer()
	{
		applyAnswer(createAnswer());
	}

	protected Bundle createAnswer() {
		Bundle bundle = new Bundle();
		ArrayList<LocalizedName> data = adapter.getData();

		String[] names = new String[data.size()];
		String[] languageCodes = new String[data.size()];
		for (int i = 0; i<data.size(); ++i)
		{
			names[i] = data.get(i).name;
			languageCodes[i] = data.get(i).languageCode;
		}

		bundle.putStringArray(NAMES, names);
		bundle.putStringArray(LANGUAGE_CODES, languageCodes);

		return bundle;
	}

	protected void confirmPossibleAbbreviationsIfAny(final Queue<String> names, final Runnable onConfirmedAll)
	{
		if(names.isEmpty())
		{
			onConfirmedAll.run();
		}
		else
		{
			/* recursively call self on confirm until the list of not-abbreviations to confirm is
			   through */
			String name = names.remove();
			confirmPossibleAbbreviation(name,
					() -> confirmPossibleAbbreviationsIfAny(names, onConfirmedAll));
		}
	}

	protected void confirmPossibleAbbreviation(String name, final Runnable onConfirmed)
	{
		Spanned title = Html.fromHtml(getResources().getString(
			R.string.quest_streetName_nameWithAbbreviations_confirmation_title_name,
			"<i>"+ Html.escapeHtml(name)+"</i>"));

		new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
				.setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive, (dialog, which) -> onConfirmed.run())
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.show();
	}

	protected void showKeyboardInfo()
	{
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.quest_streetName_cantType_title)
				.setMessage(R.string.quest_streetName_cantType_description)
				.setPositiveButton(R.string.quest_streetName_cantType_open_settings,
						(dialog, which) -> startActivity(new Intent(Settings.ACTION_SETTINGS)))
				.setNeutralButton(R.string.quest_streetName_cantType_open_store, (dialog, which) ->
				{
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_APP_MARKET);
					startActivity(intent);
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	protected static HashMap<String,String> toNameByLanguage(Bundle answer)
	{
		String[] names = answer.getStringArray(NAMES);
		String[] languages = answer.getStringArray(LANGUAGE_CODES);

		HashMap<String,String> result = new HashMap<>();
		result.put("", names[0]);
		// add languages only if there is more than one name specified. If there is more than one
		// name, the "main" name (name specified in top row) is also added with the language.
		if(names.length > 1)
		{
			for (int i = 0; i < names.length; i++)
			{
				// (the first) element may have no specific language
				if(!languages[i].isEmpty())
				{
					result.put(languages[i], names[i]);
				}
			}
		}
		return result;
	}

	@Override public boolean isFormComplete()
	{
		// all added name rows are not empty
		for (LocalizedName localizedName : adapter.getData())
		{
			if(localizedName.name.trim().isEmpty()) return false;
		}
		return true;
	}
}
