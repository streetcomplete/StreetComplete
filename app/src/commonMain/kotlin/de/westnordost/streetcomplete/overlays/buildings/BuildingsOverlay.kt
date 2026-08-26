package de.westnordost.streetcomplete.overlays.buildings

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingType.ABANDONED
import de.westnordost.streetcomplete.osm.building.BuildingType.ALLOTMENT_HOUSE
import de.westnordost.streetcomplete.osm.building.BuildingType.APARTMENTS
import de.westnordost.streetcomplete.osm.building.BuildingType.BOATHOUSE
import de.westnordost.streetcomplete.osm.building.BuildingType.BRIDGE
import de.westnordost.streetcomplete.osm.building.BuildingType.BUNGALOW
import de.westnordost.streetcomplete.osm.building.BuildingType.BUNKER
import de.westnordost.streetcomplete.osm.building.BuildingType.CARPORT
import de.westnordost.streetcomplete.osm.building.BuildingType.CATHEDRAL
import de.westnordost.streetcomplete.osm.building.BuildingType.CHAPEL
import de.westnordost.streetcomplete.osm.building.BuildingType.CHURCH
import de.westnordost.streetcomplete.osm.building.BuildingType.CIVIC
import de.westnordost.streetcomplete.osm.building.BuildingType.COLLEGE
import de.westnordost.streetcomplete.osm.building.BuildingType.COMMERCIAL
import de.westnordost.streetcomplete.osm.building.BuildingType.CONSTRUCTION
import de.westnordost.streetcomplete.osm.building.BuildingType.CONTAINER
import de.westnordost.streetcomplete.osm.building.BuildingType.DETACHED
import de.westnordost.streetcomplete.osm.building.BuildingType.DORMITORY
import de.westnordost.streetcomplete.osm.building.BuildingType.FARM
import de.westnordost.streetcomplete.osm.building.BuildingType.FARM_AUXILIARY
import de.westnordost.streetcomplete.osm.building.BuildingType.FIRE_STATION
import de.westnordost.streetcomplete.osm.building.BuildingType.GARAGE
import de.westnordost.streetcomplete.osm.building.BuildingType.GARAGES
import de.westnordost.streetcomplete.osm.building.BuildingType.GOVERNMENT
import de.westnordost.streetcomplete.osm.building.BuildingType.GRANDSTAND
import de.westnordost.streetcomplete.osm.building.BuildingType.GREENHOUSE
import de.westnordost.streetcomplete.osm.building.BuildingType.GUARDHOUSE
import de.westnordost.streetcomplete.osm.building.BuildingType.HANGAR
import de.westnordost.streetcomplete.osm.building.BuildingType.HISTORIC
import de.westnordost.streetcomplete.osm.building.BuildingType.HOSPITAL
import de.westnordost.streetcomplete.osm.building.BuildingType.HOTEL
import de.westnordost.streetcomplete.osm.building.BuildingType.HOUSE
import de.westnordost.streetcomplete.osm.building.BuildingType.HOUSEBOAT
import de.westnordost.streetcomplete.osm.building.BuildingType.HUT
import de.westnordost.streetcomplete.osm.building.BuildingType.INDUSTRIAL
import de.westnordost.streetcomplete.osm.building.BuildingType.KINDERGARTEN
import de.westnordost.streetcomplete.osm.building.BuildingType.KIOSK
import de.westnordost.streetcomplete.osm.building.BuildingType.MOSQUE
import de.westnordost.streetcomplete.osm.building.BuildingType.OFFICE
import de.westnordost.streetcomplete.osm.building.BuildingType.OUTBUILDING
import de.westnordost.streetcomplete.osm.building.BuildingType.PAGODA
import de.westnordost.streetcomplete.osm.building.BuildingType.PARKING
import de.westnordost.streetcomplete.osm.building.BuildingType.RELIGIOUS
import de.westnordost.streetcomplete.osm.building.BuildingType.RESIDENTIAL
import de.westnordost.streetcomplete.osm.building.BuildingType.RETAIL
import de.westnordost.streetcomplete.osm.building.BuildingType.ROOF
import de.westnordost.streetcomplete.osm.building.BuildingType.RUINS
import de.westnordost.streetcomplete.osm.building.BuildingType.SCHOOL
import de.westnordost.streetcomplete.osm.building.BuildingType.SEMI_DETACHED
import de.westnordost.streetcomplete.osm.building.BuildingType.SERVICE
import de.westnordost.streetcomplete.osm.building.BuildingType.SHED
import de.westnordost.streetcomplete.osm.building.BuildingType.SHRINE
import de.westnordost.streetcomplete.osm.building.BuildingType.SILO
import de.westnordost.streetcomplete.osm.building.BuildingType.SPORTS_CENTRE
import de.westnordost.streetcomplete.osm.building.BuildingType.STADIUM
import de.westnordost.streetcomplete.osm.building.BuildingType.STATIC_CARAVAN
import de.westnordost.streetcomplete.osm.building.BuildingType.STORAGE_TANK
import de.westnordost.streetcomplete.osm.building.BuildingType.SYNAGOGUE
import de.westnordost.streetcomplete.osm.building.BuildingType.TEMPLE
import de.westnordost.streetcomplete.osm.building.BuildingType.TENT
import de.westnordost.streetcomplete.osm.building.BuildingType.TERRACE
import de.westnordost.streetcomplete.osm.building.BuildingType.TOILETS
import de.westnordost.streetcomplete.osm.building.BuildingType.TOMB
import de.westnordost.streetcomplete.osm.building.BuildingType.TOWER
import de.westnordost.streetcomplete.osm.building.BuildingType.TRAIN_STATION
import de.westnordost.streetcomplete.osm.building.BuildingType.TRANSPORTATION
import de.westnordost.streetcomplete.osm.building.BuildingType.UNIVERSITY
import de.westnordost.streetcomplete.osm.building.BuildingType.UNSUPPORTED
import de.westnordost.streetcomplete.osm.building.BuildingType.WAREHOUSE
import de.westnordost.streetcomplete.osm.building.OTHER_KEYS_POTENTIALLY_DESCRIBING_BUILDING_TYPE
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.createBuildingUseType
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.overlay_buildings
import de.westnordost.streetcomplete.resources.quest_building

class BuildingsOverlay : Overlay {

    override val title = Res.string.overlay_buildings
    override val icon = Res.drawable.quest_building
    override val changesetComment = "Survey buildings"
    override val wikiLink = "Key:building"
    override val achievements = listOf(BUILDING)
    override val hidesQuestTypes = setOf(AddBuildingType::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry) = mapData.filter(
        """
            ways, relations with
              (
                building and building !~ no|entrance
                or historic ~ monument|ship|wreck
                or man_made ~ ${listOf(
                  "antenna",
                  "chimney",
                  "cooling_tower",
                  "communications_tower",
                  "gasometer",
                  "lighthouse",
                  "obelisk",
                  "silo",
                  "storage_tank",
                  "stupa",
                  "telescope",
                  "tower",
                  "watermill",
                  "water_tower",
                  "windmill",
                ).joinToString("|")}
              )
        """)
        .map { element ->
            val building = createBuildingType(element.tags)
            val buildingUse = createBuildingUseType(element.tags)

            val color = building?.color
                ?: if (isBuildingTypeMissing(element.tags)) OverlayColor.Red else OverlayColor.Invisible

            val outline = buildingUse?.color
            // val height = estimateBuildingHeight(element.tags)
            // val minHeight = if (height != null) estimateMinBuildingHeight(element.tags) else null

            element to OverlayStyle.Polygon(
                color = outline ?: color,
                outline = if (buildingUse!=null) color else null,
                icon = building?.icon,
                // TODO MapLibre: 3D buildings are disabled until
                //      https://github.com/maplibre/maplibre-native/issues/2746 is fixed
                // height = height,
                // minHeight = minHeight
            )
        }

    @Composable
    override fun Form(
        on: (OverlayAction) -> Unit,
        element: Element?,
        geometry: ElementGeometry,
        countryInfo: CountryInfo,
    ) {
        requireNotNull(element)
        BuildingsOverlayForm(on, element)
    }
    private fun isBuildingTypeMissing(tags: Map<String, String>): Boolean =
        !OTHER_KEYS_POTENTIALLY_DESCRIBING_BUILDING_TYPE.any { it in tags }
}
val BuildingType.color get() = when (this) {
    // ~detached homes
    DETACHED, SEMI_DETACHED, HOUSEBOAT, BUNGALOW, STATIC_CARAVAN, HUT, FARM, -> // 10%
        OverlayColor.Blue

    // ~non-detached homes
    HOUSE, DORMITORY, APARTMENTS, TERRACE, -> // 52%
        OverlayColor.Sky

    // unspecified residential
    RESIDENTIAL, -> // 12%
        OverlayColor.Cyan

    // parking, sheds, outbuildings in general...
    OUTBUILDING, CARPORT, GARAGE, GARAGES, SHED, BOATHOUSE, SERVICE, ALLOTMENT_HOUSE,
    TENT, CONTAINER, GUARDHOUSE, -> // 11%
        OverlayColor.Lime

    // commercial, industrial, farm buildings
    COMMERCIAL, KIOSK, RETAIL, OFFICE, BRIDGE, HOTEL, PARKING,
    INDUSTRIAL, WAREHOUSE, HANGAR, STORAGE_TANK,
    FARM_AUXILIARY, SILO, GREENHOUSE,
    ROOF -> // 5%
        OverlayColor.Gold

    // amenity buildings
    TRAIN_STATION, TRANSPORTATION,
    CIVIC, GOVERNMENT, FIRE_STATION, HOSPITAL,
    KINDERGARTEN, SCHOOL, COLLEGE, UNIVERSITY, SPORTS_CENTRE, STADIUM, GRANDSTAND,
    RELIGIOUS, CHURCH, CHAPEL, CATHEDRAL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE,
    TOILETS, -> // 2%
        OverlayColor.Orange

    // other/special
    HISTORIC, ABANDONED, RUINS, CONSTRUCTION, BUNKER, TOMB, TOWER,
    UNSUPPORTED ->
        OverlayColor.Purple
}
