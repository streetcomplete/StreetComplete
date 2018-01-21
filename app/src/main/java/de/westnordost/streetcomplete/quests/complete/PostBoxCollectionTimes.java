package de.westnordost.streetcomplete.quests.complete;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.complete.CompleteTypes;
import de.westnordost.streetcomplete.data.complete.SimpleOverpassCompleteQuestType;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class PostBoxCollectionTimes extends SimpleOverpassCompleteQuestType
{
	@Inject public PostBoxCollectionTimes(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters() { return "nodes, ways with amenity ~ post|post_box"; }

	@Override public AbstractQuestAnswerFragment createForm() { return new CompleteQuestYesNoAnswerFragment(); }

	@Override public int getIcon() { return R.drawable.ic_quest_mail; }
	@Override public int getTitle() { return R.string.complete_postBox_title; }

	@Override public int getApiId() { return 2; }
	@Override public String getCompleteType() { return CompleteTypes.CHART; }
}
