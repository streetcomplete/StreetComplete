package de.westnordost.streetcomplete.quests.sport;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShapeForm;

public class AddSport extends SimpleOverpassQuestType
{
	@Inject public AddSport(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways with leisure=pitch and (!sport or sport=team_handball or sport=hockey)";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MAJOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddSportForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddRoofShapeForm.OSM_VALUES);
		if(values != null && !values.isEmpty())
		{
			changes.add("sport", TextUtils.join(";", values));
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add pitches sport";
	}

	@Override public String getIconName() {	return "sport"; }
}
