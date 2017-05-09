package de.westnordost.streetcomplete.quests.building_levels;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;

public class AddBuildingLevelsForm extends AbstractQuestFormAnswerFragment
{
	public static final String BUILDING_LEVELS = "building_levels";
	public static final String ROOF_LEVELS = "roof_levels";

	private EditText levelsInput, roofLevelsInput;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle();

		View contentView = setContentView(R.layout.quest_building_levels);

		levelsInput = (EditText) contentView.findViewById(R.id.levelsInput);
		roofLevelsInput = (EditText) contentView.findViewById(R.id.roofLevelsInput);

		return view;
	}

	private void setTitle()
	{
		if(getOsmElement().getTags().containsKey("building:part"))
		{
			setTitle(R.string.quest_buildingLevels_title_buildingPart);
		}
		else
		{
			setTitle(R.string.quest_buildingLevels_title);
		}
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		String totalBuildingLevelsString = levelsInput.getText().toString();
		String roofLevelsString  = roofLevelsInput.getText().toString();

		if (totalBuildingLevelsString.isEmpty())
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
		}
		else
		{
			// the form asks for "levels in total" because it is more intuitive to ask but OSM expects
			// the building:levels to not include the roof
			int totalBuildingLevels = Integer.parseInt(totalBuildingLevelsString);
			int roofLevels = !roofLevelsString.isEmpty() ? Integer.parseInt(roofLevelsString) : 0;
			int buildingLevels = totalBuildingLevels - roofLevels; // without roof

			if(buildingLevels < 0)
			{
				Toast.makeText(getActivity(), R.string.quest_buildingLevels_negativeBuildingLevels, Toast.LENGTH_LONG).show();
				return;
			}

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