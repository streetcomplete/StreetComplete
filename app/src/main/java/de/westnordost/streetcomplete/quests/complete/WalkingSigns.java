package de.westnordost.streetcomplete.quests.complete;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.complete.CompleteTypes;
import de.westnordost.streetcomplete.data.complete.SimpleOverpassCompleteQuestType;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class WalkingSigns extends SimpleOverpassCompleteQuestType
{
	@Inject public WalkingSigns(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters() { return "ways with highway ~ cycleway|footway|path and" +
			" ((bicycle ~ yes|designated) or (foot ~ yes|designated))";
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new CompleteQuestImageAnswerFragment(); }

	@Override public int getIcon() { return R.drawable.ic_quest_pedestrian; }
	@Override public int getTitle() { return R.string.complete_walking_signs_title; }

	@Override public int getApiId() { return 3; }
	@Override public String getCompleteType() { return CompleteTypes.IMAGE; }
}
