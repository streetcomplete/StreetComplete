package de.westnordost.streetcomplete.quests.road_surface;

import de.westnordost.streetcomplete.R;

public class AddRoadSurfaceForm extends RoadSurfaceForm {
	RoadSurfaceForm.Surface[] GetSurfaceMenuStructure()
	{
		return new RoadSurfaceForm.Surface[]{
				new RoadSurfaceForm.Surface("paved", R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, new RoadSurfaceForm.Surface[]{
						new RoadSurfaceForm.Surface("asphalt", R.drawable.surface_asphalt, R.string.quest_surface_value_asphalt),
						new RoadSurfaceForm.Surface("concrete", R.drawable.surface_concrete, R.string.quest_surface_value_concrete),
						new RoadSurfaceForm.Surface("sett", R.drawable.surface_sett, R.string.quest_surface_value_sett),
						new RoadSurfaceForm.Surface("paving_stones", R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones),
						new RoadSurfaceForm.Surface("cobblestone", R.drawable.surface_cobblestone, R.string.quest_surface_value_cobblestone),
						new RoadSurfaceForm.Surface("wood", R.drawable.surface_wood, R.string.quest_surface_value_wood),
				}),
				new RoadSurfaceForm.Surface("unpaved", R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, new RoadSurfaceForm.Surface[]{
						new RoadSurfaceForm.Surface("compacted", R.drawable.surface_compacted, R.string.quest_surface_value_compacted),
						new RoadSurfaceForm.Surface("gravel", R.drawable.surface_gravel, R.string.quest_surface_value_gravel),
						new RoadSurfaceForm.Surface("fine_gravel", R.drawable.surface_fine_gravel, R.string.quest_surface_value_fine_gravel),
						new RoadSurfaceForm.Surface("pebblestone", R.drawable.surface_pebblestone, R.string.quest_surface_value_pebblestone),
						new RoadSurfaceForm.Surface("grass_paver", R.drawable.surface_grass_paver, R.string.quest_surface_value_grass_paver),
				}),
				new RoadSurfaceForm.Surface("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, new RoadSurfaceForm.Surface[]{
						new RoadSurfaceForm.Surface("dirt", R.drawable.surface_dirt, R.string.quest_surface_value_dirt),
						new RoadSurfaceForm.Surface("grass", R.drawable.surface_grass, R.string.quest_surface_value_grass),
						new RoadSurfaceForm.Surface("sand", R.drawable.surface_sand, R.string.quest_surface_value_sand),
				}),
		};
	}
}
