package de.westnordost.streetcomplete.quests.recycling;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddRecyclingTypeForm extends ImageListQuestAnswerFragment
{
	private final Item[] TYPES = new Item[] {
			new Item("overground", R.drawable.recycling_container, R.string.overground_recycling_container),
			new Item("underground", R.drawable.recycling_container_underground, R.string.underground_recycling_container),
			new Item("centre", R.drawable.recycling_centre, R.string.recycling_centre)
	};

	@Override protected Item[] getItems() { return TYPES; }
	@Override protected int getItemsPerRow() { return 3; }
}
