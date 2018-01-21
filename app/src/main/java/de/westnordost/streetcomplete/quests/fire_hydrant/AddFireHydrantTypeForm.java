package de.westnordost.streetcomplete.quests.fire_hydrant;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddFireHydrantTypeForm extends ImageListQuestAnswerFragment
{
	private final Item[] TYPES = new Item[] {
			new Item("pillar", R.drawable.fire_hydrant_pillar, R.string.quest_fireHydrant_type_pillar),
			new Item("underground", R.drawable.fire_hydrant_underground, R.string.quest_fireHydrant_type_underground),
			new Item("wall", R.drawable.fire_hydrant_wall, R.string.quest_fireHydrant_type_wall),
			new Item("pond", R.drawable.fire_hydrant_pond, R.string.quest_fireHydrant_type_pond)
	};

	@Override protected Item[] getItems() { return TYPES; }
	@Override protected int getItemsPerRow() { return 2; }
	@Override protected int getMaxNumberOfInitiallyShownItems() { return 2; }
}
