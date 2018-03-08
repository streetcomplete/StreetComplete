package de.westnordost.streetcomplete.quests.max_height;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddMaxHeightForm extends AbstractQuestFormAnswerFragment
{
	public static final String
		MAX_HEIGHT = "max_height",
		NO_SIGN = "no_sign";

	private EditText heightInput, feetInput, inchInput;

	private boolean isMetric;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		isMetric = getCountryInfo().getHeightUnits().get(0).equals("m");

		setMaxHeightSignLayout(getMaxHeightLayoutResourceId());
		addOtherAnswers();

		return view;
	}

	private void setMaxHeightSignLayout(int resourceId)
	{
		View contentView = setContentView(resourceId);

		heightInput = contentView.findViewById(R.id.maxHeightInput);
		feetInput = contentView.findViewById(R.id.maxHeightFeetInput);
		inchInput = contentView.findViewById(R.id.maxHeightInchInput);
	}

	private int getMaxHeightLayoutResourceId()
	{
		return isMetric ? R.layout.quest_max_height : R.layout.quest_max_height_us;
	}

	private void addOtherAnswers()
	{

		if (getCountryInfo().getHeightUnits().size() == 2)
		{
			if (isMetric)
			{
				addOtherAnswer(R.string.quest_maxheight_answer_imperial_unit, () ->
				{
					isMetric = false;
					setMaxHeightSignLayout(getMaxHeightLayoutResourceId());
				});
			}
			else
			{
				addOtherAnswer(R.string.quest_maxheight_answer_metric_unit, () ->
				{
					isMetric = true;
					setMaxHeightSignLayout(getMaxHeightLayoutResourceId());
				});
			}
		}

		addOtherAnswer(R.string.quest_maxheight_answer_noSign, () ->
		{
			new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_maxheight_answer_noSign_confirmation_title)
				.setMessage(R.string.quest_maxheight_answer_noSign_confirmation)
				.setPositiveButton(R.string.quest_maxheight_answer_noSign_confirmation_positive, (dialog, which) -> {
					Bundle answer = new Bundle();
					answer.putBoolean(NO_SIGN, true);
					applyImmediateAnswer(answer);
				})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
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
		double height = Double.parseDouble(getHeightFromInput());
		double heightInMeter = isMetric ? height : feetToMeter(height);
		return heightInMeter > 6;
	}

	private static double feetToMeter(double feet)
	{
		return feet / 3.2808;
	}

	private void applyMaxHeightFormAnswer()
	{
		Bundle answer = new Bundle();

		String height = getFinalMaxHeight(getHeightFromInput());

		// metric is the OSM default, does not need to be mentioned
		if(!isMetric)
		{
			//this adds an apostrophe and a double-quote to be in a format like e.g. 6'7"
			height = height.replace(".", "'");
			height += "\"";
		}
		answer.putString(MAX_HEIGHT, height);

		applyFormAnswer(answer);
	}

	private String getFinalMaxHeight(String value)
	{
		if (isMetric)
		{
			/*TODO: should a trailing zero be added or does it even work without?
			https://taginfo.openstreetmap.org/keys/maxheight#values contains both...*/
			if (!value.contains("."))
			{
				value += ".0";
			}
			return value;
		}
		else
		{
			return value.replace(".", "'") + "\"";
		}
	}

	private String getHeightFromInput()
	{
		if (isMetric)
		{
			if (!heightInput.getText().toString().isEmpty())
			{

				return heightInput.getText().toString().replace(",", ".");
			}
			else
			{
				return null;
			}
		}
		else
		{
			if (!feetInput.getText().toString().isEmpty() && !inchInput.getText().toString().isEmpty())
			{
				return feetInput.getText().toString() + "." + inchInput.getText().toString();
			}
			else
			{
				return null;
			}
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
		return getHeightFromInput() != null;
	}
}
