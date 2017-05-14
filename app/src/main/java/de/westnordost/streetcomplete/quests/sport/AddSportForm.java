package de.westnordost.streetcomplete.quests.sport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddSportForm extends ImageListQuestAnswerFragment
{
	private static final ListValue[] SPORTS_VALUES = new ListValue[]{
            new ListValue("soccer", R.drawable.ic_sport_soccer, R.string.quest_sport_soccer),
            new ListValue("tennis", R.drawable.ic_sport_tennis, R.string.quest_sport_tennis),
            new ListValue("baseball", R.drawable.ic_sport_baseball, R.string.quest_sport_baseball),
            new ListValue("basketball", R.drawable.ic_sport_basketball, R.string.quest_sport_basketball),
            new ListValue("golf", R.drawable.ic_sport_golf, R.string.quest_sport_golf),
            new ListValue("equestrian", R.drawable.ic_sport_equestrian, R.string.quest_sport_equestrian),
            new ListValue("athletics", R.drawable.ic_sport_athletics, R.string.quest_sport_athletics),
            new ListValue("volleyball", R.drawable.ic_sport_volleyball, R.string.quest_sport_volleyball),
            new ListValue("beachvolleyball", R.drawable.ic_sport_beachvolleyball, R.string.quest_sport_beachvolleyball),
            new ListValue("american_football", R.drawable.ic_sport_american_football, R.string.quest_sport_american_football),
            new ListValue("skateboard", R.drawable.ic_sport_skateboard, R.string.quest_sport_skateboard),
            new ListValue("bowls", R.drawable.ic_sport_bowls, R.string.quest_sport_bowls),
            new ListValue("boules", R.drawable.ic_sport_boules, R.string.quest_sport_boules),
            new ListValue("shooting", R.drawable.ic_sport_shooting, R.string.quest_sport_shooting),
            new ListValue("cricket", R.drawable.ic_sport_cricket, R.string.quest_sport_cricket),
            new ListValue("table_tennis", R.drawable.ic_sport_table_tennis, R.string.quest_sport_table_tennis),
            new ListValue("gymnastics", R.drawable.ic_sport_gymnastics, R.string.quest_sport_gymnastics),

			new ListValue("archery", R.drawable.ic_sport_archery, R.string.quest_sport_archery),
			new ListValue("australian_football", R.drawable.ic_sport_australian_football, R.string.quest_sport_australian_football),
			new ListValue("badminton", R.drawable.ic_sport_badminton, R.string.quest_sport_badminton),
			new ListValue("canadian_football", R.drawable.ic_sport_canadian_football, R.string.quest_sport_canadian_football),
			new ListValue("field_hockey", R.drawable.ic_sport_field_hockey, R.string.quest_sport_field_hockey),
			new ListValue("handball", R.drawable.ic_sport_handball, R.string.quest_sport_handball),
			new ListValue("ice_hockey", R.drawable.ic_sport_ice_hockey, R.string.quest_sport_ice_hockey),
			new ListValue("netball", R.drawable.ic_sport_netball, R.string.quest_sport_netball),
			new ListValue("rugby", R.drawable.ic_sport_rugby, R.string.quest_sport_rugby),
			};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_sport_title);
		return view;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return 8;
	}

	@Override protected ListValue[] getItems()
	{
		return SPORTS_VALUES;
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
			Bundle answer = new Bundle();
			answer.putString(OSM_VALUE, "multi");
			applyImmediateAnswer(answer);
			return true;
		}

		return false;
	}
}