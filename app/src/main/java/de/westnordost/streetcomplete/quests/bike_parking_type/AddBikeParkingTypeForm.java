package de.westnordost.streetcomplete.quests.bike_parking_type;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;


public class AddBikeParkingTypeForm extends ImageListQuestAnswerFragment {
	@Override
	protected Item[] getItems() {
		return new Item[]{
			new Item("stands", R.drawable.bicycle_parking_type_stand, R.string.quest_bicycle_parking_type_stand),
			new Item("wall_loops", R.drawable.bicycle_parking_type_wheelbenders, R.string.quest_bicycle_parking_type_wheelbender),
			new Item("shed", R.drawable.bicycle_parking_type_shed, R.string.quest_bicycle_parking_type_shed),
			new Item("lockers", R.drawable.bicycle_parking_type_lockers, R.string.quest_bicycle_parking_type_locker),
			new Item("building", R.drawable.bicycle_parking_type_building, R.string.quest_bicycle_parking_type_building),
		};
	}

	@Override protected int getItemsPerRow() { return 3; }
}
