package de.westnordost.streetcomplete.quests.complete;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.complete.CompleteTypes;
import de.westnordost.streetcomplete.data.complete.SimpleOverpassCompleteQuestType;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class TactilePavings extends SimpleOverpassCompleteQuestType
{
	@Inject public TactilePavings(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters() { return "nodes with highway=crossing or " +
			"(public_transport=platform or (highway=bus_stop and public_transport!=stop_position))"; }

	@Override public AbstractQuestAnswerFragment createForm() { return new CompleteQuestYesNoAnswerFragment(); }

	@Override public int getIcon() { return R.drawable.ic_quest_blind_pedestrian_crossing; }
	@Override public int getTitle() { return R.string.complete_tactile_pavings_title; }

	@Override public int getApiId() { return 1; }
	@Override public String getCompleteType() { return CompleteTypes.YES_NO; }
}
