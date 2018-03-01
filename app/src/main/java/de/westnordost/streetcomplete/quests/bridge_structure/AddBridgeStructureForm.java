package de.westnordost.streetcomplete.quests.bridge_structure;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddBridgeStructureForm extends ImageListQuestAnswerFragment
{
	// structures sorted highest to lowest amount of values on taginfo, footbridge-types last
	private final Item[] STRUCTURES = new Item[]
	{
		new Item("beam", R.drawable.bridge_structure_beam),
		new Item("suspension", R.drawable.bridge_structure_suspension),
		new Item("arch", R.drawable.bridge_structure_arch),
		new Item("arch", R.drawable.bridge_structure_tied_arch), // a subtype of arch, but visually quite different
		new Item("truss", R.drawable.bridge_structure_truss),
		new Item("cable-stayed", R.drawable.bridge_structure_cablestayed),
		new Item("humpback", R.drawable.bridge_structure_humpback),
		new Item("simple-suspension", R.drawable.bridge_structure_simple_suspension),
		new Item("floating", R.drawable.bridge_structure_floating),
	};

	@Override protected Item[] getItems() { return STRUCTURES; }
	@Override protected int getItemsPerRow() { return 2; }
}
