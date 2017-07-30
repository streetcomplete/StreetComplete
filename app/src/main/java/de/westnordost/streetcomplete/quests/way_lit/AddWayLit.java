package de.westnordost.streetcomplete.quests.way_lit;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddWayLit extends SimpleOverpassQuestType
{
	static final String[] ROADS_WITH_LIGHT = {
			"primary", "secondary", "tertiary", "unclassified", "residential", "living_street",
			"service", "pedestrian"
	};

	static final String[] WAYS_WITH_LIGHT = {
			"footway", "road", "cycleway", "path"
	};

	@Inject public AddWayLit(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "ways with highway ~ " + TextUtils.join("|", ROADS_WITH_LIGHT)
				+ "|" + TextUtils.join("|", WAYS_WITH_LIGHT) + " and !lit";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new WayLitForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String other = answer.getString(WayLitForm.OTHER_ANSWER);
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
		return "Add way lit";
	}

	@Override public String getIconName()
	{
		return "lantern";
	}
}
