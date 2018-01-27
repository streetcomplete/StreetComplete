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
import de.westnordost.streetcomplete.view.Item;

public class AddReligionToPlaceOfWorshipForm extends ImageListQuestAnswerFragment
{
	private static final int INITIALLY_DISPLAYED_ITEMS = 8;

	private static final Item[] ALL_RELIGION_VALUES = new Item[]{
			// woldwide usage, values covering vast majority of used tags
			new Item("christian",      R.drawable.ic_religion_christian,      R.string.quest_religion_christian),
			new Item("muslim",      R.drawable.ic_religion_muslim,      R.string.quest_religion_muslim),
			new Item("buddhist",      R.drawable.ic_religion_buddhist,      R.string.quest_religion_buddhist),
			new Item("hindu",      R.drawable.ic_religion_hindu,      R.string.quest_religion_hindu),
			new Item("shinto",      R.drawable.ic_religion_shinto,      R.string.quest_religion_shinto),
			new Item("jewish",      R.drawable.ic_religion_jewish,      R.string.quest_religion_jewish),
			new Item("taoist",      R.drawable.ic_religion_taoist,      R.string.quest_religion_taoist),
			new Item("sikh",      R.drawable.ic_religion_sikh,      R.string.quest_religion_sikh),
			new Item("jain",      R.drawable.ic_religion_jain,      R.string.quest_religion_jain),
			new Item("bahai",      R.drawable.ic_religion_bahai,      R.string.quest_religion_bahai),
			new Item("caodaism",      R.drawable.ic_religion_caodaist,      R.string.quest_religion_caodaist),
			new Item("confucian",      R.drawable.ic_religion_confucian,      R.string.quest_religion_confucian),
			};

	private Item[] actualReligionsValues;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		actualReligionsValues = createItems();
		imageSelector.setCellLayout(R.layout.cell_icon_select_with_label_below);

		addOtherAnswer(R.string.quest_religion_for_place_of_worship_answer_multi, this::applyMultiAnswer);

		return view;
	}

	private Item[] createItems()
	{
		List<Item> religionsList = new ArrayList<>(Arrays.asList(ALL_RELIGION_VALUES));
		List<String> popularReligionsNames = getCountryInfo().getPopularReligions();

		// in reverse because the first element in the list should be first in religionsList
		for (int i = popularReligionsNames.size()-1; i >= 0; --i)
		{
			String popularReligionName = popularReligionsNames.get(i);
			for(int j = 0; j < religionsList.size(); ++j)
			{
				Item sport = religionsList.get(j);
				if(sport.value.equals(popularReligionName))
				{
					// shuffle to start of list
					religionsList.remove(j);
					religionsList.add(0,sport);
					break;
				}
			}
		}
		return religionsList.toArray(new Item[religionsList.size()]);
	}

	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return INITIALLY_DISPLAYED_ITEMS;
	}

	@Override protected Item[] getItems()
	{
		return actualReligionsValues;
	}

	private void applyMultiAnswer()
	{
		Bundle answer = new Bundle();
		ArrayList<String> strings = new ArrayList<>(1);
		strings.add("multifaith");
		answer.putStringArrayList(OSM_VALUES, strings);
		applyImmediateAnswer(answer);
	}
}