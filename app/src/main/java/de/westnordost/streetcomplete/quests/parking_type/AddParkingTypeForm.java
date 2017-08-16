package de.westnordost.streetcomplete.quests.parking_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddParkingTypeForm extends ImageListQuestAnswerFragment
{
	private final OsmItem[] TYPES = new OsmItem[] {
			new OsmItem("surface", R.drawable.parking_type_surface, R.string.quest_parkingType_surface),
			new OsmItem("underground", R.drawable.parking_type_underground, R.string.quest_parkingType_underground),
			new OsmItem("multi-storey", R.drawable.parking_type_multistorey, R.string.quest_parkingType_multiStorage)
	};

	@Override protected OsmItem[] getItems()
	{
		return TYPES;
	}

	@Override protected int getItemsPerRow()
	{
		return 3;
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_parkingType_title);
		return view;
	}
}
