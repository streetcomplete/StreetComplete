package de.westnordost.streetcomplete.quests.complete;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.complete.CompleteTypes;
import de.westnordost.streetcomplete.data.complete.SimpleOverpassCompleteQuestType;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class PeakFeatures extends SimpleOverpassCompleteQuestType
{
	@Inject public PeakFeatures(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters() { return "nodes with natural=peak"; }

	@Override public AbstractQuestAnswerFragment createForm() { return new CompleteQuestYesNoAnswerFragment(); }

	//TODO: create an icon
	@Override public int getIcon() { return R.drawable.ic_quest_notes; }
	@Override public int getTitle() { return R.string.complete_peakFeatures_title; }

	@Override public int getApiId() { return 1; }
	@Override public String getCompleteType() { return CompleteTypes.CHART; }
}
