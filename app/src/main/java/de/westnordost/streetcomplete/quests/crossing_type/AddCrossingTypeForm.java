package de.westnordost.streetcomplete.quests.crossing_type;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddCrossingTypeForm extends ImageListQuestAnswerFragment
{
    private final Item[] TYPES = new Item[] {
            new Item("traffic_signals", R.drawable.crossing_type_signals, R.string.quest_crossing_type_signals),
            new Item("uncontrolled", R.drawable.crossing_type_zebra, R.string.quest_crossing_type_uncontrolled),
            new Item("unmarked", R.drawable.crossing_type_unmarked, R.string.quest_crossing_type_unmarked)
    };

    @Override protected Item[] getItems() { return TYPES; }
    @Override protected int getItemsPerRow() { return 3; }
}
