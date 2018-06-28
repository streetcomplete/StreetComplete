package de.westnordost.streetcomplete.quests.car_wash_type;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;
import de.westnordost.streetcomplete.view.Item;

public class AddCarWashTypeForm extends ImageListQuestAnswerFragment
		implements ImageSelectAdapter.OnItemSelectionListener
{
	public static final String
			AUTOMATED = "AUTOMATED",
			SELF_SERVICE = "SELF_SERVICE",
			SERVICE = "SERVICE";

	private final Item[] TYPES = new Item[] {
			new Item(AUTOMATED, R.drawable.car_wash_automated, R.string.quest_carWashType_automated),
			new Item(SELF_SERVICE, R.drawable.car_wash_self_service, R.string.quest_carWashType_selfService),
			new Item(SERVICE, R.drawable.car_wash_service, R.string.quest_carWashType_service)
	};

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		imageSelector.setOnItemSelectionListener(this);
	}

	@Override protected Item[] getItems() { return TYPES; }
	@Override protected int getItemsPerRow() { return 3; }
	@Override protected int getMaxSelectableItems() { return 3; }

	@Override public void onIndexSelected(int index)
	{
		// service is exclusive with everything else
		if(index == 2)
		{
			imageSelector.deselect(0);
			imageSelector.deselect(1);
		}
		else
		{
			imageSelector.deselect(2);
		}
	}

	@Override public void onIndexDeselected(int index)
	{
		// no thanks, we are fine
	}
}
