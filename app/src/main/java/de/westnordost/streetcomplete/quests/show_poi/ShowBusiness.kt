package de.westnordost.streetcomplete.quests.show_poi

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.quests.NoAnswerFragment
import de.westnordost.streetcomplete.quests.getLabelOrElementSelectionDialog

class ShowBusiness : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        (
         shop and shop !~ no|vacant
         or craft
         or office
         or tourism = information and information = office
         or healthcare
         or """.trimIndent() +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "wheelchair only" tab.

        mapOf(
            "amenity" to arrayOf(
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub", // eat & drink
                "cinema", "planetarium", "casino",                                                                     // amenities
                "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace", "internet_cafe", "payment_centre", // commercial
                "car_wash", "car_rental", "fuel",                                                                      // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",                                              // health
                "animal_boarding", "animal_shelter", "animal_breeding",                                                // animals
                "coworking_space", "prep_school", "dojo",

                "boat_rental",
                "theatre",                             // culture
                "conference_centre", "arts_centre",    // events
                "ferry_terminal",                      // transport
                "hospital",                            // health care

                "studio",                                                                // culture
                "events_venue", "exhibition_centre", "music_venue",                      // events
                "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", // social
                "driving_school", "dive_centre", "language_school", "music_school",      // learning
                "brothel", "gambling", "love_hotel", "stripclub"                         // bad stuff
            ),
            "tourism" to arrayOf(
                "zoo", "aquarium", "theme_park", "gallery", "museum", "attraction",
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site", "chalet" // accommodations
            ),
            "leisure" to arrayOf(
                "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon", "escape_game",
                "sauna", "trampoline_park"

            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") +
        "\n)"

    override val changesetComment = "Adjust shops and similar"
    override val wikiLink = "Key:shop"
    override val icon = R.drawable.ic_quest_poi_business
    override val dotColor = "sandybrown"
    override val isReplacePlaceEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_business

    override fun getTitle(tags: Map<String, String>) = R.string.quest_poi_business_title

    override fun createForm() = NoAnswerFragment()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlace() }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}

    override fun getQuestSettingsDialog(context: Context) = getLabelOrElementSelectionDialog(context, this, prefs)
}
