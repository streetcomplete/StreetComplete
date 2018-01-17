package de.westnordost.streetcomplete.quests.powerpoles_material;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddPowerPolesMaterialForm extends ImageListQuestAnswerFragment
{
	private final Item[] TYPES = new Item[] {
			new Item("wood", R.drawable.power_pole_wood, R.string.quest_powerPolesMaterial_wood),
			new Item("steel", R.drawable.power_pole_steel, R.string.quest_powerPolesMaterial_metal),
			new Item("concrete", R.drawable.power_pole_concrete, R.string.quest_powerPolesMaterial_concrete)
	};

	@Override protected Item[] getItems() { return TYPES; }
	@Override protected int getItemsPerRow() { return 3; }
}
