package de.westnordost.osmagent.quests.opening_hours;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.osmagent.R;
import de.westnordost.osmagent.quests.AbstractQuestAnswerFragment;
import de.westnordost.osmapi.map.data.OsmElement;

public class AddOpeningHoursForm extends AbstractQuestAnswerFragment
{
	public static final String OPENING_HOURS = "opening_hours";

	private OpeningHoursPerMonth openingHoursPerMonth;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle();

		View contentView = setContentView(R.layout.quest_opening_hours);

		openingHoursPerMonth = (OpeningHoursPerMonth) contentView.findViewById(R.id.month_select_container);
		if(savedInstanceState == null)
		{
			openingHoursPerMonth.addDefault();
		}

		return view;
	}

	private void setTitle()
	{
		OsmElement element = (OsmElement) getArguments().getSerializable(AbstractQuestAnswerFragment.ELEMENT);
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

	@Override protected void onClickOk()
	{
		String openingHours = openingHoursPerMonth.getOpeningHoursString();
		if(openingHours.equals("00:00-24:00"))
		{
			confirm24_7();
		}
		else
		{
			applyOpeningHours(openingHours);
		}
	}

	private void confirm24_7()
	{
		AlertDialog confirmation = new AlertDialog.Builder(getActivity())
				.setMessage(R.string.quest_openingHours_24_7_confirmation)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						applyOpeningHours("24/7");
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
		return !openingHoursPerMonth.getOpeningHoursString().isEmpty();
	}

}
