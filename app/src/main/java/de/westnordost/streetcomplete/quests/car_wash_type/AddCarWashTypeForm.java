package de.westnordost.streetcomplete.quests.car_wash_type;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.ImageSelectAdapter;

public class AddCarWashTypeForm extends ImageListQuestAnswerFragment
		implements ImageSelectAdapter.OnItemSelectionListener
{
	public static final String
			AUTOMATED = "AUTOMATED",
			SELF_SERVICE = "SELF_SERVICE",
			SERVICE = "SERVICE";

	private final ImageListQuestAnswerFragment.OsmItem[] TYPES = new ImageListQuestAnswerFragment.OsmItem[] {
			new ImageListQuestAnswerFragment.OsmItem(AUTOMATED, R.drawable.car_wash_automated, R.string.quest_carWashType_automated),
			new ImageListQuestAnswerFragment.OsmItem(SELF_SERVICE, R.drawable.car_wash_self_service, R.string.quest_carWashType_selfService),
			new ImageListQuestAnswerFragment.OsmItem(SERVICE, R.drawable.car_wash_service, R.string.quest_carWashType_service)
	};

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		imageSelector.setOnItemSelectionListener(this);
	}

	@Override protected ImageListQuestAnswerFragment.OsmItem[] getItems()
	{
		return TYPES;
	}

	@Override protected int getItemsPerRow()
	{
		return 3;
	}

	@Override protected int getMaxSelectableItems()
	{
		return 3;
	}

	@Override public void onIndexSelected(int index)
	{
		// service is exclusive with everything else
		if(index == 2)
		{
			imageSelector.deselectIndex(0);
			imageSelector.deselectIndex(1);
		}
		else
		{
			imageSelector.deselectIndex(2);
		}
	}

	@Override public void onIndexDeselected(int index)
	{
		// no thanks, we are fine
	}
}
