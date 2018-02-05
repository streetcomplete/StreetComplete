package de.westnordost.streetcomplete.quests.religion;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddReligionToWaysideShrine extends SimpleOverpassQuestType {
    @Inject
    public AddReligionToWaysideShrine(OverpassMapDataDao overpassServer) { super(overpassServer); }

    @Override
    protected String getTagFilters() {
        return "nodes, ways, relations with historic=wayside_shrine and" +
                " !religion and" +
                " (access !~ private|no)"; // exclude ones without access to general public
    }

    public AbstractQuestAnswerFragment createForm()
    {
        return new AddReligionToPlaceOfWorshipForm();
    }

    public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
    {
        ArrayList<String> values = answer.getStringArrayList(AddReligionToPlaceOfWorshipForm.OSM_VALUES);
        if(values != null && !values.isEmpty())
        {
            String religionValueStr = values.get(0);
            changes.add("religion", religionValueStr);
        }
    }

    @Override public String getCommitMessage() { return "Add religion to historic=wayside_shrine"; }
    @Override public int getIcon() { return R.drawable.ic_quest_religion; }
    @Override public int getTitle(Map<String,String> tags)
    {
        return R.string.quest_religion_for_wayside_shrine_title;
    }
}
