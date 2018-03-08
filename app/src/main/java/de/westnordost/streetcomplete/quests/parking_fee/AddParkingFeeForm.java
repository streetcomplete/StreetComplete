package de.westnordost.streetcomplete.quests.parking_fee;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	                            IS_FEE_ONLY_AT_HOURS = "oh_fee_only_at",
	                            IS_DEFINING_HOURS = "oh";

	private boolean isDefiningHours;
	private boolean isFeeOnlyAtHours;
	private AddOpeningHoursAdapter openingHoursAdapter;
	private Button buttonOk, buttonYes, buttonNo;

	private View hoursView;

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

		addOtherAnswer(R.string.quest_fee_answer_hours, () -> setOpeningHoursMode(true));

		hoursView = setContentView(R.layout.quest_fee_hours);

		ArrayList<OpeningMonths> data = loadOpeningHoursData(savedInstanceState);
		openingHoursAdapter = new AddOpeningHoursAdapter(data, getActivity(), getCountryInfo());
		RecyclerView openingHoursList = hoursView.findViewById(R.id.hours_list);
		openingHoursList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		openingHoursList.setAdapter(openingHoursAdapter);
		openingHoursList.setNestedScrollingEnabled(false);

		Button addTimes = hoursView.findViewById(R.id.btn_add);
		addTimes.setOnClickListener((v) -> openingHoursAdapter.addNewWeekdays());

		isFeeOnlyAtHours = savedInstanceState == null || savedInstanceState.getBoolean(IS_FEE_ONLY_AT_HOURS, true);

		List<String> speedUnits = Arrays.asList(
			getString(R.string.quest_fee_only_at_hours),
			getString(R.string.quest_fee_not_at_hours));
		Spinner select = hoursView.findViewById(R.id.select);
		select.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item_centered, speedUnits));
		select.setSelection(isFeeOnlyAtHours ? 0 : 1);
		select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				isFeeOnlyAtHours = position == 0;
			}

			@Override public void onNothingSelected(AdapterView<?> parent) { }
		});

		setOpeningHoursMode(savedInstanceState != null && savedInstanceState.getBoolean(IS_DEFINING_HOURS));

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
		bundle.putBoolean(FEE, !isFeeOnlyAtHours);
		bundle.putString(FEE_CONDITONAL_HOURS, openingHoursAdapter.toString());
		applyImmediateAnswer(bundle);
	}

	private void onClickYesNo(boolean answer)
	{
		Bundle bundle = new Bundle();
		bundle.putBoolean(FEE, answer);
		applyImmediateAnswer(bundle);
	}

	private ArrayList<OpeningMonths> loadOpeningHoursData(Bundle savedInstanceState)
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
		return data;
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putByteArray(OPENING_HOURS_DATA, serializer.toBytes(openingHoursAdapter.getData()));
		outState.putBoolean(IS_DEFINING_HOURS, isDefiningHours);
		outState.putBoolean(IS_FEE_ONLY_AT_HOURS, isFeeOnlyAtHours);
	}

	private void setOpeningHoursMode(boolean isDefiningHours)
	{
		this.isDefiningHours = isDefiningHours;

		hoursView.setVisibility(isDefiningHours ? View.VISIBLE : View.GONE);
		buttonOk.setVisibility(isDefiningHours ? View.VISIBLE : View.GONE);
		buttonNo.setVisibility(isDefiningHours ? View.GONE : View.VISIBLE);
		buttonYes.setVisibility(isDefiningHours ? View.GONE : View.VISIBLE);
	}

	@Override public boolean hasChanges()
	{
		return isDefiningHours && !openingHoursAdapter.toString().isEmpty();
	}
}
