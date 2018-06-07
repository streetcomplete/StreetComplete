package de.westnordost.streetcomplete.quests.surface;

import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

import static de.westnordost.streetcomplete.quests.surface.Surface.*;

public class AddPathSurfaceForm extends GroupedImageListQuestAnswerFragment
{
	@Override protected Item[] getTopItems()
	{
		// the path type is a strong indicator for what surfaces are likely
		String pathType = determinePathType(getOsmElement().getTags());
		switch (pathType)
		{
			case "bridleway":
				return new Item[] { DIRT, GRASS, SAND, PEBBLES, FINE_GRAVEL, COMPACTED };
			case "path":
				return new Item[] { DIRT, PEBBLES, COMPACTED, ASPHALT, FINE_GRAVEL, PAVING_STONES };
			case "footway":
				return new Item[] { PAVING_STONES, ASPHALT, CONCRETE, COMPACTED, FINE_GRAVEL, DIRT };
			case "cycleway":
				return new Item[] { PAVING_STONES, ASPHALT, CONCRETE, COMPACTED, WOOD, METAL };
			case "steps":
				return new Item[] { PAVING_STONES, ASPHALT, CONCRETE, WOOD, SETT, UNHEWN_COBBLESTONE };
		}
		throw new IllegalStateException("Unexpected path type " + pathType);
	}

	@Override protected Item[] getAllItems()
	{
		// except for different panorama images, should be the same as for the road quest, to avoid confusion
		return new Item[] {
			new Item("paved", R.drawable.panorama_path_surface_paved, R.string.quest_surface_value_paved, new Item[]{
				ASPHALT, CONCRETE, PAVING_STONES,
				SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
				WOOD, METAL,
			}),
			new Item("unpaved", R.drawable.panorama_path_surface_unpaved, R.string.quest_surface_value_unpaved, new Item[]{
				COMPACTED, FINE_GRAVEL, GRAVEL,
				PEBBLES,
			}),
			new Item("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, new Item[]{
				DIRT, GRASS, SAND
			}),
		};
	}

	private static String determinePathType(Map<String,String> tags)
	{
		String pathType = tags.get("highway");
		// interpet paths with foot/bicycle/horse=designated as...
		if("path".equals(pathType))
		{
			if ("designated".equals(tags.get("bicycle"))) return "cycleway";
			if ("designated".equals(tags.get("horse"))) return "bridleway";
			if ("designated".equals(tags.get("foot"))) return "footway";
		}
		return pathType;
	}
}
