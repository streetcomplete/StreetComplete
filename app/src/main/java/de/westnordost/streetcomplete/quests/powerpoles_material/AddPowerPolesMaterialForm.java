package de.westnordost.streetcomplete.quests.powerpoles_material;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddPowerPolesMaterialForm extends ImageListQuestAnswerFragment
{
	private final OsmItem[] TYPES = new OsmItem[] {
			new OsmItem("wood", R.drawable.power_pole_wood, R.string.quest_powerPolesMaterial_wood),
			new OsmItem("steel", R.drawable.power_pole_steel, R.string.quest_powerPolesMaterial_metal),
			new OsmItem("concrete", R.drawable.power_pole_concrete, R.string.quest_powerPolesMaterial_concrete)
	};

	@Override protected OsmItem[] getItems() { return TYPES; }
	@Override protected int getItemsPerRow() { return 3; }
}
