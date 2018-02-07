package de.westnordost.streetcomplete.quests;

import java.util.ArrayList;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingTypeForm;

public class AddBridgeStructureTest extends AOsmElementQuestTypeTest {

    @Override
    public void setUp() throws Exception
	{
        super.setUp();
        tags.put("man_made", "bridge");
    }

    public void testStructure()
	{
		ArrayList<String> values = new ArrayList<>();
		values.add("arch");
		bundle.putStringArrayList(AddRecyclingTypeForm.OSM_VALUES, values);
        verify(new StringMapEntryAdd("bridge:structure", "arch"));
    }

    @Override
    protected OsmElementQuestType createQuestType() {
        return new AddBridgeStructure(null);
    }
}
