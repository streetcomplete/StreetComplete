package de.westnordost.streetcomplete.quests.sport;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.PriorityList;
import de.westnordost.streetcomplete.view.Item;


public class AddSportForm extends ImageListQuestAnswerFragment
{
	private static final int
			MAX_DISPLAYED_ITEMS = 24, INITIALLY_DISPLAYED_ITEMS = 8;

	private static final Item[] ALL_SPORTS_VALUES = new Item[]{
			// ~worldwide usages, minus country specific ones
			// 250k - 10k
			new Item("soccer",			R.drawable.ic_sport_soccer,			R.string.quest_sport_soccer),
			new Item("tennis",			R.drawable.ic_sport_tennis,			R.string.quest_sport_tennis),
			new Item("basketball",		R.drawable.ic_sport_basketball,		R.string.quest_sport_basketball),
			new Item("golf",			R.drawable.ic_sport_golf,			R.string.quest_sport_golf),
			new Item("volleyball",		R.drawable.ic_sport_volleyball,		R.string.quest_sport_volleyball),
			new Item("beachvolleyball",	R.drawable.ic_sport_beachvolleyball, R.string.quest_sport_beachvolleyball),
			new Item("skateboard",		R.drawable.ic_sport_skateboard,		R.string.quest_sport_skateboard),
			new Item("shooting",		R.drawable.ic_sport_shooting,		R.string.quest_sport_shooting),
			// 7k - 5k
			new Item("baseball",		R.drawable.ic_sport_baseball,		R.string.quest_sport_baseball),
			new Item("athletics",		R.drawable.ic_sport_athletics,		R.string.quest_sport_athletics),
			new Item("table_tennis",	R.drawable.ic_sport_table_tennis,	R.string.quest_sport_table_tennis),
			new Item("gymnastics",		R.drawable.ic_sport_gymnastics,		R.string.quest_sport_gymnastics),
			// 4k - 2k
			new Item("boules",			R.drawable.ic_sport_boules,			R.string.quest_sport_boules),
			new Item("handball",		R.drawable.ic_sport_handball,		R.string.quest_sport_handball),
			new Item("field_hockey",	R.drawable.ic_sport_field_hockey,	R.string.quest_sport_field_hockey),
			new Item("ice_hockey",		R.drawable.ic_sport_ice_hockey,		R.string.quest_sport_ice_hockey),
			new Item("american_football", R.drawable.ic_sport_american_football, R.string.quest_sport_american_football),
			new Item("equestrian",		R.drawable.ic_sport_equestrian,		R.string.quest_sport_equestrian),
			new Item("archery",			R.drawable.ic_sport_archery,		R.string.quest_sport_archery),
			new Item("roller_skating",	R.drawable.ic_sport_roller_skating,		R.string.quest_sport_roller_skating),
			// 1k - 0k
			new Item("badminton",		R.drawable.ic_sport_badminton,		R.string.quest_sport_badminton),
			new Item("cricket",			R.drawable.ic_sport_cricket,		R.string.quest_sport_cricket),
			new Item("rugby",			R.drawable.ic_sport_rugby,			R.string.quest_sport_rugby),
			new Item("bowls",			R.drawable.ic_sport_bowls,			R.string.quest_sport_bowls),
			new Item("softball",		R.drawable.ic_sport_softball,		R.string.quest_sport_softball),
			new Item("racquet",			R.drawable.ic_sport_racquet,		R.string.quest_sport_racquet),
			new Item("ice_skating",		R.drawable.ic_sport_ice_skating,	R.string.quest_sport_ice_skating),
			new Item("paddle_tennis",	R.drawable.ic_sport_paddle_tennis,	R.string.quest_sport_paddle_tennis),
			new Item("australian_football", R.drawable.ic_sport_australian_football, R.string.quest_sport_australian_football),
			new Item("canadian_football", R.drawable.ic_sport_canadian_football, R.string.quest_sport_canadian_football),
			new Item("netball",			R.drawable.ic_sport_netball,		R.string.quest_sport_netball),
			new Item("gaelic_games",	R.drawable.ic_sport_gaelic_games,	R.string.quest_sport_gaelic_games),
			new Item("sepak_takraw",	R.drawable.ic_sport_sepak_takraw,	R.string.quest_sport_sepak_takraw),
			};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		imageSelector.setCellLayout(R.layout.cell_icon_select_with_label_below);

		addOtherAnswer(R.string.quest_sport_answer_multi, this::applyMultiAnswer);

		return view;
	}

	private Item[] createItems()
	{
		List<Item> sportsList = new ArrayList<>(Arrays.asList(ALL_SPORTS_VALUES));
		List<String> popularSportsNames = getCountryInfo().getPopularSports();

		sportsList = PriorityList.buildList(sportsList, popularSportsNames);

		// only first 24 items (6 rows)
		int arraySize = Math.min(sportsList.size(), MAX_DISPLAYED_ITEMS);
		return sportsList.subList(0, arraySize).toArray(new Item[arraySize]);
	}

	@Override protected void onClickOk()
	{
		if(imageSelector.getSelectedIndices().size() > 3)
		{
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.quest_sport_manySports_confirmation_title)
					.setMessage(R.string.quest_sport_manySports_confirmation_description)
					.setPositiveButton(R.string.quest_manySports_confirmation_specific, (dialog, which) -> applyAnswer())
					.setNegativeButton(R.string.quest_manySports_confirmation_generic, (dialog, which) -> applyMultiAnswer())
					.show();
		}
		else
		{
			applyAnswer();
		}
	}

	@Override protected int getMaxSelectableItems()
	{
		return -1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return INITIALLY_DISPLAYED_ITEMS;
	}

	@Override protected Item[] getItems()
	{
		return createItems();
	}

	private void applyMultiAnswer()
	{
		Bundle answer = new Bundle();
		ArrayList<String> strings = new ArrayList<>(1);
		strings.add("multi");
		answer.putStringArrayList(OSM_VALUES, strings);
		applyAnswer(answer);
	}
}
