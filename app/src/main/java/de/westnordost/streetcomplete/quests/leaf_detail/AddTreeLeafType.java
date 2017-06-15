package de.westnordost.streetcomplete.quests.leaf_detail;

import android.os.Bundle;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddTreeLeafType extends SimpleOverpassQuestType
{
	@Inject public AddTreeLeafType(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes with natural=tree and !leaf_type";
	}

	@Override
	public int importance()
	{
		return QuestImportance.INSIGNIFICANT;
	}

	public AbstractQuestAnswerFragment createForm()
	{
        return new AddTreeLeafTypeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddTreeLeafTypeForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			changes.add("leaf_type", values.get(0));
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add leaf_type";
	}

	@Override public String getIconName() {	return "leaf"; }
}