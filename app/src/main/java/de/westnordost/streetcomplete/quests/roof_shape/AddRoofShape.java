package de.westnordost.streetcomplete.quests.roof_shape;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddRoofShape extends SimpleOverpassQuestType
{
	@Inject public AddRoofShape(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "ways, relations with roof:levels and roof:levels!=0 and !roof:shape and !3dr:type and !3dr:roof";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddRoofShapeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddRoofShapeForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			changes.add("roof:shape", values.get(0));
		}
	}

	@Override public String getCommitMessage() { return "Add roof shapes"; }
	@Override public int getIcon() { return R.drawable.ic_quest_roof_shape; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_roofShape_title;
	}
}
