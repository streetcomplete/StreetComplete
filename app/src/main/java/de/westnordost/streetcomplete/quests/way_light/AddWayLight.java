package de.westnordost.streetcomplete.quests.way_light;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddWayLight extends SimpleOverpassQuestType
{
	private static final String[] WAYS_WITH_LIGHT = {
			"motorway", "motorway_link", "trunk", "trunk_link",
			"primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
			"unclassified", "residential", "living_street",
			"pedestrian", "footway",
			"track", "road",
			"service",
			"cycleway",
			"path"
	};

	@Inject public AddWayLight(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "ways with ( highway ~ " + TextUtils.join("|", WAYS_WITH_LIGHT) + " and !lit)";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddWayLightFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String other = answer.getString(AddWayLightFragment.OTHER_ANSWER);
		if (other != null)
		{
			changes.add("lit", other);
		}
		else
		{
			changes.add("lit", answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no");
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add way light";
	}

	@Override public String getIconName()
	{
		return "lantern";
	}
}
