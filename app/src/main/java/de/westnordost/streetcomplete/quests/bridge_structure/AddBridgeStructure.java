package de.westnordost.streetcomplete.quests.bridge_structure;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;


public class AddBridgeStructure extends SimpleOverpassQuestType{


    public AddBridgeStructure(OverpassMapDataDao overpassServer) {
        super(overpassServer);
    }

    @Override
    public AbstractQuestAnswerFragment createForm() {
        return null;
    }

    @Override
    public int getIcon() {
        return 0;
    }

    @Override
    public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) {

    }

    @Override
    public String getCommitMessage() {
        return null;
    }

    @Override
    public int getTitle(@NonNull Map<String, String> tags) {
        return 0;
    }

    @Override
    protected String getTagFilters() {
        return null;
    }
}
