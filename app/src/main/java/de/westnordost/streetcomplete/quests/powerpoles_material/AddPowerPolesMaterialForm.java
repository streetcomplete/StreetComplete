package de.westnordost.streetcomplete.quests.powerpoles_material;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddPowerPolesMaterialForm extends ImageListQuestAnswerFragment
{
	private final OsmItem[] TYPES = new OsmItem[] {
			new OsmItem("wood", R.drawable.power_pole_wood, R.string.quest_powerPolesMaterial_wood),
			new OsmItem("steel", R.drawable.power_pole_steel, R.string.quest_powerPolesMaterial_metal),
			new OsmItem("concrete", R.drawable.power_pole_concrete, R.string.quest_powerPolesMaterial_concrete)
	};

	@Override protected OsmItem[] getItems()
	{
		return TYPES;
	}

	@Override protected int getItemsPerRow()
	{
		return 3;
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_powerPolesMaterial_title);
		return view;
	}
}
