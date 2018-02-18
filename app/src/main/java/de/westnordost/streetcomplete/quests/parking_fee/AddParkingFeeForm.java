package de.westnordost.streetcomplete.quests.parking_fee;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursAdapter;
import de.westnordost.streetcomplete.quests.opening_hours.OpeningMonths;
import de.westnordost.streetcomplete.util.Serializer;

public class AddParkingFeeForm extends AbstractQuestAnswerFragment
{

	public static final String FEE = "fee",
	                           FEE_CONDITONAL_HOURS = "fee_conditional_hours";

	private static final String	OPENING_HOURS_DATA = "oh_data",
	                            IS_DEFINING_OPENING_HOURS = "oh";

	private boolean isDefiningHours;
	private AddOpeningHoursAdapter openingHoursAdapter;
	private Button buttonOk, buttonYes, buttonNo;

	private View openingHoursView;

	@Inject Serializer serializer;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_yes_no_ok);
		buttonOk = buttonPanel.findViewById(R.id.buttonOk);
		buttonOk.setOnClickListener(v -> onClickOk());

		buttonYes = buttonPanel.findViewById(R.id.buttonYes);
		buttonYes.setOnClickListener(v -> onClickYesNo(true));

		buttonNo = buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(v -> onClickYesNo(false));

		addOtherAnswer(R.string.quest_parking_fee_answer_opening_hours, () -> setOpeningHoursMode(true));

		openingHoursView = setContentView(R.layout.quest_opening_hours);

		initOpeningHoursView(openingHoursView, savedInstanceState);

		setOpeningHoursMode(savedInstanceState != null && savedInstanceState.getBoolean(IS_DEFINING_OPENING_HOURS));

		return view;
	}

	private void onClickOk()
	{
		if(!hasChanges())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putString(FEE_CONDITONAL_HOURS, openingHoursAdapter.toString());
		applyImmediateAnswer(bundle);
	}

	private void onClickYesNo(boolean answer)
	{
		Bundle bundle = new Bundle();
		bundle.putBoolean(FEE, answer);
		applyImmediateAnswer(bundle);
	}

	private void initOpeningHoursView(View contentView, Bundle savedInstanceState)
	{
		ArrayList<OpeningMonths> data;
		if(savedInstanceState != null)
		{
			data = serializer.toObject(savedInstanceState.getByteArray(OPENING_HOURS_DATA),ArrayList.class);
		}
		else
		{
			data = new ArrayList<>();
			data.add(new OpeningMonths());
		}

		openingHoursAdapter = new AddOpeningHoursAdapter(data, getActivity(), getCountryInfo());
		RecyclerView openingHoursList = contentView.findViewById(R.id.opening_hours_list);
		openingHoursList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		openingHoursList.setAdapter(openingHoursAdapter);
		openingHoursList.setNestedScrollingEnabled(false);

		Button addTimes = openingHoursView.findViewById(R.id.btn_add);
		addTimes.setOnClickListener((v) -> openingHoursAdapter.addNewWeekdays());
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putByteArray(OPENING_HOURS_DATA, serializer.toBytes(openingHoursAdapter.getData()));
		outState.putBoolean(IS_DEFINING_OPENING_HOURS, isDefiningHours);
	}

	private void setOpeningHoursMode(boolean isDefiningHours)
	{
		this.isDefiningHours = isDefiningHours;

		openingHoursView.setVisibility(isDefiningHours ? View.VISIBLE : View.GONE);
		buttonOk.setVisibility(isDefiningHours ? View.VISIBLE : View.GONE);
		buttonNo.setVisibility(isDefiningHours ? View.GONE : View.VISIBLE);
		buttonYes.setVisibility(isDefiningHours ? View.GONE : View.VISIBLE);
	}

	@Override public boolean hasChanges()
	{
		return isDefiningHours && openingHoursAdapter.toString().isEmpty();
	}
}
