package de.westnordost.streetcomplete.quests.road_name;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddRoadNameForm extends AbstractQuestAnswerFragment
{
	public static final String NO_NAME = "no_name";
	public static final String NAME = "name";

	public static final String NO_PROPER_ROAD = "no_proper_road";
	public static final int IS_SERVICE = 1, IS_LINK = 2, IS_TRACK = 3;

	private AutoCorrectAbbreviationsEditText nameInput;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_streetName_title);
		View contentView = setContentView(R.layout.quest_roadname);

		nameInput = (AutoCorrectAbbreviationsEditText) contentView.findViewById(R.id.nameInput);

		return view;
	}

	@Override protected void onClickOk()
	{
		if(!validate()) return;

		applyNameAnswer();
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_name_answer_noName);
		answers.add(R.string.quest_streetName_answer_noProperStreet);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(super.onClickOtherAnswer(itemResourceId)) return true;

		if(itemResourceId == R.string.quest_name_answer_noName)
		{
			confirmNoStreetName();
			return true;
		}
		else if (itemResourceId == R.string.quest_streetName_answer_noProperStreet)
		{
			selectNoProperStreetWhatThen();
		}

		return false;
	}

	private void applyNameAnswer()
	{
		Bundle data = new Bundle();
		String name = nameInput.getText().toString().trim();
		data.putString(NAME, name);
		applyAnswer(data);
	}

	private boolean validate()
	{
		String name = nameInput.getText().toString().trim();
		if(name.isEmpty())
		{
			nameInput.setError(getResources().getString(R.string.quest_generic_error_field_empty));
			return false;
		}

		if(name.contains(".") || nameInput.containsAbbreviations())
		{
			confirmPossibleAbbreviation();
			return false;
		}
		return true;
	}

	private void confirmPossibleAbbreviation()
	{
		DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				applyNameAnswer();
			}
		};
		DialogInterface.OnClickListener onNo = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// nothing, just go back
			}
		};

		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.quest_streetName_nameWithAbbreviations_confirmation_title)
				.setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
				.setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive, onYes)
				.setNegativeButton(R.string.quest_generic_confirmation_no, onNo)
				.show();
	}

	private void confirmNoStreetName()
	{
		DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Bundle data = new Bundle();
				data.putBoolean(NO_NAME, true);
				applyOtherAnswer(data);
			}
		};
		DialogInterface.OnClickListener onNo = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// nothing, just go back
			}
		};

		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.quest_name_answer_noName_confirmation_title)
				.setMessage(R.string.quest_streetName_answer_noName_confirmation_description)
				.setPositiveButton(R.string.quest_name_noName_confirmation_positive, onYes)
				.setNegativeButton(R.string.quest_generic_confirmation_no, onNo)
				.show();
	}

	private void selectNoProperStreetWhatThen()
	{
		final String
				linkRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_link),
				serviceRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_service),
				trackRoad = getResources().getString(R.string.quest_streetName_answer_noProperStreet_track),
				leaveNote = getResources().getString(R.string.quest_streetName_answer_noProperStreet_leaveNote);

		String highwayValue = getOsmElement().getTags().get("highway");
		boolean mayBeLink = highwayValue.matches("primary|secondary|tertiary");

		final List<String> answers = new ArrayList<>(3);
		if(mayBeLink) answers.add(linkRoad);
		answers.add(serviceRoad);
		answers.add(trackRoad);
		answers.add(leaveNote);

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
				if(answer.equals(leaveNote))
				{
					onClickCantSay();
				}
				else
				{
					Bundle data = new Bundle();
					int type = 0;
					if(answer.equals(linkRoad))		type = IS_LINK;
					if(answer.equals(serviceRoad))	type = IS_SERVICE;
					if(answer.equals(trackRoad))    type = IS_TRACK;
					data.putInt(NO_PROPER_ROAD, type);
					applyOtherAnswer(data);
				}
			}
		};

		AlertDialog dlg = new AlertDialog.Builder(getActivity())
				.setSingleChoiceItems(answers.toArray(new String[0]), -1, onSelect)
				.setTitle(R.string.quest_streetName_answer_noProperStreet_question)
				.setPositiveButton(android.R.string.ok, onSelect)
				.setNegativeButton(android.R.string.cancel, null)
				.show();

		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
	}

	@Override public boolean hasChanges()
	{
		return !nameInput.getText().toString().trim().isEmpty();
	}
}
