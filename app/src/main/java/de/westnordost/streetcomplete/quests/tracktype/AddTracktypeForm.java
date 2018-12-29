package de.westnordost.streetcomplete.quests.tracktype;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddTracktypeForm extends ImageListQuestAnswerFragment
{
    private final Item[] TYPES = new Item[] {
            new Item("grade1", R.drawable.tracktype_grade1, R.string.quest_tracktype_grade1),
            new Item("grade2", R.drawable.tracktype_grade2, R.string.quest_tracktype_grade2),
            new Item("grade3", R.drawable.tracktype_grade3, R.string.quest_tracktype_grade3),
            new Item("grade4", R.drawable.tracktype_grade4, R.string.quest_tracktype_grade4),
            new Item("grade5", R.drawable.tracktype_grade5, R.string.quest_tracktype_grade5),
    };

    @Override protected Item[] getItems() { return TYPES; }

    @Override protected int getItemsPerRow() { return 3; }
}
