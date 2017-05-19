package de.westnordost.streetcomplete.quests.sport;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddSportForm extends ImageListQuestAnswerFragment
{
	private static final int
			MAX_DISPLAYED_ITEMS = 24, INITIALLY_DISPLAYED_ITEMS = 8;

	private static final OsmItem[] ALL_SPORTS_VALUES = new OsmItem[]{
			// ~worldwide usages, minus country specific ones
			// 250k - 10k
			new OsmItem("soccer",			R.drawable.ic_sport_soccer,			R.string.quest_sport_soccer),
			new OsmItem("tennis",			R.drawable.ic_sport_tennis,			R.string.quest_sport_tennis),
			new OsmItem("basketball",		R.drawable.ic_sport_basketball,		R.string.quest_sport_basketball),
			new OsmItem("golf",				R.drawable.ic_sport_golf,			R.string.quest_sport_golf),
			new OsmItem("volleyball",		R.drawable.ic_sport_volleyball,		R.string.quest_sport_volleyball),
			new OsmItem("beachvolleyball",	R.drawable.ic_sport_beachvolleyball, R.string.quest_sport_beachvolleyball),
			new OsmItem("skateboard",		R.drawable.ic_sport_skateboard,		R.string.quest_sport_skateboard),
			new OsmItem("shooting",			R.drawable.ic_sport_shooting,		R.string.quest_sport_shooting),
			// 7k - 5k
			new OsmItem("baseball",			R.drawable.ic_sport_baseball,		R.string.quest_sport_baseball),
			new OsmItem("athletics",		R.drawable.ic_sport_athletics,		R.string.quest_sport_athletics),
			new OsmItem("table_tennis",		R.drawable.ic_sport_table_tennis,	R.string.quest_sport_table_tennis),
			new OsmItem("gymnastics",		R.drawable.ic_sport_gymnastics,		R.string.quest_sport_gymnastics),
			// 4k - 2k
			new OsmItem("boules",			R.drawable.ic_sport_boules,			R.string.quest_sport_boules),
			new OsmItem("handball",			R.drawable.ic_sport_handball,		R.string.quest_sport_handball),
			new OsmItem("field_hockey",		R.drawable.ic_sport_field_hockey,	R.string.quest_sport_field_hockey),
			new OsmItem("ice_hockey",		R.drawable.ic_sport_ice_hockey,		R.string.quest_sport_ice_hockey),
			new OsmItem("american_football", R.drawable.ic_sport_american_football, R.string.quest_sport_american_football),
			new OsmItem("equestrian",		R.drawable.ic_sport_equestrian,		R.string.quest_sport_equestrian),
			new OsmItem("archery",			R.drawable.ic_sport_archery,		R.string.quest_sport_archery),
			new OsmItem("skating",			R.drawable.ic_sport_skating,		R.string.quest_sport_skating),
			// 1k - 0k
			new OsmItem("badminton",		R.drawable.ic_sport_badminton,		R.string.quest_sport_badminton),
			new OsmItem("cricket",			R.drawable.ic_sport_cricket,		R.string.quest_sport_cricket),
			new OsmItem("rugby",			R.drawable.ic_sport_rugby,			R.string.quest_sport_rugby),
			new OsmItem("bowls",			R.drawable.ic_sport_bowls,			R.string.quest_sport_bowls),
			new OsmItem("softball",			R.drawable.ic_sport_softball,		R.string.quest_sport_softball),
			new OsmItem("racquet",			R.drawable.ic_sport_racquet,		R.string.quest_sport_racquet),
			new OsmItem("ice_skating",		R.drawable.ic_sport_ice_skating,	R.string.quest_sport_ice_skating),
			new OsmItem("paddle_tennis",	R.drawable.ic_sport_paddle_tennis,	R.string.quest_sport_paddle_tennis),
			new OsmItem("australian_football", R.drawable.ic_sport_australian_football, R.string.quest_sport_australian_football),
			new OsmItem("canadian_football", R.drawable.ic_sport_canadian_football, R.string.quest_sport_canadian_football),
			new OsmItem("netball",			R.drawable.ic_sport_netball,		R.string.quest_sport_netball),
			new OsmItem("gaelic_games",		R.drawable.ic_sport_gaelic_games,	R.string.quest_sport_gaelic_games),
			new OsmItem("sepak_takraw",		R.drawable.ic_sport_sepak_takraw,	R.string.quest_sport_sepak_takraw),
			};

	private OsmItem[] actualSportsValues;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		actualSportsValues = createItems();
		setTitle(R.string.quest_sport_title);
		imageSelector.setCellLayout(R.layout.icon_select_cell_with_label_below);
		return view;
	}

	private OsmItem[] createItems()
	{
		List<OsmItem> sportsList = new ArrayList<>(Arrays.asList(ALL_SPORTS_VALUES));
		List<String> popularSportsNames = getCountryInfo().getPopularSports();
		if(popularSportsNames != null)
		{
			// in reverse because the first element in the list should be first in sportsList
			for (int i = popularSportsNames.size()-1; i >= 0; --i)
			{
				String popularSportName = popularSportsNames.get(i);
				for(int j = 0; j < sportsList.size(); ++j)
				{
					OsmItem sport = sportsList.get(j);
					if(sport.osmValue.equals(popularSportName))
					{
						// shuffle to start of list
						sportsList.remove(j);
						sportsList.add(0,sport);
						break;
					}
				}
			}
		}
		// only first 24 items (6 rows)
		return sportsList.subList(0,MAX_DISPLAYED_ITEMS).toArray(new OsmItem[MAX_DISPLAYED_ITEMS]);
	}

	@Override protected void onClickOk()
	{
		if(imageSelector.getSelectedIndices().size() > 3)
		{
			DialogInterface.OnClickListener onSpecific = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					applyAnswer();
				}
			};
			DialogInterface.OnClickListener onGeneric = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					applyMultiAnswer();
				}
			};

			new AlertDialogBuilder(getActivity())
					.setTitle(R.string.quest_sport_manySports_confirmation_title)
					.setMessage(R.string.quest_sport_manySports_confirmation_description)
					.setPositiveButton(R.string.quest_manySports_confirmation_specific, onSpecific)
					.setNegativeButton(R.string.quest_manySports_confirmation_generic, onGeneric)
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

	@Override protected OsmItem[] getItems()
	{
		return actualSportsValues;
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_sport_answer_multi);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(super.onClickOtherAnswer(itemResourceId)) return true;

		if(itemResourceId == R.string.quest_sport_answer_multi)
		{
			applyMultiAnswer();
			return true;
		}

		return false;
	}

	private void applyMultiAnswer()
	{
		Bundle answer = new Bundle();
		ArrayList<String> strings = new ArrayList<>(1);
		strings.add("multi");
		answer.putStringArrayList(OSM_VALUES, strings);
		applyImmediateAnswer(answer);
	}
}