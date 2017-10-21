package de.westnordost.streetcomplete.quests.road_name;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.Abbreviations;
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao;
import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddRoadNameForm extends AbstractQuestFormAnswerFragment
{
	private static final String	ROAD_NAMES_DATA = "road_names_data";

	public static final String
			NO_NAME = "no_name",
			NO_PROPER_ROAD = "no_proper_road",
			NAMES = "names",
			LANGUAGE_CODES = "language_codes",
			WAY_ID = "way_id",
			WAY_GEOMETRY = "way_geometry";

	public static final int IS_SERVICE = 1, IS_LINK = 2, IS_TRACK = 3;

	@Inject AbbreviationsByLocale abbreviationsByLocale;
	@Inject RoadNameSuggestionsDao roadNameSuggestionsDao;
	@Inject Serializer serializer;

	private AddRoadNameAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		View contentView = setContentView(R.layout.quest_roadname);

		addOtherAnswers();

		initRoadNameAdapter(contentView, savedInstanceState);

		return view;
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_name_answer_noName, new Runnable()
		{
			@Override public void run()
			{
				confirmNoStreetName();
			}
		});
		addOtherAnswer(R.string.quest_streetName_answer_noProperStreet, new Runnable()
		{
			@Override public void run()
			{
				selectNoProperStreetWhatThen();
			}
		});
		addOtherAnswer(R.string.quest_streetName_answer_cantType, new Runnable()
		{
			@Override public void run()
			{
				showKeyboardInfo();
			}
		});
	}

	private void initRoadNameAdapter(View contentView, Bundle savedInstanceState)
	{
		ArrayList<RoadName> data;
		if(savedInstanceState != null)
		{
			data = serializer.toObject(savedInstanceState.getByteArray(ROAD_NAMES_DATA),ArrayList.class);
		}
		else
		{
			data = new ArrayList<>();
		}

		Button addLanguageButton = contentView.findViewById(R.id.btn_add);

		adapter = new AddRoadNameAdapter(
				data, getActivity(), getPossibleStreetsignLanguages(),
				abbreviationsByLocale, getRoadnameSuggestions(), addLanguageButton);
		RecyclerView recyclerView = contentView.findViewById(R.id.roadnames);
		recyclerView.setLayoutManager(
				new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		recyclerView.setAdapter(adapter);
		recyclerView.setNestedScrollingEnabled(false);
	}

	private List<Map<String, String>> getRoadnameSuggestions()
	{
		ElementGeometry geometry = getElementGeometry();
		if(geometry == null || geometry.polylines == null || geometry.polylines.isEmpty())
		{
			return new ArrayList<>();
		}
		List<LatLon> points = geometry.polylines.get(0);
		List<LatLon> onlyFirstAndLast = Arrays.asList(points.get(0), points.get(points.size()-1));

		return roadNameSuggestionsDao.getNames(onlyFirstAndLast, AddRoadName.MAX_DIST_FOR_ROAD_NAME_SUGGESTION);
	}

	private List<String> getPossibleStreetsignLanguages()
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
		outState.putByteArray(ROAD_NAMES_DATA, serializer.toBytes(adapter.getData()));
	}

	@Override protected void onClickOk()
	{
		LinkedList<String> possibleAbbreviations = new LinkedList<>();
		for (RoadName roadName : adapter.getData())
		{
			String name = roadName.name;
			if(name.trim().isEmpty())
			{
				Toast.makeText(getActivity(), R.string.quest_generic_error_a_field_empty,
						Toast.LENGTH_LONG).show();
				return;
			}

			Abbreviations abbr = abbreviationsByLocale.get(new Locale(roadName.languageCode));
			boolean containsAbbreviations = abbr != null && abbr.containsAbbreviations(name);

			if (name.contains(".") || containsAbbreviations)
			{
				possibleAbbreviations.add(name);
			}
		}

		confirmPossibleAbbreviationsIfAny(possibleAbbreviations, new Runnable()
		{
			@Override public void run()
			{
				applyNameAnswer();
			}
		});
	}

	private void applyNameAnswer()
	{
		Bundle bundle = new Bundle();
		ArrayList<RoadName> data = adapter.getData();

		String[] names = new String[data.size()];
		String[] languageCodes = new String[data.size()];
		for (int i = 0; i<data.size(); ++i)
		{
			names[i] = data.get(i).name;
			languageCodes[i] = data.get(i).languageCode;
		}

		bundle.putStringArray(NAMES, names);
		bundle.putStringArray(LANGUAGE_CODES, languageCodes);
		bundle.putLong(WAY_ID, getOsmElement().getId());
		bundle.putSerializable(WAY_GEOMETRY, getElementGeometry());
		applyFormAnswer(bundle);
	}

	private void confirmPossibleAbbreviationsIfAny(final Queue<String> names, final Runnable onConfirmedAll)
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
			confirmPossibleAbbreviation(name, new Runnable() { @Override public void run()
			{
				confirmPossibleAbbreviationsIfAny(names, onConfirmedAll);
			}
			});
		}
	}

	private void confirmPossibleAbbreviation(String name, final Runnable onConfirmed)
	{
		String title = String.format(
				getResources().getString(R.string.quest_streetName_nameWithAbbreviations_confirmation_title_name),
				name);

		new AlertDialogBuilder(getActivity())
				.setTitle(title)
				.setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
				.setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						onConfirmed.run();
					}
				})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.show();
	}

	private void showKeyboardInfo()
	{
		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_streetName_cantType_title)
				.setMessage(R.string.quest_streetName_cantType_description)
				.setPositiveButton(R.string.quest_streetName_cantType_open_settings, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						startActivity(new Intent(Settings.ACTION_SETTINGS));
					}
				})
				.setNeutralButton(R.string.quest_streetName_cantType_open_store, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_APP_MARKET);
						startActivity(intent);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private void confirmNoStreetName()
	{
		DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Bundle data = new Bundle();
				data.putBoolean(NO_NAME, true);
				applyImmediateAnswer(data);
			}
		};
		DialogInterface.OnClickListener onNo = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// nothing, just go back
			}
		};

		new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_name_answer_noName_confirmation_title)
				.setMessage(R.string.quest_streetName_answer_noName_confirmation_description)
				.setPositiveButton(R.string.quest_name_noName_confirmation_positive, onYes)
				.setNegativeButton(R.string.quest_generic_confirmation_no, onNo)
				.show();
	}

	private void selectNoProperStreetWhatThen()
	{
		final String
				linkRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_link),
				serviceRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_service),
				trackRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_track),
				leaveNote = getResources().getString(R.string.quest_streetName_answer_noProperStreet_leaveNote);

		String highwayValue = getOsmElement().getTags().get("highway");
		boolean mayBeLink = highwayValue.matches("primary|secondary|tertiary");

		final List<String> answers = new ArrayList<>(3);
		if(mayBeLink) answers.add(linkRoad);
		answers.add(serviceRoad);
		answers.add(trackRoad);
		answers.add(leaveNote);

		DialogInterface.OnClickListener onSelect = new DialogInterface.OnClickListener()
		{
			Integer selection = null;

			@Override public void onClick(DialogInterface dialog, int which)
			{
				if (which >= 0)
				{
					selection = which;
					((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
				}
				else if (which == DialogInterface.BUTTON_POSITIVE)
				{
					if(selection == null || selection < 0 || selection >= answers.size()) return;
					onAnswer();
				}
			}

			private void onAnswer()
			{
				String answer = answers.get(selection);
				if(answer.equals(leaveNote))
				{
					onClickCantSay();
				}
				else
				{
					Bundle data = new Bundle();
					int type = 0;
					if(answer.equals(linkRoad))		type = IS_LINK;
					if(answer.equals(serviceRoad))	type = IS_SERVICE;
					if(answer.equals(trackRoad))    type = IS_TRACK;
					data.putInt(NO_PROPER_ROAD, type);
					applyImmediateAnswer(data);
				}
			}
		};

		AlertDialog dlg = new AlertDialogBuilder(getActivity())
				.setSingleChoiceItems(answers.toArray(new String[0]), -1, onSelect)
				.setTitle(R.string.quest_streetName_answer_noProperStreet_question)
				.setPositiveButton(android.R.string.ok, onSelect)
				.setNegativeButton(android.R.string.cancel, null)
				.show();

		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
	}

	@Override public boolean hasChanges()
	{
		// either the user added a language or typed something for the street name
		return adapter.getData().size() > 1 || !adapter.getData().get(0).name.trim().isEmpty();
	}
}
