package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.overlays.places.PlacesOverlay
import de.westnordost.streetcomplete.overlays.street_parking.StreetParkingOverlay
import de.westnordost.streetcomplete.quests.amenity_cover.AddAmenityCover
import de.westnordost.streetcomplete.quests.amenity_indoor.AddIsAmenityIndoor
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.quests.crossing_markings.AddCrossingMarkings
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.quests.traffic_signals_vibrate.AddTrafficSignalsVibration
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransport
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToilets
import org.koin.core.qualifier.named
import org.koin.dsl.module

enum class EditTypeAchievement(val id: String) {
    RARE("rare"),
    CAR("car"),
    VEG("veg"),
    PEDESTRIAN("pedestrian"),
    BUILDING("building"),
    POSTMAN("postman"),
    BLIND("blind"),
    WHEELCHAIR("wheelchair"),
    BICYCLIST("bicyclist"),
    CITIZEN("citizen"),
    OUTDOORS("outdoors"),
    LIFESAVER("lifesaver"),
}

val achievementsModule = module {
    factory(named("Achievements")) { achievements }
    factory(named("Links")) { links }
    factory(named("TypeAliases")) { typeAliases }
    factory { UserAchievementsDao(get()) }
    factory { UserLinksDao(get()) }

    single<AchievementsSource> { get<AchievementsController>() }
    single { AchievementsController(get(), get(), get(), get(), get(named("Achievements")), get(named("Links"))) }
}

// list of (quest) synonyms (this alternate name is mentioned to aid searching for this code)
private val typeAliases = listOf(
    "AddAccessibleForPedestrians"        to AddProhibitedForPedestrians::class.simpleName!!,
    "AddWheelChairAccessPublicTransport" to AddWheelchairAccessPublicTransport::class.simpleName!!,
    "AddWheelChairAccessToilets"         to AddWheelchairAccessToilets::class.simpleName!!,
    "AddSidewalks"                       to AddSidewalk::class.simpleName!!,
    "DetailRoadSurface"                  to AddRoadSurface::class.simpleName!!,
    "AddTrafficSignalsBlindFeatures"     to AddTrafficSignalsVibration::class.simpleName!!,
    "AddSuspectedOneway"                 to AddOneway::class.simpleName!!,
    "AddPicnicTableCover"                to AddAmenityCover::class.simpleName!!,
    "AddCrossingType"                    to AddCrossingMarkings::class.simpleName!!,
    // whether lit roads have been added in context of the quest or the overlay should not matter for the statistics
    "WayLitOverlay"                      to AddWayLit::class.simpleName!!,
    "SidewalkOverlay"                    to AddSidewalk::class.simpleName!!,
    "CyclewayOverlay"                    to AddCycleway::class.simpleName!!,
    "BuildingsOverlay"                   to AddBuildingType::class.simpleName!!,
    "AddStreetParking"                   to StreetParkingOverlay::class.simpleName!!,
    "AddIsDefibrillatorIndoor"           to AddIsAmenityIndoor::class.simpleName!!,
    "ShopsOverlay"                       to PlacesOverlay::class.simpleName!!,
)

/** this is only public so that it can be previewed in compose */
val links = listOf(

    /* ---------------------------------------- Intro ----------------------------------------*/
    Link(
        "wiki",
        "https://wiki.openstreetmap.org",
        "OpenStreetMap Wiki",
        LinkCategory.INTRO,
        R.drawable.ic_link_wiki,
        R.string.link_wiki_description
    ),
    Link(
        "forum",
        "https://community.openstreetmap.org",
        "OpenStreetMap Community Forum",
        LinkCategory.INTRO,
        R.drawable.ic_link_openstreetmap,
        R.string.link_forum_description
    ),
    Link(
        "calendar",
        "https://osmcal.org",
        "OpenStreetMap Calendar",
        LinkCategory.INTRO,
        R.drawable.ic_link_openstreetmap,
        R.string.link_calendar_description
    ),
    Link(
        "welcomemat",
        "https://welcome.openstreetmap.org",
        "Welcome Mat",
        LinkCategory.INTRO,
        R.drawable.ic_link_welcome_mat,
        R.string.link_welcome_mat_description
    ),
    Link(
        "learnosm",
        "https://learnosm.org/en/beginner/",
        "learnOSM",
        LinkCategory.INTRO,
        R.drawable.ic_link_learnosm,
        R.string.link_learnosm_description
    ),
    Link(
        "weeklyosm",
        "https://weeklyosm.eu/",
        "weeklyOSM",
        LinkCategory.INTRO,
        R.drawable.ic_link_weeklyosm,
        R.string.link_weeklyosm_description
    ),
    Link(
        "neis-one",
        "https://resultmaps.neis-one.org/",
        "ResultMaps",
        LinkCategory.INTRO,
        R.drawable.ic_link_neis_one,
        R.string.link_neis_one_description
    ),
    Link(
        "disaster.ninja",
        "https://disaster.ninja/live/#position=11,46;zoom=3",
        "disaster.ninja",
        LinkCategory.INTRO,
        R.drawable.ic_link_kontur,
        R.string.link_disaster_ninja_description
    ),

    Link(
        "mapillary",
        "https://www.mapillary.com",
        "Mapillary",
        LinkCategory.INTRO,
        R.drawable.ic_link_mapillary,
        R.string.link_mapillary_description
    ),

    Link(
        "kartaview",
        "https://kartaview.org/map/",
        "KartaView",
        LinkCategory.INTRO,
        R.drawable.ic_link_kartaview,
        R.string.link_openstreetcam_description
    ),

    Link(
        "ohsomehex",
        "https://hex.ohsome.org",
        "OSM History eXplorer",
        LinkCategory.INTRO,
        R.drawable.ic_link_ohsomehex,
        R.string.link_ohsomehex_description
    ),

    /* --------------------------------------- Editors ---------------------------------------*/

    Link( // TODO: this is hardly used and not updated for 2 years, remove?
        "pic4review",
        "https://pic4review.pavie.info",
        "Pic4Review",
        LinkCategory.EDITORS,
        R.drawable.ic_link_pic4review,
        R.string.link_pic4review_description
    ),
    Link(
        "ideditor",
        "http://ideditor.com",
        "iD",
        LinkCategory.EDITORS,
        R.drawable.ic_link_ideditor,
        R.string.link_ideditor_description
    ),
    Link(
        "vespucci",
        "https://vespucci.io",
        "Vespucci",
        LinkCategory.EDITORS,
        R.drawable.ic_link_vespucci,
        R.string.link_vespucci_description
    ),
    Link(
        "josm",
        "https://josm.openstreetmap.de",
        "JOSM",
        LinkCategory.EDITORS,
        R.drawable.ic_link_josm,
        R.string.link_josm_description
    ),
    Link(
        "notesreview",
        "https://ent8r.github.io/NotesReview/",
        "NotesReview",
        LinkCategory.EDITORS,
        R.drawable.ic_link_notesreview,
        R.string.link_notesreview_description
    ),
    Link(
        "every-door",
        "https://every-door.app/",
        "Every Door",
        LinkCategory.EDITORS,
        R.drawable.ic_link_every_door,
        R.string.link_every_door_description
    ),
    Link(
        "mapcomplete",
        "https://mapcomplete.org/",
        "MapComplete",
        LinkCategory.EDITORS,
        R.drawable.ic_link_mapcomplete,
        R.string.link_mapcomplete_description
    ),

    /* ---------------------------------------- Maps -----------------------------------------*/

    Link(
        "openstreetbrowser",
        "https://openstreetbrowser.org",
        "OpenStreetBrowser",
        LinkCategory.MAPS,
        R.drawable.ic_link_openstreetbrowser,
        R.string.link_openstreetbrowser_description
    ),
    Link(
        "qwant_maps",
        "https://map.qwant.com/",
        "Qwant Maps",
        LinkCategory.MAPS,
        R.drawable.ic_link_qwant,
        R.string.link_qwant_maps_description
    ),
    Link(
        "organic_maps",
        "https://organicmaps.app/",
        "Organic Maps",
        LinkCategory.MAPS,
        R.drawable.ic_link_organic_maps,
        R.string.link_organic_maps_description
    ),
    Link(
        "osmand",
        "https://osmand.net/",
        "OsmAnd",
        LinkCategory.MAPS,
        R.drawable.ic_link_osmand,
        R.string.link_osmand_description
    ),
    Link(
        "cyclosm",
        "https://www.cyclosm.org",
        "CyclOSM",
        LinkCategory.MAPS,
        R.drawable.ic_link_cyclosm,
        R.string.link_cyclosm_description
    ),
    Link(
        "öpnvkarte",
        "https://öpnvkarte.de",
        "ÖPNVKarte",
        LinkCategory.MAPS,
        R.drawable.ic_link_opnvkarte,
        R.string.link_opnvkarte_description
    ),
    Link(
        "wheelmap",
        "https://wheelmap.org",
        "wheelmap.org",
        LinkCategory.MAPS,
        R.drawable.ic_link_wheelmap,
        R.string.link_wheelmap_description
    ),
    Link(
        "veggiekarte",
        "https://www.veggiekarte.de",
        "veggiekarte.de",
        LinkCategory.MAPS,
        R.drawable.ic_link_veggiekarte,
        R.string.link_openvegemap_description
    ),
    Link(
        "osm_buildings",
        "https://osmbuildings.org",
        "OSM Buildings",
        LinkCategory.MAPS,
        R.drawable.ic_link_osm_buildings,
        R.string.link_osm_buildings_description
    ),
    Link(
        "mapy_tactile",
        "https://hapticke.mapy.cz/?x=14.4343228&y=50.0652972&z=19&lang=en",
        "Mapy.cz Tactile",
        LinkCategory.MAPS,
        R.drawable.ic_link_mapy_tactile,
        R.string.link_mapy_tactile_description
    ),
    Link( // TODO no achievement yet
        "openinframap",
        "https://openinframap.org",
        "Open Infrastructure Map",
        LinkCategory.MAPS,
        null,
        R.string.link_openinframap_description
    ),
    Link(
        "indoorequal",
        "https://indoorequal.org",
        "indoor=",
        LinkCategory.MAPS,
        R.drawable.ic_link_indoorequal,
        R.string.link_indoorequal_description
    ),
    Link(
        "osmhydrant",
        "https://www.osmhydrant.org",
        "OsmHydrant",
        LinkCategory.MAPS,
        R.drawable.ic_link_osmhydrant,
        R.string.link_osmhydrant_description
    ),
    Link(
        "sunders",
        "https://sunders.uber.space/",
        "Surveillance under Surveillance",
        LinkCategory.MAPS,
        R.drawable.ic_link_sunders,
        R.string.link_sunders_description
    ),

    /* -------------------------------------- Showcase ---------------------------------------*/
    Link(
        "brouter",
        "https://brouter.de/brouter-web/",
        "BRouter",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_brouter,
        R.string.link_brouter_description
    ),

    Link(
        "openrouteservice-wheelchair",
        "https://classic-maps.openrouteservice.org/directions?b=3",
        "Openrouteservice (Wheelchair)",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_heigit,
        R.string.link_openrouteservice_wheelchair_description
    ),
    Link(
        "touch_mapper",
        "https://touch-mapper.org",
        "Touch Mapper",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_touch_mapper,
        R.string.link_touch_mapper_description
    ),
    Link(
        "nominatim",
        "https://nominatim.openstreetmap.org",
        "Nominatim",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_openstreetmap,
        R.string.link_nominatim_description
    ),
    Link(
        "photon",
        "https://photon.komoot.io",
        "Photon",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_photon,
        R.string.link_photon_description
    ),
    Link(
        "osrm",
        "https://map.project-osrm.org",
        "OSRM",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_osrm,
        R.string.link_osrm_description
    ),
    Link(
        "openrouteservice",
        "https://maps.openrouteservice.org",
        "Openrouteservice",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_heigit,
        R.string.link_openrouteservice_description
    ),
    Link(
        "graphhopper",
        "https://graphhopper.com/maps/",
        "GraphHopper",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_graphhopper,
        R.string.link_graphhopper_description
    ),
    Link(
        "valhalla",
        "https://valhalla.openstreetmap.de/",
        "Valhalla",
        LinkCategory.SHOWCASE,
        R.drawable.ic_link_valhalla,
        R.string.link_valhalla_description
    ),

    /* -------------------------------------- Goodies ----------------------------------------*/
    Link(
        "umap",
        "https://umap.openstreetmap.fr",
        "uMap",
        LinkCategory.GOODIES,
        R.drawable.ic_link_umap,
        R.string.link_umap_description
    ),
    Link(
        "city_roads",
        "https://anvaka.github.io/city-roads",
        "city roads",
        LinkCategory.GOODIES,
        R.drawable.ic_link_city_roads,
        R.string.link_city_roads_description
    ),
    Link(
        "myosmatic",
        "https://print.get-map.org",
        "MyOSMatic",
        LinkCategory.GOODIES,
        R.drawable.ic_link_myosmatic,
        R.string.link_myosmatic_description
    ),
    Link(
        "show_me_the_way",
        "https://osmlab.github.io/show-me-the-way",
        "show-me-the-way",
        LinkCategory.GOODIES,
        R.drawable.ic_link_osmlab,
        R.string.link_show_me_the_way_description
    ),
    Link( // TODO map does not show since Nov 2023
        "osm-haiku",
        "https://satellitestud.io/osm-haiku/app",
        "OpenStreetMap Haiku",
        LinkCategory.GOODIES,
        R.drawable.ic_link_haiku,
        R.string.link_osm_haiku_description
    ),
    Link(
        "openorienteeringmap",
        "https://oomap.co.uk/global/",
        "OpenOrienteeringMap",
        LinkCategory.GOODIES,
        R.drawable.ic_link_openorienteeringmap,
        R.string.link_openorienteeringmap_description
    ),
    Link(
        "figuregrounder",
        "https://hanshack.com/figuregrounder/",
        "Figuregrounder",
        LinkCategory.GOODIES,
        R.drawable.ic_link_figuregrounder,
        R.string.link_figuregrounder_description
    ),
    Link(
        "backofyourhand",
        "https://backofyourhand.com/",
        "Back Of Your Hand",
        LinkCategory.GOODIES,
        R.drawable.ic_link_backofyourhand,
        R.string.link_backofyourhand_description
    ),
    Link(
        "opencampingmap",
        "https://opencampingmap.org/",
        "Open Camping Map",
        LinkCategory.GOODIES,
        R.drawable.ic_link_opencampingmap,
        R.string.link_opencampingmap_description
    ),
    Link(
        "prettymapp",
        "https://prettymapp.streamlit.app/",
        "Prettymapp",
        LinkCategory.GOODIES,
        R.drawable.ic_link_prettymapp,
        R.string.link_prettymapp_description
    )
)

private val linksById = links.associateBy { it.id }

/** this is only public so that it can be previewed in compose */
val achievements = listOf(

    Achievement(
        "first_edit",
        R.drawable.ic_achievement_first_edit,
        R.string.achievement_first_edit_title,
        R.string.achievement_first_edit_description,
        TotalEditCount,
        { 1 },
        mapOf(),
        1
    ),

    Achievement(
        "surveyor",
        R.drawable.ic_achievement_surveyor,
        R.string.achievement_surveyor_title,
        R.string.achievement_surveyor_solved_X,
        TotalEditCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            /* Achievements rewarded for general activity should first cover introduction to OSM
               and then most of all goodies and general (OSM) showcases */
            1 to links("wiki"), // most important link
            2 to links("forum"), // this is the go-to place when people have questions too
            3 to links("welcomemat"),
            4 to links("show_me_the_way"),

            6 to links("myosmatic"),

            8 to links("osm-haiku"),

            10 to links("umap"),

            12 to links("backofyourhand")
        )
    ),

    Achievement(
        "regular",
        R.drawable.ic_achievement_regular,
        R.string.achievement_regular_title,
        R.string.achievement_regular_description,
        DaysActive,
        // levels: 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, ...
        { 4 },
        mapOf(
            /* Achievements rewarded for regular activity should focus mostly on introducing
               user to the community and other editors. Introducing editors should be delayed
               because users should not get sidetracked too early - best first show community
               intro links */
            1 to links("weeklyosm"), // newspaper first
            2 to links("calendar"),
            3 to links("neis-one"), // who-is-around-me, leaderboards etc fits into "community intro"
            4 to links("ideditor"),
            5 to links("learnosm"), // learnosm mostly concerns itself with tutorials about how to use editors
            6 to links("disaster.ninja"),
            7 to links("vespucci", "josm"), // together because both are full-featured-editors for each their platform
            8 to links("ohsomehex"),
            9 to links("notesreview"),
            10 to links("every-door"),
            // space for some other cool statistics tool (see comment above)
            12 to links("mapcomplete", "pic4review"),
        )
    ),

    Achievement(
        EditTypeAchievement.RARE.id,
        R.drawable.ic_achievement_rare,
        R.string.achievement_rare_title,
        R.string.achievement_rare_solved_X,
        EditsOfTypeCount,
        // levels: 3, 9, 18, 30, 45, 63, ...
        { lvl -> (lvl + 1) * 3 },
        mapOf()
    ),

    Achievement(
        EditTypeAchievement.CAR.id,
        R.drawable.ic_achievement_car,
        R.string.achievement_car_title,
        R.string.achievement_car_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            2 to links("city_roads"), // put it here because it shows roads

            5 to links("osrm"), // routing engines are not that interesting for end users
            6 to links("openrouteservice"),
            7 to links("graphhopper"),
            8 to links("valhalla"),
            12 to links("kartaview", "mapillary") // useful to OSM, but not directly OSM and interesting only to extreme enthusiasts
        )
    ),

    Achievement(
        EditTypeAchievement.VEG.id,
        R.drawable.ic_achievement_veg,
        R.string.achievement_veg_title,
        R.string.achievement_veg_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("veggiekarte")
        )
    ),

    Achievement(
        EditTypeAchievement.PEDESTRIAN.id,
        R.drawable.ic_achievement_pedestrian,
        R.string.achievement_pedestrian_title,
        R.string.achievement_pedestrian_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("öpnvkarte")
        )
    ),

    Achievement(
        EditTypeAchievement.BUILDING.id,
        R.drawable.ic_achievement_building,
        R.string.achievement_building_title,
        R.string.achievement_building_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("osm_buildings"),
            2 to links("figuregrounder"),
            3 to links("prettymapp")
        )
    ),

    Achievement(
        EditTypeAchievement.POSTMAN.id,
        R.drawable.ic_achievement_postman,
        R.string.achievement_postman_title,
        R.string.achievement_postman_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            // Nominatim should not appear too early because it is a bit technical, the functionality
            // itself is often integrated into other services
            3 to links("nominatim"),
            5 to links("photon")
        )
    ),

    Achievement(
        EditTypeAchievement.BLIND.id,
        R.drawable.ic_achievement_blind,
        R.string.achievement_blind_title,
        R.string.achievement_blind_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("touch_mapper"),
            2 to links("mapy_tactile")
        )
    ),

    Achievement(
        EditTypeAchievement.WHEELCHAIR.id,
        R.drawable.ic_achievement_wheelchair,
        R.string.achievement_wheelchair_title,
        R.string.achievement_wheelchair_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("wheelmap"),
            2 to links("openrouteservice-wheelchair")
        )
    ),

    Achievement(
        EditTypeAchievement.BICYCLIST.id,
        R.drawable.ic_achievement_bicyclist,
        R.string.achievement_bicyclist_title,
        R.string.achievement_bicyclist_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("cyclosm"),
            2 to links("brouter")
        )
    ),

    Achievement(
        EditTypeAchievement.CITIZEN.id,
        R.drawable.ic_achievement_citizen,
        R.string.achievement_citizen_title,
        R.string.achievement_citizen_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("openstreetbrowser"),
            2 to links("qwant_maps"),
            3 to links("organic_maps"),
            4 to links("indoorequal"),
            5 to links("osmand"),
            6 to links("sunders"),
        )
    ),

    Achievement(
        EditTypeAchievement.OUTDOORS.id,
        R.drawable.ic_achievement_outdoors,
        R.string.achievement_outdoors_title,
        R.string.achievement_outdoors_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("openorienteeringmap"),
            4 to links("opencampingmap")
        )
    ),

    Achievement(
        EditTypeAchievement.LIFESAVER.id,
        R.drawable.ic_achievement_lifesaver,
        R.string.achievement_lifesaver_title,
        R.string.achievement_lifesaver_solved_X,
        EditsOfTypeCount,
        // levels: 10, 30, 60, 100, 150, 210, 280, 360, 450, 550, 660, 780, 910, 1050, ...
        { lvl -> (lvl + 1) * 10 },
        mapOf(
            1 to links("osmhydrant")
        )
    ),

)

private fun links(vararg linksKeys: String = emptyArray()): List<Link> =
    linksKeys.map { linksById.getValue(it) }
