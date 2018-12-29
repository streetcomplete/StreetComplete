package de.westnordost.streetcomplete.quests.localized_name;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.Abbreviations;
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao;


public class AddRoadNameForm extends AddLocalizedNameForm
{

	public static final String
			NO_PROPER_ROAD = "no_proper_road",
			WAY_ID = "way_id",
			WAY_GEOMETRY = "way_geometry";

	public static final int IS_SERVICE = 1, IS_LINK = 2, IS_TRACK = 3;

	@Inject AbbreviationsByLocale abbreviationsByLocale;
	@Inject RoadNameSuggestionsDao roadNameSuggestionsDao;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		View contentView = setContentView(R.layout.quest_localizedname);

		addOtherAnswers();

		initLocalizedNameAdapter(contentView, savedInstanceState);

		return view;
	}

	private void addOtherAnswers() {
		addOtherAnswer(R.string.quest_name_answer_noName, this::selectNoStreetNameReason);
		addOtherAnswer(R.string.quest_streetName_answer_cantType, this::showKeyboardInfo);
	}

	@Override
	protected AddLocalizedNameAdapter setupNameAdapter(ArrayList<LocalizedName> data, Button addLanguageButton) {
		return new AddLocalizedNameAdapter(
			data, getActivity(), getPossibleStreetsignLanguages(),
			abbreviationsByLocale, getRoadnameSuggestions(), addLanguageButton);
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

	@Override protected void onClickOk()
	{
		LinkedList<String> possibleAbbreviations = new LinkedList<>();
		for (LocalizedName localizedName : adapter.getData())
		{
			String name = localizedName.name;
			if(name.trim().isEmpty())
			{
				Toast.makeText(getActivity(), R.string.quest_generic_error_a_field_empty,
					Toast.LENGTH_LONG).show();
				return;
			}

			Abbreviations abbr = abbreviationsByLocale.get(new Locale(localizedName.languageCode));
			boolean containsAbbreviations = abbr != null && abbr.containsAbbreviations(name);

			if (name.contains(".") || containsAbbreviations)
			{
				possibleAbbreviations.add(name);
			}
		}

		confirmPossibleAbbreviationsIfAny(possibleAbbreviations, this::applyNameAnswer);
	}

	@Override
	protected void applyNameAnswer()
	{
		Bundle bundle = createAnswer();
		bundle.putLong(WAY_ID, getOsmElement().getId());
		bundle.putSerializable(WAY_GEOMETRY, getElementGeometry());
		applyAnswer(bundle);
	}

	private void selectNoStreetNameReason()
	{
		final String
				linkRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_link),
				serviceRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_service2),
				trackRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_track2),
				noName = getResources().getString(R.string.quest_streetName_answer_noName_noname),
				leaveNote = getResources().getString(R.string.quest_streetName_answer_noProperStreet_leaveNote);

		String highwayValue = getOsmElement().getTags().get("highway");
		boolean mayBeLink = highwayValue.matches("primary|secondary|tertiary");

		final List<String> answers = new ArrayList<>(5);
		if(mayBeLink) answers.add(linkRoad);
		answers.add(serviceRoad);
		answers.add(trackRoad);
		answers.add(leaveNote);
		answers.add(noName);

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
				else if(answer.equals(noName))
				{
					confirmNoStreetName();
				}
				else
				{
					Bundle data = new Bundle();
					int type = 0;
					if(answer.equals(linkRoad))		type = IS_LINK;
					if(answer.equals(serviceRoad))	type = IS_SERVICE;
					if(answer.equals(trackRoad))    type = IS_TRACK;
					data.putInt(NO_PROPER_ROAD, type);
					applyAnswer(data);
				}
			}
		};

		AlertDialog dlg = new AlertDialog.Builder(getActivity())
				.setSingleChoiceItems(answers.toArray(new String[0]), -1, onSelect)
				.setTitle(R.string.quest_streetName_answer_noName_question)
				.setPositiveButton(android.R.string.ok, onSelect)
				.setNegativeButton(android.R.string.cancel, null)
				.show();

		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
	}

	private void confirmNoStreetName()
	{
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.quest_name_answer_noName_confirmation_title)
				.setMessage(R.string.quest_streetName_answer_noName_confirmation_description)
				.setPositiveButton(R.string.quest_name_noName_confirmation_positive, (dialog, which) ->
				{
					Bundle data = new Bundle();
					data.putBoolean(NO_NAME, true);
					applyAnswer(data);
				})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.show();
	}
}
