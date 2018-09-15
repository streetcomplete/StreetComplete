package de.westnordost.streetcomplete.quests.religion;

import android.os.Bundle;
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

public class AddReligionToPlaceOfWorshipForm extends ImageListQuestAnswerFragment
{
	private static final Item[] ALL_RELIGION_VALUES = new Item[]{
			// sorted by worldwide usages, *minus* country specific ones
			new Item("christian", R.drawable.ic_religion_christian, R.string.quest_religion_christian),
			new Item("muslim",    R.drawable.ic_religion_muslim,    R.string.quest_religion_muslim),
			new Item("buddhist",  R.drawable.ic_religion_buddhist,  R.string.quest_religion_buddhist),
			new Item("hindu",     R.drawable.ic_religion_hindu,     R.string.quest_religion_hindu),

			new Item("jewish",    R.drawable.ic_religion_jewish,    R.string.quest_religion_jewish),
			// difficult to get the numbers on this, as they are counted alternating as buddhists,
			// taoists, confucianists, not religious or "folk religion" in statistics. See
			// https://en.wikipedia.org/wiki/Chinese_folk_religion
			// sorting relatively far up because there are many Chinese expats around the world
			new Item("chinese_folk", R.drawable.ic_religion_chinese_folk, R.string.quest_religion_chinese_folk),
			new Item("bahai",     R.drawable.ic_religion_bahai,     R.string.quest_religion_bahai),
			new Item("sikh",      R.drawable.ic_religion_sikh,      R.string.quest_religion_sikh),

			new Item("taoist",    R.drawable.ic_religion_taoist,    R.string.quest_religion_taoist),
			new Item("jain",      R.drawable.ic_religion_jain,      R.string.quest_religion_jain), // India
			new Item("shinto",    R.drawable.ic_religion_shinto,    R.string.quest_religion_shinto), // Japan
			new Item("caodaism",  R.drawable.ic_religion_caodaist,  R.string.quest_religion_caodaist), // Vietnam
		};


	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		imageSelector.setCellLayout(R.layout.cell_icon_select_with_label_below);

		addOtherAnswer(R.string.quest_religion_for_place_of_worship_answer_multi, this::applyMultiAnswer);

		return view;
	}

	private Item[] createItems()
	{
		List<Item> religionsList = new ArrayList<>(Arrays.asList(ALL_RELIGION_VALUES));
		List<String> popularReligionsNames = getCountryInfo().getPopularReligions();

		religionsList = PriorityList.buildList(religionsList, popularReligionsNames);

		return religionsList.toArray(new Item[religionsList.size()]);
	}

	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return 4;
	}

	@Override protected Item[] getItems()
	{
		return  createItems();
	}

	private void applyMultiAnswer()
	{
		Bundle answer = new Bundle();
		ArrayList<String> strings = new ArrayList<>(1);
		strings.add("multifaith");
		answer.putStringArrayList(OSM_VALUES, strings);
		applyAnswer(answer);
	}
}
