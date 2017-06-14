package de.westnordost.streetcomplete.quests.road_surface;

import de.westnordost.streetcomplete.R;

public class DetailUnpavedRoadSurfaceForm extends RoadSurfaceForm {
	Surface[] GetSurfaceMenuStructure()
	{
		return new Surface[]{
				new RoadSurfaceForm.Surface("compacted", R.drawable.surface_compacted, R.string.quest_surface_value_compacted),
				new RoadSurfaceForm.Surface("gravel", R.drawable.surface_gravel, R.string.quest_surface_value_gravel),
				new RoadSurfaceForm.Surface("fine_gravel", R.drawable.surface_fine_gravel, R.string.quest_surface_value_fine_gravel),
				new RoadSurfaceForm.Surface("pebblestone", R.drawable.surface_pebblestone, R.string.quest_surface_value_pebblestone),
				new RoadSurfaceForm.Surface("grass_paver", R.drawable.surface_grass_paver, R.string.quest_surface_value_grass_paver),
				new RoadSurfaceForm.Surface("dirt", R.drawable.surface_dirt, R.string.quest_surface_value_dirt),
				new RoadSurfaceForm.Surface("grass", R.drawable.surface_grass, R.string.quest_surface_value_grass),
				new RoadSurfaceForm.Surface("sand", R.drawable.surface_sand, R.string.quest_surface_value_sand),
		};
	}
}
