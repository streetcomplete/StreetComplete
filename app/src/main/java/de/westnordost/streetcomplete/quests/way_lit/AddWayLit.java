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
	static final String[] LIT_RESIDENTIAL_ROADS = { "residential", "living_street", "pedestrian" };

	static final String[] LIT_NON_RESIDENTIAL_ROADS = {
			"primary", "secondary", "tertiary", "unclassified", "service",
	};

	private static final String[] LIT_WAYS = { "footway", "cycleway" };

	@Inject public AddWayLit(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		/* Using sidewalk as a tell-tale tag for (urban) streets which reached a certain level of
		   development. I.e. non-urban streets will usually not even be lit in industrialized
		   countries.
		   Also, only include paths only for those which are equal to footway/cycleway to exclude
		   most hike paths and trails.

		   See #427 for discussion. */

		return "ways with " +
				"(" +
				" highway ~ " + TextUtils.join("|", LIT_RESIDENTIAL_ROADS) +
				" or" +
				" highway ~ " + TextUtils.join("|", LIT_NON_RESIDENTIAL_ROADS) +
				" and ( sidewalk ~ both|left|right|yes|separate or source:maxspeed ~ .+:urban )" +
				" or" +
				" highway ~ " + TextUtils.join("|", LIT_WAYS) +
				" or" +
				" highway = path and (foot = designated or bicycle = designated)" +
				")" +
				" and !lit";
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
