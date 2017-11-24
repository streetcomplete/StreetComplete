package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure;
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructureForm;


public class AddBridgeStructureTest extends AOsmElementQuestTypeTest {

    @Override
    public void setUp() {
        super.setUp();
        tags.put("bridge", "structure");
    }

    public void testStructure() {
        bundle.putString(AddBridgeStructureForm.STRUCTURE, "arch");
        verify(new StringMapEntryAdd("structure", "arch"));
    }

    @Override
    protected OsmElementQuestType createQuestType() {
        return new AddBridgeStructure(null);
    }
}
