package de.westnordost.streetcomplete.quests.powerpoles_material;

import android.os.Bundle;

import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddPowerPolesMaterial extends SimpleOverpassQuestType
{
	@Inject public AddPowerPolesMaterial(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways with power=poles and !material";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddPowerPolesMaterialForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		List<String> values = answer.getStringArrayList(AddPowerPolesMaterialForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			changes.add("material", values.get(0));
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add powerpoles material type";
	}

	@Override public String getIconName() {
		return "quest";
	}
}
