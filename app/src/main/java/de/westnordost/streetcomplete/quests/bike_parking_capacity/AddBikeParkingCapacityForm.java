package de.westnordost.streetcomplete.quests.bike_parking_capacity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.quests.TextInputQuestAnswerFragment;
import de.westnordost.streetcomplete.util.TextChangedWatcher;

public class AddBikeParkingCapacityForm extends TextInputQuestAnswerFragment
{
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setContentView(R.layout.quest_bike_parking_capacity);
		return view;
	}

	@Override protected EditText getEditText()
	{
		return getView().findViewById(R.id.capacityInput);
	}
}
