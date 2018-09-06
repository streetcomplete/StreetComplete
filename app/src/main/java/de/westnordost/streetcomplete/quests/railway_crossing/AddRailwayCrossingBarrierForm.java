package de.westnordost.streetcomplete.quests.railway_crossing;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddRailwayCrossingBarrierForm extends ImageListQuestAnswerFragment
{
	@Override protected Item[] getItems() { return new Item[]
		{
			new Item("no", R.drawable.ic_railway_crossing_none, R.string.quest_railway_crossing_barrier_none),
			new Item("half", getCountryInfo().isLeftHandTraffic() ?
				R.drawable.ic_railway_crossing_half_l : R.drawable.ic_railway_crossing_half),
			new Item("double_half", R.drawable.ic_railway_crossing_double_half),
			new Item("full", getCountryInfo().isLeftHandTraffic() ?
				R.drawable.ic_railway_crossing_full_l : R.drawable.ic_railway_crossing_full),
		};
	}
	@Override protected int getItemsPerRow() { return 4; }
}
