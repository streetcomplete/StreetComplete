package de.westnordost.streetcomplete.quests.max_height;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
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
		NO_SIGN = "no_sign",
		BELOW_DEFAULT = "below_default",
		DEFAULT = "default";

	private EditText meterInput, feetInput, inchInput;
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
		View contentView = setContentView(resourceId);

		meterInput = contentView.findViewById(R.id.meterInput);
		feetInput = contentView.findViewById(R.id.feetInput);
		inchInput = contentView.findViewById(R.id.inchInput);

		meterInputSign = contentView.findViewById(R.id.meterInputSign);
		feetInputSign = contentView.findViewById(R.id.feetInputSign);

		heightUnitSelect = contentView.findViewById(R.id.heightUnitSelect);

		if (heightUnitSelect != null) initHeightUnitSelect();

		if (inchInput != null)
		{
			inchInput.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) ->
			{
				try
				{
					String input = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
					input = input.substring(0, dstart) + source.toString() + input.substring(dstart, input.length());

					if (Integer.parseInt(input) <= 12) return null;
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				return "";
			}});
		}
		/* Workaround for an Android bug that it assumes the decimal separator to always be the "."
		   for EditTexts with inputType "numberDecimal", independent of Locale. See
		   https://issuetracker.google.com/issues/36907764 .

		   Affected Android versions are all versions till (exclusive) Android Oreo. */
		if(meterInput != null)
		{
			/* actually, let's not care about which separator the user uses, he might be confused
			   whether he should use the one as displayed on the sign or in his phone's locale */
			//char separator = DecimalFormatSymbols.getInstance(getCountryInfo().getLocale()).getDecimalSeparator();
			meterInput.setKeyListener(DigitsKeyListener.getInstance("0123456789,."));
		}

		switchLayout(unit);
	}

	private void switchLayout(Height.Unit unit)
	{
		boolean isMetric = unit == Height.Unit.METRIC;
		boolean isImperial = unit == Height.Unit.IMPERIAL;

		if(meterInputSign != null) meterInputSign.setVisibility(isMetric ? View.VISIBLE : View.GONE);
		if(feetInputSign != null) feetInputSign.setVisibility(isImperial ? View.VISIBLE : View.GONE);

		if(meterInput != null && isMetric) meterInput.requestFocus();
		if(feetInput != null && isImperial) feetInput.requestFocus();
	}

	private void initHeightUnitSelect()
	{
		List<String> measurementUnits = getCountryInfo().getMeasurementSystem();
		heightUnitSelect.setVisibility(measurementUnits.size() == 1 ? View.GONE : View.VISIBLE);
		heightUnitSelect.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_item_centered, getSpinnerItems(measurementUnits)));
		heightUnitSelect.setSelection(0);

		heightUnitSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
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

		for (String unit : units)
		{
			if (unit.equals("metric"))        items.add("m");
			else if (unit.equals("imperial")) items.add("ft");
		}
		return items;
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_maxheight_answer_noSign, () ->
		{
			new AlertDialogBuilder(getActivity())
				.setMessage(R.string.quest_maxheight_answer_noSign_question)
				.setPositiveButton(R.string.quest_generic_hasFeature_yes, (d, w) -> {
					Bundle data = new Bundle();
					data.putString(NO_SIGN, DEFAULT);
					applyImmediateAnswer(data);
				})
				.setNegativeButton(R.string.quest_generic_hasFeature_no, (d, w) -> {
					Bundle data = new Bundle();
					data.putString(NO_SIGN, BELOW_DEFAULT);
					applyImmediateAnswer(data);
				})
				.show();
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
		double height = getHeightFromInput().getInMeters();
		return height > 6 || height < 2;
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
		if (isMetric())
		{
			String input = meterInput.getText().toString().replaceAll(",",".");
			if (!input.isEmpty())
			{
				try
				{
					return new Height(Double.parseDouble(input));
				}
				catch (NumberFormatException e)
				{
					return null;
				}
			}
		}
		else
		{
			if (!feetInput.getText().toString().isEmpty() && !inchInput.getText().toString().isEmpty())
			{
				return new Height(
					Integer.parseInt(feetInput.getText().toString()),
					Integer.parseInt(inchInput.getText().toString())
				);
			}
		}
		return null;
	}

	private boolean isMetric()
	{
		boolean isMetric;

		if (heightUnitSelect != null)
		{
			isMetric = heightUnitSelect.getSelectedItem().equals("m");
		} else {
			List<String> measurementUnits = getCountryInfo().getMeasurementSystem();
			isMetric = measurementUnits.get(0).equals("metric");
		}
		return isMetric;
	}

	private void confirmUnusualInput(final Runnable callback)
	{
		if(getActivity() == null) return;
		new AlertDialogBuilder(getActivity())
			.setTitle(R.string.quest_generic_confirmation_title)
			.setMessage(R.string.quest_maxheight_unusualInput_confirmation_description)
			.setPositiveButton(R.string.quest_generic_confirmation_yes, (dialog, which) -> callback.run())
			.setNegativeButton(R.string.quest_generic_confirmation_no, null)
			.show();
	}

	@Override public boolean hasChanges()
	{
		return getHeightFromInput() != null;
	}
}
