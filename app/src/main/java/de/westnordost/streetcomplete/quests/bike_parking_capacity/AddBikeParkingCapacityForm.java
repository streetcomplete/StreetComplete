package de.westnordost.streetcomplete.quests.bike_parking_capacity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;

public class AddBikeParkingCapacityForm extends AbstractQuestFormAnswerFragment
{
	public static final String BIKE_PARKING_CAPACITY = "bike_parking_capacity";

	private EditText capacityInput;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_bikeParkingCapacity_title);

		View contentView = setContentView(R.layout.quest_bike_parking_capacity);

		capacityInput = (EditText) contentView.findViewById(R.id.capacityInput);

		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		String bikeCapacityString = capacityInput.getText().toString();

		if(hasChanges())
		{
			int bikeCapacity = Integer.parseInt(bikeCapacityString);
			answer.putInt(BIKE_PARKING_CAPACITY, bikeCapacity);
		}
		applyFormAnswer(answer);
	}

	@Override public boolean hasChanges()
	{
		return !capacityInput.getText().toString().isEmpty();
	}
}