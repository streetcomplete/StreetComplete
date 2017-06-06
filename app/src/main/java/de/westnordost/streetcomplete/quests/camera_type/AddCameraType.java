package de.westnordost.streetcomplete.quests.camera_type;

import android.os.Bundle;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddCameraType extends SimpleOverpassQuestType
{
	@Inject public AddCameraType(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes with surveillance:type=camera and !camera:type";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddCameraTypeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddCameraTypeForm.OSM_VALUES);
		if(values != null && values.size() == 1)
		{
			changes.add("camera:type", values.get(0));
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add camera type";
	}

	@Override public String getIconName() {	return "camera_type"; }
}
