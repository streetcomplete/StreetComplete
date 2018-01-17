package de.westnordost.streetcomplete.quests.parking_type;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddParkingTypeForm extends ImageListQuestAnswerFragment
{
	private final Item[] TYPES = new Item[] {
			new Item("surface", R.drawable.parking_type_surface, R.string.quest_parkingType_surface),
			new Item("underground", R.drawable.parking_type_underground, R.string.quest_parkingType_underground),
			new Item("multi-storey", R.drawable.parking_type_multistorey, R.string.quest_parkingType_multiStorage)
	};

	@Override protected Item[] getItems() { return TYPES; }
	@Override protected int getItemsPerRow() { return 3; }
}
