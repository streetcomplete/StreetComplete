package de.westnordost.streetcomplete.quests.tracktype;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddTracktype extends SimpleOverpassQuestType
{
    @Inject public AddTracktype(OverpassMapDataDao overpassServer) { super(overpassServer); }

    @Override
    protected String getTagFilters() { return "ways with highway=track and !tracktype" +
			" and (access !~ private|no or (foot and foot !~ private|no))"; }

    public AbstractQuestAnswerFragment createForm() { return new AddTracktypeForm(); }

    public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
    {
        List<String> values = answer.getStringArrayList(AddTracktypeForm.OSM_VALUES);
        if(values != null  && values.size() == 1)
        {
            changes.add("tracktype", values.get(0));
        }
    }

    @Override public String getCommitMessage() { return "Add tracktype"; }
    @Override public int getIcon() { return R.drawable.ic_quest_tractor; }
    @Override public int getTitle(@NonNull Map<String, String> tags) {
    	return R.string.quest_tracktype_title;
    }
}
