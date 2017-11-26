package de.westnordost.streetcomplete.quests.bridge_structure;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;


public class AddBridgeStructure extends SimpleOverpassQuestType{


    @Inject
    public AddBridgeStructure(OverpassMapDataDao overpassServer) {
        super(overpassServer);
    }

    @Override
    public AbstractQuestAnswerFragment createForm() {
        return new AddBridgeStructureForm();
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_quest_bridge;
    }

    @Override
    public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) {
        changes.add("structure", answer.getString(AddBridgeStructureForm.STRUCTURE));
    }

    @Override
    public String getCommitMessage() {
        return "Add bridge structure";
    }

    @Override
    public int getTitle(Map<String, String> tags) {
        boolean hasName = tags.containsKey("name");
        if (hasName) return R.string.quest_bridge_structure_name_title;
        else return R.string.quest_bridge_structure_title;
    }
    @Override
    protected String getTagFilters() {
        return "ways with bridge and !structure";
    }
}
