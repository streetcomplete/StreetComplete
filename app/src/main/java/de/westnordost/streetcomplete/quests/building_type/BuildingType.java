package de.westnordost.streetcomplete.quests.building_type;

import android.support.annotation.Nullable;

import java.lang.reflect.Field;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.Item;

public class BuildingType
{
	public static final Item
		RESIDENTIAL =   new Item("residential",R.drawable.ic_building_apartments, R.string.quest_buildingType_residential, R.string.quest_buildingType_residential_description),
		HOUSE =         new Item("house",      R.drawable.ic_building_house,      R.string.quest_buildingType_house, R.string.quest_buildingType_house_description),
		APARTMENTS =    new Item("apartments", R.drawable.ic_building_apartments, R.string.quest_buildingType_apartments, R.string.quest_buildingType_apartments_description),
		DETACHED =      new Item("detached",   R.drawable.ic_building_detached,   R.string.quest_buildingType_detached, R.string.quest_buildingType_detached_description),
		SEMI_DETACHED = new Item("semidetached_house", R.drawable.ic_building_semi_detached, R.string.quest_buildingType_semi_detached, R.string.quest_buildingType_semi_detached_description),
		TERRACE =       new Item("terrace",    R.drawable.ic_building_terrace,    R.string.quest_buildingType_terrace, R.string.quest_buildingType_terrace_description),
		HOTEL =         new Item("hotel",      R.drawable.ic_building_hotel,      R.string.quest_buildingType_hotel),
		DORMITORY =     new Item("dormitory",  R.drawable.ic_building_dormitory,  R.string.quest_buildingType_dormitory),
		HOUSEBOAT =     new Item("houseboat",  R.drawable.ic_building_houseboat,  R.string.quest_buildingType_houseboat),
		BUNGALOW =      new Item("bungalow",   R.drawable.ic_building_bungalow,   R.string.quest_buildingType_bungalow, R.string.quest_buildingType_bungalow_description),
		STATIC_CARAVAN =new Item("static_caravan", R.drawable.ic_building_static_caravan, R.string.quest_buildingType_static_caravan),
		HUT =           new Item("hut",        R.drawable.ic_building_hut,        R.string.quest_buildingType_hut, R.string.quest_buildingType_hut_description),

		COMMERCIAL =    new Item("commercial", R.drawable.ic_building_office,     R.string.quest_buildingType_commercial, R.string.quest_buildingType_commercial_generic_description),
		INDUSTRIAL =    new Item("industrial", R.drawable.ic_building_industrial, R.string.quest_buildingType_industrial, R.string.quest_buildingType_industrial_description),
		RETAIL =        new Item("retail",     R.drawable.ic_building_retail,     R.string.quest_buildingType_retail),
		OFFICE =        new Item("office",     R.drawable.ic_building_office,     R.string.quest_buildingType_office),
		WAREHOUSE =     new Item("warehouse",  R.drawable.ic_building_warehouse,  R.string.quest_buildingType_warehouse),
		KIOSK =         new Item("kiosk",      R.drawable.ic_building_kiosk,      R.string.quest_buildingType_kiosk),
		STORAGE_TANK =  new Item("man_made=storage_tank", R.drawable.ic_building_storage_tank, R.string.quest_buildingType_storage_tank),

		CIVIC =         new Item("civic",      R.drawable.ic_building_civic,      R.string.quest_buildingType_civic, R.string.quest_buildingType_civic_description),
		KINDERGARTEN =  new Item("kindergarten", R.drawable.ic_building_kindergarten, R.string.quest_buildingType_kindergarten),
		SCHOOL =        new Item("school",     R.drawable.ic_building_school,     R.string.quest_buildingType_school),
		COLLEGE =       new Item("college",    R.drawable.ic_building_college,    R.string.quest_buildingType_college),
		SPORTS_CENTRE = new Item("sports_centre", R.drawable.ic_sport_volleyball, R.string.quest_buildingType_sports_centre),
		HOSPITAL =      new Item("hospital",   R.drawable.ic_building_hospital,   R.string.quest_buildingType_hospital),
		STADIUM =       new Item("stadium",    R.drawable.ic_sport_volleyball,    R.string.quest_buildingType_stadium),
		TRAIN_STATION = new Item("train_station", R.drawable.ic_building_train_station, R.string.quest_buildingType_train_station),
		TRANSPORTATION =new Item("transportation", R.drawable.ic_building_transportation, R.string.quest_buildingType_transportation),
		UNIVERSITY =    new Item("university", R.drawable.ic_building_university, R.string.quest_buildingType_university),
		GOVERNMENT =    new Item("government", R.drawable.ic_building_civic,      R.string.quest_buildingType_government),

		RELIGIOUS =     new Item("religious",  R.drawable.ic_building_temple,     R.string.quest_buildingType_religious),
		CHURCH =        new Item("church",     R.drawable.ic_religion_christian,  R.string.quest_buildingType_church),
		CHAPEL =        new Item("chapel",     R.drawable.ic_religion_christian,  R.string.quest_buildingType_chapel),
		CATHEDRAL =     new Item("cathedral",  R.drawable.ic_religion_christian,  R.string.quest_buildingType_cathedral),
		MOSQUE =        new Item("mosque",     R.drawable.ic_religion_muslim,     R.string.quest_buildingType_mosque),
		TEMPLE =        new Item("temple",     R.drawable.ic_building_temple,     R.string.quest_buildingType_temple),
		PAGODA =        new Item("pagoda",     R.drawable.ic_building_temple,     R.string.quest_buildingType_pagoda),
		SYNAGOGUE =     new Item("synagogue",  R.drawable.ic_religion_jewish,     R.string.quest_buildingType_synagogue),
		SHRINE =        new Item("shrine",     R.drawable.ic_building_temple,     R.string.quest_buildingType_shrine),

		CARPORT =       new Item("carport",    R.drawable.ic_building_carport,    R.string.quest_buildingType_carport, R.string.quest_buildingType_carport_description),
		GARAGE =        new Item("garage",     R.drawable.ic_building_garage,     R.string.quest_buildingType_garage),
		GARAGES =       new Item("garages",    R.drawable.ic_building_garages,    R.string.quest_buildingType_garages),
		PARKING =       new Item("parking",    R.drawable.ic_building_parking,    R.string.quest_buildingType_parking),

		FARM =          new Item("farm",       R.drawable.ic_building_farm_house, R.string.quest_buildingType_farmhouse, R.string.quest_buildingType_farmhouse_description),
		FARM_AUXILIARY =new Item("farm_auxiliary", R.drawable.ic_building_barn,   R.string.quest_buildingType_farm_auxiliary, R.string.quest_buildingType_farm_auxiliary_description),
		GREENHOUSE =    new Item("greenhouse", R.drawable.ic_building_greenhouse, R.string.quest_buildingType_greenhouse),

		SHED =          new Item("shed",       R.drawable.ic_building_shed,       R.string.quest_buildingType_shed),
		ROOF =          new Item("roof",       R.drawable.ic_building_roof,       R.string.quest_buildingType_roof),
		TOILETS =       new Item("toilets",    R.drawable.ic_building_toilets,    R.string.quest_buildingType_toilets),
		SERVICE =       new Item("service",    R.drawable.ic_building_service,    R.string.quest_buildingType_service, R.string.quest_buildingType_service_description),
		HANGAR =        new Item("hangar",     R.drawable.ic_building_hangar,     R.string.quest_buildingType_hangar, R.string.quest_buildingType_hangar_description),
		BUNKER =        new Item("bunker",     R.drawable.ic_building_bunker,     R.string.quest_buildingType_bunker);


	@Nullable public static Item getByTag(String key, String value)
	{
		String tag = key.equals("building") ? value : key+"="+value;
		// synonyms
		if(tag.equals("semi")) tag = "semidetached_house";
		else if(tag.equals("public")) tag = "civic";
		Field[] declaredFields = BuildingType.class.getDeclaredFields();
		for (Field field : declaredFields)
		{
			Item item;
			try { item = (Item) field.get(null); } catch (Exception e) { continue; }
			if(item != null && tag.equals(item.value)) return item;
		}
		return null;
	}
}
