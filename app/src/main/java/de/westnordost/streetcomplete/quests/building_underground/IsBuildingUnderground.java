package de.westnordost.streetcomplete.quests.building_underground;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class IsBuildingUnderground extends SimpleOverpassQuestType
{
	@Inject public IsBuildingUnderground(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "ways, relations with building and !location and layer~-[0-9]+";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
			changes.add("location", "underground");
		} else {
			changes.add("location", "surface");
		}
	}

	@Override public String getCommitMessage() { return "Determine whatever building is fully underground"; }
	@Override public int getIcon() { return R.drawable.ic_quest_building_underground; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean hasName = tags.containsKey("name");
		if (hasName) {
			return R.string.quest_building_underground_name_title;
		} else{
			return R.string.quest_building_underground_title;
		}
	}
}
