package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.building_abandoned
import de.westnordost.streetcomplete.resources.building_allotment_house
import de.westnordost.streetcomplete.resources.building_apartments
import de.westnordost.streetcomplete.resources.building_barn
import de.westnordost.streetcomplete.resources.building_boathouse
import de.westnordost.streetcomplete.resources.building_bridge
import de.westnordost.streetcomplete.resources.building_bungalow
import de.westnordost.streetcomplete.resources.building_bunker
import de.westnordost.streetcomplete.resources.building_carport
import de.westnordost.streetcomplete.resources.building_civic
import de.westnordost.streetcomplete.resources.building_college
import de.westnordost.streetcomplete.resources.building_construction
import de.westnordost.streetcomplete.resources.building_container
import de.westnordost.streetcomplete.resources.building_detached
import de.westnordost.streetcomplete.resources.building_dormitory
import de.westnordost.streetcomplete.resources.building_farm_house
import de.westnordost.streetcomplete.resources.building_fire_truck
import de.westnordost.streetcomplete.resources.building_garage
import de.westnordost.streetcomplete.resources.building_garages
import de.westnordost.streetcomplete.resources.building_greenhouse
import de.westnordost.streetcomplete.resources.building_guardhouse
import de.westnordost.streetcomplete.resources.building_hangar
import de.westnordost.streetcomplete.resources.building_historic
import de.westnordost.streetcomplete.resources.building_hospital
import de.westnordost.streetcomplete.resources.building_hotel
import de.westnordost.streetcomplete.resources.building_house
import de.westnordost.streetcomplete.resources.building_houseboat
import de.westnordost.streetcomplete.resources.building_hut
import de.westnordost.streetcomplete.resources.building_industrial
import de.westnordost.streetcomplete.resources.building_kindergarten
import de.westnordost.streetcomplete.resources.building_kiosk
import de.westnordost.streetcomplete.resources.building_office
import de.westnordost.streetcomplete.resources.building_other
import de.westnordost.streetcomplete.resources.building_parking
import de.westnordost.streetcomplete.resources.building_retail
import de.westnordost.streetcomplete.resources.building_roof
import de.westnordost.streetcomplete.resources.building_ruins
import de.westnordost.streetcomplete.resources.building_school
import de.westnordost.streetcomplete.resources.building_semi_detached
import de.westnordost.streetcomplete.resources.building_service
import de.westnordost.streetcomplete.resources.building_shed
import de.westnordost.streetcomplete.resources.building_silo
import de.westnordost.streetcomplete.resources.building_static_caravan
import de.westnordost.streetcomplete.resources.building_storage_tank
import de.westnordost.streetcomplete.resources.building_temple
import de.westnordost.streetcomplete.resources.building_tent
import de.westnordost.streetcomplete.resources.building_terrace
import de.westnordost.streetcomplete.resources.building_toilets
import de.westnordost.streetcomplete.resources.building_tomb
import de.westnordost.streetcomplete.resources.building_tower
import de.westnordost.streetcomplete.resources.building_train_station
import de.westnordost.streetcomplete.resources.building_transportation
import de.westnordost.streetcomplete.resources.building_university
import de.westnordost.streetcomplete.resources.building_warehouse
import de.westnordost.streetcomplete.resources.quest_buildingType_abandoned
import de.westnordost.streetcomplete.resources.quest_buildingType_abandoned_description
import de.westnordost.streetcomplete.resources.quest_buildingType_allotment_house
import de.westnordost.streetcomplete.resources.quest_buildingType_apartments
import de.westnordost.streetcomplete.resources.quest_buildingType_apartments_description
import de.westnordost.streetcomplete.resources.quest_buildingType_boathouse
import de.westnordost.streetcomplete.resources.quest_buildingType_bridge
import de.westnordost.streetcomplete.resources.quest_buildingType_bungalow
import de.westnordost.streetcomplete.resources.quest_buildingType_bungalow_description2
import de.westnordost.streetcomplete.resources.quest_buildingType_bunker
import de.westnordost.streetcomplete.resources.quest_buildingType_carport
import de.westnordost.streetcomplete.resources.quest_buildingType_carport_description
import de.westnordost.streetcomplete.resources.quest_buildingType_cathedral
import de.westnordost.streetcomplete.resources.quest_buildingType_chapel
import de.westnordost.streetcomplete.resources.quest_buildingType_church
import de.westnordost.streetcomplete.resources.quest_buildingType_civic
import de.westnordost.streetcomplete.resources.quest_buildingType_civic_description
import de.westnordost.streetcomplete.resources.quest_buildingType_college
import de.westnordost.streetcomplete.resources.quest_buildingType_commercial
import de.westnordost.streetcomplete.resources.quest_buildingType_commercial_generic_description
import de.westnordost.streetcomplete.resources.quest_buildingType_container
import de.westnordost.streetcomplete.resources.quest_buildingType_detached
import de.westnordost.streetcomplete.resources.quest_buildingType_detached_description
import de.westnordost.streetcomplete.resources.quest_buildingType_dormitory
import de.westnordost.streetcomplete.resources.quest_buildingType_farm_auxiliary
import de.westnordost.streetcomplete.resources.quest_buildingType_farm_auxiliary_description
import de.westnordost.streetcomplete.resources.quest_buildingType_farmhouse
import de.westnordost.streetcomplete.resources.quest_buildingType_farmhouse_description
import de.westnordost.streetcomplete.resources.quest_buildingType_fire_station
import de.westnordost.streetcomplete.resources.quest_buildingType_garage
import de.westnordost.streetcomplete.resources.quest_buildingType_garages
import de.westnordost.streetcomplete.resources.quest_buildingType_garages_description
import de.westnordost.streetcomplete.resources.quest_buildingType_government
import de.westnordost.streetcomplete.resources.quest_buildingType_grandstand
import de.westnordost.streetcomplete.resources.quest_buildingType_greenhouse
import de.westnordost.streetcomplete.resources.quest_buildingType_guardhouse
import de.westnordost.streetcomplete.resources.quest_buildingType_hangar
import de.westnordost.streetcomplete.resources.quest_buildingType_hangar_description
import de.westnordost.streetcomplete.resources.quest_buildingType_historic
import de.westnordost.streetcomplete.resources.quest_buildingType_historic_description
import de.westnordost.streetcomplete.resources.quest_buildingType_hospital
import de.westnordost.streetcomplete.resources.quest_buildingType_hotel
import de.westnordost.streetcomplete.resources.quest_buildingType_house
import de.westnordost.streetcomplete.resources.quest_buildingType_house_description2
import de.westnordost.streetcomplete.resources.quest_buildingType_houseboat
import de.westnordost.streetcomplete.resources.quest_buildingType_hut
import de.westnordost.streetcomplete.resources.quest_buildingType_hut_description
import de.westnordost.streetcomplete.resources.quest_buildingType_industrial
import de.westnordost.streetcomplete.resources.quest_buildingType_industrial_description
import de.westnordost.streetcomplete.resources.quest_buildingType_kindergarten
import de.westnordost.streetcomplete.resources.quest_buildingType_kiosk
import de.westnordost.streetcomplete.resources.quest_buildingType_mosque
import de.westnordost.streetcomplete.resources.quest_buildingType_office
import de.westnordost.streetcomplete.resources.quest_buildingType_other
import de.westnordost.streetcomplete.resources.quest_buildingType_other_description
import de.westnordost.streetcomplete.resources.quest_buildingType_outbuilding
import de.westnordost.streetcomplete.resources.quest_buildingType_outbuilding_description
import de.westnordost.streetcomplete.resources.quest_buildingType_pagoda
import de.westnordost.streetcomplete.resources.quest_buildingType_parking
import de.westnordost.streetcomplete.resources.quest_buildingType_religious
import de.westnordost.streetcomplete.resources.quest_buildingType_residential
import de.westnordost.streetcomplete.resources.quest_buildingType_residential_description
import de.westnordost.streetcomplete.resources.quest_buildingType_retail
import de.westnordost.streetcomplete.resources.quest_buildingType_retail_description
import de.westnordost.streetcomplete.resources.quest_buildingType_roof
import de.westnordost.streetcomplete.resources.quest_buildingType_ruins
import de.westnordost.streetcomplete.resources.quest_buildingType_ruins_description
import de.westnordost.streetcomplete.resources.quest_buildingType_school
import de.westnordost.streetcomplete.resources.quest_buildingType_semi_detached
import de.westnordost.streetcomplete.resources.quest_buildingType_semi_detached_description2
import de.westnordost.streetcomplete.resources.quest_buildingType_service
import de.westnordost.streetcomplete.resources.quest_buildingType_service_description
import de.westnordost.streetcomplete.resources.quest_buildingType_shed
import de.westnordost.streetcomplete.resources.quest_buildingType_shrine
import de.westnordost.streetcomplete.resources.quest_buildingType_silo
import de.westnordost.streetcomplete.resources.quest_buildingType_sports_centre
import de.westnordost.streetcomplete.resources.quest_buildingType_stadium
import de.westnordost.streetcomplete.resources.quest_buildingType_static_caravan
import de.westnordost.streetcomplete.resources.quest_buildingType_storage_tank
import de.westnordost.streetcomplete.resources.quest_buildingType_synagogue
import de.westnordost.streetcomplete.resources.quest_buildingType_temple
import de.westnordost.streetcomplete.resources.quest_buildingType_tent
import de.westnordost.streetcomplete.resources.quest_buildingType_terrace2
import de.westnordost.streetcomplete.resources.quest_buildingType_terrace_description
import de.westnordost.streetcomplete.resources.quest_buildingType_toilets
import de.westnordost.streetcomplete.resources.quest_buildingType_tomb
import de.westnordost.streetcomplete.resources.quest_buildingType_tower
import de.westnordost.streetcomplete.resources.quest_buildingType_tower_description
import de.westnordost.streetcomplete.resources.quest_buildingType_train_station
import de.westnordost.streetcomplete.resources.quest_buildingType_transportation
import de.westnordost.streetcomplete.resources.quest_buildingType_under_construction
import de.westnordost.streetcomplete.resources.quest_buildingType_university
import de.westnordost.streetcomplete.resources.quest_buildingType_warehouse
import de.westnordost.streetcomplete.resources.religion_christian
import de.westnordost.streetcomplete.resources.religion_jewish
import de.westnordost.streetcomplete.resources.religion_muslim
import de.westnordost.streetcomplete.resources.sport_volleyball
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BuildingType.title: StringResource get() = when (this) {
    UNSUPPORTED ->     Res.string.quest_buildingType_other
    HOUSE ->           Res.string.quest_buildingType_house
    APARTMENTS ->      Res.string.quest_buildingType_apartments
    DETACHED ->        Res.string.quest_buildingType_detached
    SEMI_DETACHED ->   Res.string.quest_buildingType_semi_detached
    TERRACE ->         Res.string.quest_buildingType_terrace2
    HOTEL ->           Res.string.quest_buildingType_hotel
    DORMITORY ->       Res.string.quest_buildingType_dormitory
    HOUSEBOAT ->       Res.string.quest_buildingType_houseboat
    BUNGALOW ->        Res.string.quest_buildingType_bungalow
    STATIC_CARAVAN ->  Res.string.quest_buildingType_static_caravan
    HUT ->             Res.string.quest_buildingType_hut
    INDUSTRIAL ->      Res.string.quest_buildingType_industrial
    RETAIL ->          Res.string.quest_buildingType_retail
    OFFICE ->          Res.string.quest_buildingType_office
    WAREHOUSE ->       Res.string.quest_buildingType_warehouse
    KIOSK ->           Res.string.quest_buildingType_kiosk
    STORAGE_TANK ->    Res.string.quest_buildingType_storage_tank
    KINDERGARTEN ->    Res.string.quest_buildingType_kindergarten
    SCHOOL ->          Res.string.quest_buildingType_school
    COLLEGE ->         Res.string.quest_buildingType_college
    SPORTS_CENTRE ->   Res.string.quest_buildingType_sports_centre
    HOSPITAL ->        Res.string.quest_buildingType_hospital
    STADIUM ->         Res.string.quest_buildingType_stadium
    GRANDSTAND ->      Res.string.quest_buildingType_grandstand
    TRAIN_STATION ->   Res.string.quest_buildingType_train_station
    TRANSPORTATION ->  Res.string.quest_buildingType_transportation
    FIRE_STATION ->    Res.string.quest_buildingType_fire_station
    UNIVERSITY ->      Res.string.quest_buildingType_university
    GOVERNMENT ->      Res.string.quest_buildingType_government
    CHURCH ->          Res.string.quest_buildingType_church
    CHAPEL ->          Res.string.quest_buildingType_chapel
    CATHEDRAL ->       Res.string.quest_buildingType_cathedral
    MOSQUE ->          Res.string.quest_buildingType_mosque
    TEMPLE ->          Res.string.quest_buildingType_temple
    PAGODA ->          Res.string.quest_buildingType_pagoda
    SYNAGOGUE ->       Res.string.quest_buildingType_synagogue
    SHRINE ->          Res.string.quest_buildingType_shrine
    CARPORT ->         Res.string.quest_buildingType_carport
    GARAGE ->          Res.string.quest_buildingType_garage
    GARAGES ->         Res.string.quest_buildingType_garages
    PARKING ->         Res.string.quest_buildingType_parking
    FARM ->            Res.string.quest_buildingType_farmhouse
    FARM_AUXILIARY ->  Res.string.quest_buildingType_farm_auxiliary
    SILO ->            Res.string.quest_buildingType_silo
    GREENHOUSE ->      Res.string.quest_buildingType_greenhouse
    SHED ->            Res.string.quest_buildingType_shed
    ALLOTMENT_HOUSE -> Res.string.quest_buildingType_allotment_house
    ROOF ->            Res.string.quest_buildingType_roof
    BRIDGE ->          Res.string.quest_buildingType_bridge
    TOILETS ->         Res.string.quest_buildingType_toilets
    SERVICE ->         Res.string.quest_buildingType_service
    HANGAR ->          Res.string.quest_buildingType_hangar
    TOWER ->           Res.string.quest_buildingType_tower
    BUNKER ->          Res.string.quest_buildingType_bunker
    BOATHOUSE ->       Res.string.quest_buildingType_boathouse
    CONTAINER ->       Res.string.quest_buildingType_container
    OUTBUILDING ->     Res.string.quest_buildingType_outbuilding
    TENT ->            Res.string.quest_buildingType_tent
    TOMB ->            Res.string.quest_buildingType_tomb
    HISTORIC ->        Res.string.quest_buildingType_historic
    ABANDONED ->       Res.string.quest_buildingType_abandoned
    RUINS ->           Res.string.quest_buildingType_ruins
    RESIDENTIAL ->     Res.string.quest_buildingType_residential
    COMMERCIAL ->      Res.string.quest_buildingType_commercial
    CIVIC ->           Res.string.quest_buildingType_civic
    RELIGIOUS ->       Res.string.quest_buildingType_religious
    GUARDHOUSE ->      Res.string.quest_buildingType_guardhouse
    CONSTRUCTION ->    Res.string.quest_buildingType_under_construction
}

val BuildingType.description: StringResource? get() = when (this) {
    UNSUPPORTED ->     Res.string.quest_buildingType_other_description
    HOUSE ->           Res.string.quest_buildingType_house_description2
    APARTMENTS ->      Res.string.quest_buildingType_apartments_description
    DETACHED ->        Res.string.quest_buildingType_detached_description
    SEMI_DETACHED ->   Res.string.quest_buildingType_semi_detached_description2
    TERRACE ->         Res.string.quest_buildingType_terrace_description
    BUNGALOW ->        Res.string.quest_buildingType_bungalow_description2
    OUTBUILDING ->     Res.string.quest_buildingType_outbuilding_description
    HUT ->             Res.string.quest_buildingType_hut_description
    INDUSTRIAL ->      Res.string.quest_buildingType_industrial_description
    RETAIL ->          Res.string.quest_buildingType_retail_description
    CARPORT ->         Res.string.quest_buildingType_carport_description
    GARAGES ->         Res.string.quest_buildingType_garages_description
    FARM ->            Res.string.quest_buildingType_farmhouse_description
    FARM_AUXILIARY ->  Res.string.quest_buildingType_farm_auxiliary_description
    SERVICE ->         Res.string.quest_buildingType_service_description
    HANGAR ->          Res.string.quest_buildingType_hangar_description
    TOWER ->           Res.string.quest_buildingType_tower_description
    HISTORIC ->        Res.string.quest_buildingType_historic_description
    ABANDONED ->       Res.string.quest_buildingType_abandoned_description
    RUINS ->           Res.string.quest_buildingType_ruins_description
    RESIDENTIAL ->     Res.string.quest_buildingType_residential_description
    COMMERCIAL ->      Res.string.quest_buildingType_commercial_generic_description
    CIVIC ->           Res.string.quest_buildingType_civic_description
    else ->            null
}

val BuildingType.icon: DrawableResource get() = when (this) {
    UNSUPPORTED ->     Res.drawable.building_other
    HOUSE ->           Res.drawable.building_house
    APARTMENTS ->      Res.drawable.building_apartments
    DETACHED ->        Res.drawable.building_detached
    SEMI_DETACHED ->   Res.drawable.building_semi_detached
    TERRACE ->         Res.drawable.building_terrace
    HOTEL ->           Res.drawable.building_hotel
    DORMITORY ->       Res.drawable.building_dormitory
    HOUSEBOAT ->       Res.drawable.building_houseboat
    BUNGALOW ->        Res.drawable.building_bungalow
    STATIC_CARAVAN ->  Res.drawable.building_static_caravan
    HUT ->             Res.drawable.building_hut
    INDUSTRIAL ->      Res.drawable.building_industrial
    RETAIL ->          Res.drawable.building_retail
    OFFICE ->          Res.drawable.building_office
    WAREHOUSE ->       Res.drawable.building_warehouse
    KIOSK ->           Res.drawable.building_kiosk
    STORAGE_TANK ->    Res.drawable.building_storage_tank
    KINDERGARTEN ->    Res.drawable.building_kindergarten
    SCHOOL ->          Res.drawable.building_school
    COLLEGE ->         Res.drawable.building_college
    SPORTS_CENTRE ->   Res.drawable.sport_volleyball
    HOSPITAL ->        Res.drawable.building_hospital
    STADIUM ->         Res.drawable.sport_volleyball
    GRANDSTAND ->      Res.drawable.sport_volleyball
    TRAIN_STATION ->   Res.drawable.building_train_station
    TRANSPORTATION ->  Res.drawable.building_transportation
    FIRE_STATION ->    Res.drawable.building_fire_truck
    UNIVERSITY ->      Res.drawable.building_university
    GOVERNMENT ->      Res.drawable.building_historic
    CHURCH ->          Res.drawable.religion_christian
    CHAPEL ->          Res.drawable.religion_christian
    CATHEDRAL ->       Res.drawable.religion_christian
    MOSQUE ->          Res.drawable.religion_muslim
    TEMPLE ->          Res.drawable.building_temple
    PAGODA ->          Res.drawable.building_temple
    SYNAGOGUE ->       Res.drawable.religion_jewish
    SHRINE ->          Res.drawable.building_temple
    CARPORT ->         Res.drawable.building_carport
    GARAGE ->          Res.drawable.building_garage
    GARAGES ->         Res.drawable.building_garages
    PARKING ->         Res.drawable.building_parking
    FARM ->            Res.drawable.building_farm_house
    FARM_AUXILIARY ->  Res.drawable.building_barn
    SILO ->            Res.drawable.building_silo
    GREENHOUSE ->      Res.drawable.building_greenhouse
    SHED ->            Res.drawable.building_shed
    ALLOTMENT_HOUSE -> Res.drawable.building_allotment_house
    ROOF ->            Res.drawable.building_roof
    BRIDGE ->          Res.drawable.building_bridge
    TOILETS ->         Res.drawable.building_toilets
    SERVICE ->         Res.drawable.building_service
    HANGAR ->          Res.drawable.building_hangar
    TOWER ->           Res.drawable.building_tower
    BUNKER ->          Res.drawable.building_bunker
    BOATHOUSE ->       Res.drawable.building_boathouse
    OUTBUILDING ->     Res.drawable.building_shed
    CONTAINER ->       Res.drawable.building_container
    TENT ->            Res.drawable.building_tent
    TOMB ->            Res.drawable.building_tomb
    HISTORIC ->        Res.drawable.building_historic
    ABANDONED ->       Res.drawable.building_abandoned
    RUINS ->           Res.drawable.building_ruins
    RESIDENTIAL ->     Res.drawable.building_apartments
    COMMERCIAL ->      Res.drawable.building_office
    CIVIC ->           Res.drawable.building_civic
    RELIGIOUS ->       Res.drawable.building_temple
    GUARDHOUSE ->      Res.drawable.building_guardhouse
    CONSTRUCTION ->    Res.drawable.building_construction
}
