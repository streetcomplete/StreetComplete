package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowOther : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        (
         playground
         or historic
         or club
         or information and information !~ office
         or tourism ~ viewpoint|artwork|wilderness_hut
         or """.trimIndent() +

        mapOf(
            "amenity" to arrayOf(
                "place_of_worship", "toilets", "prison", "fire_station", "police", "ranger_station",
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",
                "monastery", "kindergarten", "school", "college", "university", "research_institute",
                "drinking_water", "shower", "post_box", "bbq", "grit_bin", "clock", "hunting_stand",
                "fountain", "public_bookcase"
            ),
            "leisure" to arrayOf(
                "sports_centre", "stadium", "marina",
                "horse_riding", "dance", "nature_reserve","pitch","playground"
            ),
            "landuse" to arrayOf(
                "cemetery", "allotments"
            ),
            "military" to arrayOf(
                "airfield", "barracks", "training_area"
            ),
            "emergency" to arrayOf(
                "fire_hydrant", "defibrillator", "phone", "life_ring",
                "fire_extinguisher", "water_tank", "suction_point"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") +
        "\n)"

    override val changesetComment = "Adjust public POIs and similar"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_poi_other
    override val dotColor = "gold"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_other

    override fun getTitle(tags: Map<String, String>) = R.string.quest_poi_misc_title

    override fun createForm() = NoAnswerFragment()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(filter)

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}
}
