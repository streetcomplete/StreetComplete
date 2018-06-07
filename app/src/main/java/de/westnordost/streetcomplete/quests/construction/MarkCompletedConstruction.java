package de.westnordost.streetcomplete.quests.construction;

import android.support.annotation.Nullable;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.meta.OsmTaggings;
import de.westnordost.streetcomplete.data.osm.AOsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.DateUtil;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public abstract class MarkCompletedConstruction extends AOsmElementQuestType
{
	protected final OverpassMapDataDao overpassServer;

	MarkCompletedConstruction(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Nullable
	@Override public Boolean isApplicableTo(Element element)
	{
		/* Whether this element applies to this quest cannot be determined by looking at that
		   element alone (see download()), an Overpass query would need to be made to find this out.
		   This is too heavy-weight for this method so it always returns false. */

		/* The implications of this are that this quest will never be created directly
		   as consequence of solving another quest and also after reverting an input,
		   the quest will not immediately pop up again. Instead, they are downloaded well after an
		   element became fit for this quest. */
		return null;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	protected String getCurrentDateString()
	{
		return DateUtil.getCurrentDateString() + "T00:00:00Z";
	}

	protected String getOffsetDateString(int offset)
	{
		return DateUtil.getOffsetDateString(offset) + "T00:00:00Z";
	}

	String getQueryPart(String key, String nameOfGeneratedGroup, int reviewIntervalInDays)
	{
		// Note that newer segment will ensure that any edit,
		// including adding or updating review marker like check_date or survey:date tags
		// will cause OSM elements to become ineligible for this quest for reviewIntervalInDays days.
		// It allows supporting check_date and any other survey markers without parsing of any tags.
		return "[" + key + "=construction]" +
			"(if:!is_date(t['opening_date']) || date(t['opening_date'])<date('" + getCurrentDateString() + "'))" +
			" -> .construction_with_unknown_state; " +
			getRecentlyEditedConstructionsQueryPart(key, reviewIntervalInDays) + " -> .recently_edited_construction;" +
			"(.construction_with_unknown_state; - .recently_edited_construction;) -> " + nameOfGeneratedGroup + ";";
	}

	private String getRecentlyEditedConstructionsQueryPart(String key, int reviewIntervalInDays)
	{
		return "(" +
			"way[" + key + "=construction](newer: '" + getOffsetDateString(-reviewIntervalInDays) +"');" +
			"relation[" + key + "=construction](newer: '" + getOffsetDateString(-reviewIntervalInDays) +"');" +
			")";
	}

	void removeTagsDescribingConstruction(StringMapChangesBuilder changes)
	{
		changes.deleteIfExists("construction");
		changes.deleteIfExists("source:construction");
		changes.deleteIfExists("opening_date");
		changes.deleteIfExists("source:opening_date");
		changes.deleteIfExists(OsmTaggings.SURVEY_MARK_KEY);
		changes.deleteIfExists("source:" + OsmTaggings.SURVEY_MARK_KEY);
	}

	@Override public String getCommitMessage() { return "Determine whether construction is now completed"; }
}
