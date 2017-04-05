package de.westnordost.streetcomplete.quests.roof_shape;

import android.os.Bundle;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevelsForm;

public class AddRoofShape extends OverpassQuestType
{
	@Override
	protected String getTagFilters()
	{
		return "ways, relations with roof:levels and roof:levels!=0 and !roof:shape";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddRoofShapeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("roof:shape", answer.getString(AddRoofShapeForm.ROOF_SHAPE));
	}

	@Override public String getCommitMessage()
	{
		return "Add roof shapes";
	}

	@Override public String getIconName() {	return "roof_shape"; }
}
