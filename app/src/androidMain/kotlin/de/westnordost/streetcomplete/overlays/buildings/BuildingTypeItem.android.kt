package de.westnordost.streetcomplete.overlays.buildings

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingType.*

// must remain until map has been migrated to compose
val BuildingType.iconResId: Int get() = when (this) {
    UNSUPPORTED ->     R.drawable.building_other
    HOUSE ->           R.drawable.building_house
    APARTMENTS ->      R.drawable.building_apartments
    DETACHED ->        R.drawable.building_detached
    SEMI_DETACHED ->   R.drawable.building_semi_detached
    TERRACE ->         R.drawable.building_terrace
    HOTEL ->           R.drawable.building_hotel
    DORMITORY ->       R.drawable.building_dormitory
    HOUSEBOAT ->       R.drawable.building_houseboat
    BUNGALOW ->        R.drawable.building_bungalow
    STATIC_CARAVAN ->  R.drawable.building_static_caravan
    HUT ->             R.drawable.building_hut
    INDUSTRIAL ->      R.drawable.building_industrial
    RETAIL ->          R.drawable.building_retail
    OFFICE ->          R.drawable.building_office
    WAREHOUSE ->       R.drawable.building_warehouse
    KIOSK ->           R.drawable.building_kiosk
    STORAGE_TANK ->    R.drawable.building_storage_tank
    KINDERGARTEN ->    R.drawable.building_kindergarten
    SCHOOL ->          R.drawable.building_school
    COLLEGE ->         R.drawable.building_college
    SPORTS_CENTRE ->   R.drawable.sport_volleyball
    HOSPITAL ->        R.drawable.building_hospital
    STADIUM ->         R.drawable.sport_volleyball
    GRANDSTAND ->      R.drawable.sport_volleyball
    TRAIN_STATION ->   R.drawable.building_train_station
    TRANSPORTATION ->  R.drawable.building_transportation
    FIRE_STATION ->    R.drawable.building_fire_truck
    UNIVERSITY ->      R.drawable.building_university
    GOVERNMENT ->      R.drawable.building_historic
    CHURCH ->          R.drawable.religion_christian
    CHAPEL ->          R.drawable.religion_christian
    CATHEDRAL ->       R.drawable.religion_christian
    MOSQUE ->          R.drawable.religion_muslim
    TEMPLE ->          R.drawable.building_temple
    PAGODA ->          R.drawable.building_temple
    SYNAGOGUE ->       R.drawable.religion_jewish
    SHRINE ->          R.drawable.building_temple
    CARPORT ->         R.drawable.building_carport
    GARAGE ->          R.drawable.building_garage
    GARAGES ->         R.drawable.building_garages
    PARKING ->         R.drawable.building_parking
    FARM ->            R.drawable.building_farm_house
    FARM_AUXILIARY ->  R.drawable.building_barn
    SILO ->            R.drawable.building_silo
    GREENHOUSE ->      R.drawable.building_greenhouse
    SHED ->            R.drawable.building_shed
    ALLOTMENT_HOUSE -> R.drawable.building_allotment_house
    ROOF ->            R.drawable.building_roof
    BRIDGE ->          R.drawable.building_bridge
    TOILETS ->         R.drawable.building_toilets
    SERVICE ->         R.drawable.building_service
    HANGAR ->          R.drawable.building_hangar
    TOWER ->           R.drawable.building_tower
    BUNKER ->          R.drawable.building_bunker
    BOATHOUSE ->       R.drawable.building_boathouse
    OUTBUILDING ->     R.drawable.building_shed
    CONTAINER ->       R.drawable.building_container
    TENT ->            R.drawable.building_tent
    TOMB ->            R.drawable.building_tomb
    HISTORIC ->        R.drawable.building_historic
    ABANDONED ->       R.drawable.building_abandoned
    RUINS ->           R.drawable.building_ruins
    RESIDENTIAL ->     R.drawable.building_apartments
    COMMERCIAL ->      R.drawable.building_office
    CIVIC ->           R.drawable.building_civic
    RELIGIOUS ->       R.drawable.building_temple
    GUARDHOUSE ->      R.drawable.building_guardhouse
    CONSTRUCTION ->    R.drawable.building_construction
}
