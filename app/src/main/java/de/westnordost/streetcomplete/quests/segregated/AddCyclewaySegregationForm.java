package de.westnordost.streetcomplete.quests.segregated;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddCyclewaySegregationForm extends ImageListQuestAnswerFragment {
	@Override protected Item[] getItems() {
		return new Item[]{
			new Item("yes", getCountryInfo().isLeftHandTraffic() ?
				R.drawable.ic_path_segregated_l : R.drawable.ic_path_segregated, R.string.quest_segregated_separated),
			new Item("no", R.drawable.ic_path_segregated_no, R.string.quest_segregated_mixed),
		};
	}

	@Override protected int getItemsPerRow() { return 2; }
}
