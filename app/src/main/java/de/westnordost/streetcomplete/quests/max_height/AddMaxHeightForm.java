package de.westnordost.streetcomplete.quests.max_height;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddMaxHeightForm extends AbstractQuestFormAnswerFragment
{
	public static final String
		MAX_HEIGHT = "max_height",
		NO_SIGN = "no_sign";

	public static final int
		IS_BELOW_DEFAULT = 1,
		IS_DEFAULT = 2,
		IS_NOT_INDICATED = 3;

	private EditText heightInput, feetInput, inchInput;
	private Spinner heightUnitSelect;

	private View meterInputSign, feetInputSign;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		Height.Unit unit = getCountryInfo().getMeasurementSystem().get(0).equals("metric") ? Height.Unit.METRIC : Height.Unit.IMPERIAL;
		setMaxHeightSignLayout(R.layout.quest_maxheight, unit);
		addOtherAnswers();

		return view;
	}

	private void setMaxHeightSignLayout(int resourceId, Height.Unit unit)
	{
		View contentView = setContentView(getCurrentCountryResources().getLayout(resourceId));

		heightInput = contentView.findViewById(R.id.meterInput);
		feetInput = contentView.findViewById(R.id.feetInput);
		inchInput = contentView.findViewById(R.id.inchInput);

		meterInputSign = contentView.findViewById(R.id.meterInputSign);
		feetInputSign = contentView.findViewById(R.id.feetInputSign);

		heightUnitSelect = contentView.findViewById(R.id.heightUnitSelect);
		initHeightUnitSelect();

		switchLayout(unit);
	}

	private void switchLayout(Height.Unit unit)
	{
		if(meterInputSign != null) meterInputSign.setVisibility(unit.equals(Height.Unit.METRIC) ? View.VISIBLE : View.GONE);
		if(feetInputSign != null) feetInputSign.setVisibility(unit.equals(Height.Unit.IMPERIAL) ? View.VISIBLE : View.GONE);
	}

	private void initHeightUnitSelect()
	{
		List<String> measurementUnits = getCountryInfo().getMeasurementSystem();
		heightUnitSelect.setVisibility(measurementUnits.size() == 1 ? View.GONE : View.VISIBLE);
		heightUnitSelect.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item_centered, getSpinnerItems(measurementUnits)));
		heightUnitSelect.setSelection(0);

		heightUnitSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
			{
				Height.Unit heightUnit = heightUnitSelect.getSelectedItem().equals("m") ? Height.Unit.METRIC : Height.Unit.IMPERIAL;
				switchLayout(heightUnit);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {}
		});
	}

	private List<String> getSpinnerItems(List<String> units)
	{
		List<String> items = new ArrayList<>();

		for (int i = 0; i < units.size(); i++) {
			String unit = units.get(i);
			if (unit.equals("metric"))
			{
				items.add("m");
			} else if (unit.equals("imperial"))
			{
				items.add("ft");
			}
		}
		return items;
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_maxheight_answer_noSign, () ->
		{
			final String
				restrictsTraffic = getResources().getString(R.string.quest_maxheight_answer_restrictsTraffic),
				noTrafficRestriction = getResources().getString(R.string.quest_maxheight_answer_noTrafficRestriction),
				cantEstimate = getResources().getString(R.string.quest_maxheight_answer_cantEstimate);

			final List<String> answers = new ArrayList<>(3);
			answers.add(restrictsTraffic);
			answers.add(noTrafficRestriction);
			answers.add(cantEstimate);

			DialogInterface.OnClickListener onSelect = new DialogInterface.OnClickListener()
			{
				Integer selection = null;

				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (which >= 0)
					{
						selection = which;
						((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
					}
					else if (which == DialogInterface.BUTTON_POSITIVE)
					{
						if(selection == null || selection < 0 || selection >= answers.size()) return;
						onAnswer();
					}
				}

				private void onAnswer()
				{
					String answer = answers.get(selection);
					Bundle data = new Bundle();
					int type = 0;
					if(answer.equals(restrictsTraffic))	type = IS_BELOW_DEFAULT;
					if(answer.equals(noTrafficRestriction))	type = IS_DEFAULT;
					if(answer.equals(cantEstimate))    type = IS_NOT_INDICATED;
					data.putInt(NO_SIGN, type);
					applyImmediateAnswer(data);
				}
			};

			AlertDialog dlg = new AlertDialogBuilder(getActivity())
				.setSingleChoiceItems(answers.toArray(new String[0]), -1, onSelect)
				.setTitle(R.string.quest_maxheight_answer_noSign_question)
				.setPositiveButton(android.R.string.ok, onSelect)
				.setNegativeButton(android.R.string.cancel, null)
				.show();

			dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		});
	}

	@Override protected void onClickOk()
	{
		if(!hasChanges())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		if(userSelectedUnrealisticHeight())
		{
			confirmUnusualInput(this::applyMaxHeightFormAnswer);
		}
		else
		{
			applyMaxHeightFormAnswer();
		}
	}

	private boolean userSelectedUnrealisticHeight()
	{
		double height = getHeightFromInput().toDouble();
		Height.Unit heightUnit = getHeightFromInput().getUnit();

		double heightInMeter = heightUnit.equals(Height.Unit.METRIC) ? height : feetToMeter(height);
		return heightInMeter > 6 || heightInMeter < 2;
	}

	private static double feetToMeter(double feet)
	{
		return feet / 3.2808;
	}

	private void applyMaxHeightFormAnswer()
	{
		String height = getHeightFromInput().toString();

		Bundle answer = new Bundle();
		answer.putString(MAX_HEIGHT, height);
		applyFormAnswer(answer);
	}

	private Height getHeightFromInput()
	{
		boolean isMetric = heightUnitSelect.getSelectedItem().equals("m");

		if (isMetric)
		{
			String input = heightInput.getText().toString();
			if (!input.isEmpty())
			{
				if (input.contains("."))
				{
					String[] parts = input.split("\\.");
					return new Height(parts[0], parts[1], Height.Unit.METRIC);
				} else {
					return new Height(input, "0", Height.Unit.METRIC);
				}
			}
			return new Height();
		}
		else
		{
			if (!feetInput.getText().toString().isEmpty() && !inchInput.getText().toString().isEmpty())
			{
				return new Height(feetInput.getText().toString(), inchInput.getText().toString(), Height.Unit.IMPERIAL);
			}
			return new Height();
		}
	}

	private void confirmUnusualInput(final Runnable callback)
	{
		new AlertDialogBuilder(getActivity())
			.setTitle(R.string.quest_generic_confirmation_title)
			.setMessage(R.string.quest_maxheight_unusualInput_confirmation_description)
			.setPositiveButton(R.string.quest_generic_confirmation_yes, (dialog, which) -> callback.run())
			.setNegativeButton(R.string.quest_generic_confirmation_no, null)
			.show();
	}

	@Override public boolean hasChanges()
	{
		return !getHeightFromInput().isEmpty();
	}
}
