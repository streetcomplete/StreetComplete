package de.westnordost.streetcomplete.quests.religion;

import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;

public class AddReligionToWaysideShrine extends AbstractAddReligionToQuestType
{
    @Inject public AddReligionToWaysideShrine(OverpassMapDataDao overpassServer) { super(overpassServer); }

    @Override protected String getTagFilters()
    {
        return "nodes, ways, relations with historic=wayside_shrine and" +
                " !religion and" +
                " (access !~ private|no)"; // exclude ones without access to general public
    }

    @Override public String getCommitMessage() { return "Add religion for wayside shrine"; }
    @Override public int getTitle(@NonNull Map<String,String> tags)
    {
        return R.string.quest_religion_for_wayside_shrine_title;
    }
}
