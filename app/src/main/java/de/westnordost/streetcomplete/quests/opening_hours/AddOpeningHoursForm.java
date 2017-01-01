package de.westnordost.streetcomplete.quests.opening_hours;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.osmapi.map.data.OsmElement;

public class AddOpeningHoursForm extends AbstractQuestAnswerFragment
{
	public static final String OPENING_HOURS = "opening_hours";

	private static final String
			FORM_ROOT_IS_MONTHS = "form_root",
			FORM_DATA = "form_data";

	private ViewGroup openingHoursContainer;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle();

		View contentView = setContentView(R.layout.quest_opening_hours);

		openingHoursContainer = (ViewGroup) contentView.findViewById(R.id.opening_hours_container);

		OpeningHoursFormRoot root;
		if(savedInstanceState != null && savedInstanceState.getBoolean(FORM_ROOT_IS_MONTHS))
		{
			openingHoursContainer.addView(new OpeningHoursPerMonth(getActivity()));
		}
		else
		{
			openingHoursContainer.addView(new OpeningHoursPerWeek(getActivity()));
		}

		if(savedInstanceState != null)
		{
			getFormRoot().onRestoreInstanceState(savedInstanceState.getParcelable(FORM_DATA));
		}

		return view;
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(FORM_ROOT_IS_MONTHS, getFormRoot() instanceof OpeningHoursPerMonth);
		outState.putParcelable(FORM_DATA, getFormRoot().onSaveInstanceState());
	}

	private void setTitle()
	{
		OsmElement element = getOsmElement();
		String name = null;
		if(element != null && element.getTags() != null)
		{
			name = element.getTags().get("name");
		}
		if(name != null)
		{
			setTitle(getResources().getString(R.string.quest_openingHours_name_title, name));
		}
		else
		{
			setTitle(R.string.quest_openingHours_unknownName_title);
		}
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_openingHours_answer_247);
		answers.add(R.string.quest_openingHours_answer_seasonal_opening_hours);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(super.onClickOtherAnswer(itemResourceId)) return true;

		if(itemResourceId == R.string.quest_openingHours_answer_247)
		{
			confirm24_7();
			return true;
		}
		if(itemResourceId == R.string.quest_openingHours_answer_seasonal_opening_hours)
		{
			OpeningHoursPerMonth formRoot;
			// already replaced...
			if (getFormRoot() instanceof OpeningHoursPerMonth)
			{
				formRoot = (OpeningHoursPerMonth) getFormRoot();
			}
			else
			{
				openingHoursContainer.removeAllViews();
				formRoot = new OpeningHoursPerMonth(getActivity());
				openingHoursContainer.addView(formRoot);
			}

			formRoot.add();

			return true;
		}

		return false;
	}

	private OpeningHoursFormRoot getFormRoot()
	{
		return (OpeningHoursFormRoot) openingHoursContainer.getChildAt(0);
	}

	@Override protected void onClickOk()
	{
		String openingHours = getFormRoot().getOpeningHoursString();
		applyOpeningHours(openingHours);
	}

	private void confirm24_7()
	{
		AlertDialog confirmation = new AlertDialog.Builder(getActivity())
				.setMessage(R.string.quest_openingHours_24_7_confirmation)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						Bundle answer = new Bundle();
						answer.putString(OPENING_HOURS, "24/7");
						applyOtherAnswer(answer);
					}
				})
				.setNegativeButton(android.R.string.no, null)
				.create();
		confirmation.show();
	}

	private void applyOpeningHours(String openingHours)
	{
		Bundle answer = new Bundle();
		answer.putString(OPENING_HOURS, openingHours);
		applyAnswer(answer);
	}

	@Override public boolean hasChanges()
	{
		String openingHours = getFormRoot().getOpeningHoursString();

		return !openingHours.isEmpty();
	}

}
