package de.westnordost.streetcomplete.quests.building_levels;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddBuildingLevelsForm extends AbstractQuestFormAnswerFragment
{
	public static final String BUILDING_LEVELS = "building_levels";
	public static final String ROOF_LEVELS = "roof_levels";

	private EditText levelsInput, roofLevelsInput;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_building_levels);

		levelsInput = contentView.findViewById(R.id.levelsInput);
		levelsInput.requestFocus();
		roofLevelsInput = contentView.findViewById(R.id.roofLevelsInput);

		addOtherAnswer(R.string.quest_buildingLevels_answer_multipleLevels,	() ->
		{
			new AlertDialogBuilder(getActivity())
					.setMessage(R.string.quest_buildingLevels_answer_description)
					.setPositiveButton(android.R.string.ok, null)
					.show();
		});

		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		String buildingLevelsString = levelsInput.getText().toString();
		String roofLevelsString  = roofLevelsInput.getText().toString();

		if (buildingLevelsString.isEmpty())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
		}
		else
		{
			int buildingLevels = Integer.parseInt(buildingLevelsString);
			int roofLevels = !roofLevelsString.isEmpty() ? Integer.parseInt(roofLevelsString) : 0;

			answer.putInt(BUILDING_LEVELS, buildingLevels);
			if(!roofLevelsString.isEmpty())
			{
				answer.putInt(ROOF_LEVELS, roofLevels);
			}
			applyFormAnswer(answer);
		}
	}

	@Override public boolean hasChanges()
	{
		return !levelsInput.getText().toString().isEmpty() ||
		       !roofLevelsInput.getText().toString().isEmpty();
	}
}