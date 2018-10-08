package de.westnordost.streetcomplete.quests.bike_parking_capacity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.TextChangedWatcher;

public class AddBikeParkingCapacityForm extends AbstractQuestFormAnswerFragment
{
	public static final String BIKE_PARKING_CAPACITY = "bike_parking_capacity";

	private EditText capacityInput;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View contentView = setContentView(R.layout.quest_bike_parking_capacity);

		capacityInput = contentView.findViewById(R.id.capacityInput);
		capacityInput.addTextChangedListener(new TextChangedWatcher(this::checkIsFormComplete));

		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		answer.putInt(BIKE_PARKING_CAPACITY, Integer.parseInt(getBikeCapacity()));
		applyAnswer(answer);
	}

	@Override public boolean isFormComplete() { return !getBikeCapacity().isEmpty(); }

	private String getBikeCapacity() { return capacityInput.getText().toString(); }
}
