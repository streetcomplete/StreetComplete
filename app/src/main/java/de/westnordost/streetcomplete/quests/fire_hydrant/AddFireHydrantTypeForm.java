package de.westnordost.streetcomplete.quests.fire_hydrant;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddFireHydrantTypeForm extends ImageListQuestAnswerFragment
{
	private final OsmItem[] TYPES = new OsmItem[] {
			new OsmItem("pillar", R.drawable.fire_hydrant_pillar, R.string.quest_fireHydrant_type_pillar),
			new OsmItem("underground", R.drawable.fire_hydrant_underground, R.string.quest_fireHydrant_type_underground),
			new OsmItem("wall", R.drawable.fire_hydrant_wall, R.string.quest_fireHydrant_type_wall),
			new OsmItem("pond", R.drawable.fire_hydrant_pond, R.string.quest_fireHydrant_type_pond)
	};

	@Override protected OsmItem[] getItems() { return TYPES; }
	@Override protected int getItemsPerRow() { return 2; }
	@Override protected int getMaxNumberOfInitiallyShownItems() { return 2; }
}
