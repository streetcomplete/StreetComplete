package de.westnordost.streetcomplete.quests.building_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddBuildingTypeForm extends ImageListQuestAnswerFragment
{
	protected static final int MORE_THAN_95_PERCENT_COVERED = 8;

	private static final OsmItem[] BUILDING_TYPES = new OsmItem[]{
			new OsmItem("house",		R.drawable.building_house,		R.string.quest_buildingType_house),
			new OsmItem("residential",	R.drawable.building_residential,	R.string.quest_buildingType_residential),
			new OsmItem("garage",		R.drawable.building_garage,		R.string.quest_buildingType_garage),
			new OsmItem("apartments",	R.drawable.building_apartment,	R.string.quest_buildingType_apartments),

			new OsmItem("hut",			R.drawable.building_hut,			R.string.quest_buildingType_hut),
			new OsmItem("industrial",	R.drawable.building_industrial,  R.string.quest_buildingType_industrial),
			new OsmItem("detached",		R.drawable.building_house,		R.string.quest_buildingType_detached),
			new OsmItem("shed",			R.drawable.building_shed,		R.string.quest_buildingType_shed),

			new OsmItem("roof",			R.drawable.building_roof,		R.string.quest_buildingType_roof),
			new OsmItem("commercial",	R.drawable.building_commercial,	R.string.quest_buildingType_commercial),
			new OsmItem("garages",		R.drawable.building_garages,			R.string.quest_buildingType_garages),
			new OsmItem("school", 		R.drawable.building_school, 		R.string.quest_buildingType_school),

			new OsmItem("retail",		R.drawable.building_retail,		R.string.quest_buildingType_retail),
			//Necessary?
			new OsmItem("greenhouse",	R.drawable.building_greenhouse,	R.string.quest_buildingType_greenhouse),
			new OsmItem("barn",			R.drawable.building_barn,		R.string.quest_buildingType_barn),

			new OsmItem("church",		R.drawable.building_church,		R.string.quest_buildingType_church),
			new OsmItem("civic",		R.drawable.building_civic,		R.string.quest_buildingType_civic),
			new OsmItem("hotel",		R.drawable.building_hotel,		R.string.quest_buildingType_hotel),
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		imageSelector.setCellLayout(R.layout.cell_icon_select_with_label_below);

		return view;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return MORE_THAN_95_PERCENT_COVERED;
	}

	@Override protected OsmItem[] getItems()
	{
		return BUILDING_TYPES;
	}
}
