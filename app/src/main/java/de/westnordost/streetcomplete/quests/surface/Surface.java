package de.westnordost.streetcomplete.quests.surface;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.Item;

public class Surface
{
	public static final Item
		ASPHALT =       new Item("asphalt", R.drawable.surface_asphalt, R.string.quest_surface_value_asphalt),
		CONCRETE =      new Item("concrete", R.drawable.surface_concrete, R.string.quest_surface_value_concrete),
		FINE_GRAVEL =   new Item("fine_gravel", R.drawable.surface_fine_gravel, R.string.quest_surface_value_fine_gravel),
		PAVING_STONES = new Item("paving_stones", R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones),
		COMPACTED =     new Item("compacted", R.drawable.surface_compacted, R.string.quest_surface_value_compacted),
		DIRT =          new Item("dirt", R.drawable.surface_dirt, R.string.quest_surface_value_dirt),
		SETT =          new Item("sett", R.drawable.surface_sett, R.string.quest_surface_value_sett),
		// https://forum.openstreetmap.org/viewtopic.php?id=61042
		UNHEWN_COBBLESTONE = new Item("unhewn_cobblestone", R.drawable.surface_cobblestone, R.string.quest_surface_value_unhewn_cobblestone),
		GRASS_PAVER =   new Item("grass_paver", R.drawable.surface_grass_paver, R.string.quest_surface_value_grass_paver),
		WOOD =          new Item("wood", R.drawable.surface_wood, R.string.quest_surface_value_wood),
		METAL =         new Item("metal", R.drawable.surface_metal, R.string.quest_surface_value_metal),
		GRAVEL =        new Item("gravel", R.drawable.surface_gravel, R.string.quest_surface_value_gravel),
		PEBBLES =       new Item("pebblestone", R.drawable.surface_pebblestone, R.string.quest_surface_value_pebblestone),
		GRASS =         new Item("grass", R.drawable.surface_grass, R.string.quest_surface_value_grass),
		SAND =          new Item("sand", R.drawable.surface_sand, R.string.quest_surface_value_sand);
}
