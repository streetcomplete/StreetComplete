package de.westnordost.streetcomplete.quests.recycling;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddRecyclingTypeForm extends ImageListQuestAnswerFragment
{
	private final OsmItem[] TYPES = new OsmItem[] {
			new OsmItem("overground", R.drawable.recycling_container, R.string.overground_recycling_container),
			new OsmItem("underground", R.drawable.recycling_container_underground, R.string.underground_recycling_container),
			new OsmItem("centre", R.drawable.recycling_centre, R.string.recycling_centre)
	};

	@Override protected ImageListQuestAnswerFragment.OsmItem[] getItems() { return TYPES; }
	@Override protected int getItemsPerRow() { return 3; }
}
