package de.westnordost.streetcomplete.quests.surface;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

import static de.westnordost.streetcomplete.quests.surface.Surface.*;

public class AddRoadSurfaceForm extends GroupedImageListQuestAnswerFragment
{
	@Override protected Item[] getTopItems()
	{
		// tracks often have different surfaces than other roads
		boolean isTrack = "track".equals(getOsmElement().getTags().get("highway"));
		if(isTrack)
			return new Item[] { DIRT, GRASS, PEBBLES, FINE_GRAVEL, COMPACTED, ASPHALT };
		else
			return new Item[] { ASPHALT, CONCRETE, SETT, PAVING_STONES, COMPACTED, DIRT };
	}

	@Override protected Item[] getAllItems()
	{
		return new Item[] {
			new Item("paved", R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, new Item[]{
				ASPHALT, CONCRETE, PAVING_STONES,
				SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
				WOOD, METAL,
			}),
			new Item("unpaved", R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, new Item[]{
				COMPACTED, FINE_GRAVEL, GRAVEL,
				PEBBLES,
			}),
			new Item("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, new Item[]{
				DIRT, GRASS, SAND
			}),
		};
	}
}
