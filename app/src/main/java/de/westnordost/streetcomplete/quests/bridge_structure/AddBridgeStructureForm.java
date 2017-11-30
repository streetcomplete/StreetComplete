package de.westnordost.streetcomplete.quests.bridge_structure;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddBridgeStructureForm extends ImageListQuestAnswerFragment {

    public static final String STRUCTURE = "structure";

    /**
     * structures sorted lowest to highest amount of values on taginfo, 30.11.2017
     */
    private final OsmItem[] STRUCTURES = new OsmItem[] {
            new OsmItem("beam", R.drawable.bridge_structure_beam),
            new OsmItem("suspension", R.drawable.bridge_structure_suspension),
            new OsmItem("simple-suspension", R.drawable.bridge_structure_simplesuspension),
            new OsmItem("arch", R.drawable.bridge_structure_arch),
            new OsmItem("truss", R.drawable.bridge_structure_truss),
            new OsmItem("floating", R.drawable.bridge_structure_floating),
            new OsmItem("cable-stayed", R.drawable.bridge_structure_cablestayed),
            new OsmItem("humpback", R.drawable.bridge_structure_humpback)
    };

    @Override
    protected OsmItem[] getItems() {
        return STRUCTURES;
    }
    @Override protected int getItemsPerRow() { return 2; }
}
