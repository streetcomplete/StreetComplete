package de.westnordost.streetcomplete.quests.road_surface;

import de.westnordost.streetcomplete.R;

public class DetailPavedRoadSurfaceForm extends RoadSurfaceForm {
	RoadSurfaceForm.Surface[] GetSurfaceMenuStructure()
	{
		return new RoadSurfaceForm.Surface[]{
				new RoadSurfaceForm.Surface("asphalt", R.drawable.surface_asphalt, R.string.quest_surface_value_asphalt),
				new RoadSurfaceForm.Surface("concrete", R.drawable.surface_concrete, R.string.quest_surface_value_concrete),
				new RoadSurfaceForm.Surface("sett", R.drawable.surface_sett, R.string.quest_surface_value_sett),
				new RoadSurfaceForm.Surface("paving_stones", R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones),
				new RoadSurfaceForm.Surface("cobblestone", R.drawable.surface_cobblestone, R.string.quest_surface_value_cobblestone),
				new RoadSurfaceForm.Surface("wood", R.drawable.surface_wood, R.string.quest_surface_value_wood),
		};
	}
}
