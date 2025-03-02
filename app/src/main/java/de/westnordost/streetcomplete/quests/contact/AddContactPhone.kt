package de.westnordost.streetcomplete.quests.contact

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace

class AddContactPhone : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
         tourism = information and information = office
         or craft
         or healthcare
         or """.trimIndent() +
         PLACES_FOR_CONTACT_QUESTS +
        "\n) and !phone and !contact:phone and !contact:mobile and !brand and (name or operator)"

    override val changesetComment = "Add phone number"
    override val wikiLink = "Key:phone"
    override val icon = R.drawable.ic_quest_phone
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_contact_phone

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlace() }

    override fun createForm() = AddContactPhoneForm()

    override val isReplacePlaceEnabled: Boolean = true

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["phone"] = answer
    }

}

val PLACES_FOR_CONTACT_QUESTS = mapOf(
    "amenity" to arrayOf(
        "restaurant", "cafe", "internet_cafe",
        "cinema", "townhall", "embassy", "community_centre", "youth_centre", "library",
        "dentist", "doctors", "clinic", "veterinary", "animal_shelter",
        "arts_centre", "ferry_terminal", "prep_school", "dojo"
    ),
    "leisure" to arrayOf("fitness_centre", "bowling_alley", "sports_centre", "escape_game"),
    "office" to arrayOf(
        "insurance", "government", "travel_agent", "tax_advisor", "religion", "employment_agency",
        "lawyer", "estate_agent", "therapist", "notary"
    ),
    "shop" to arrayOf(
        "beauty", "massage", "hairdresser", "wool", "tattoo", "electrical", "glaziery", "tailor",
        "computer", "electronics", "hifi", "bicycle", "outdoor", "sports", "art", "craft", "model",
        "musical_instrument", "camera", "books", "travel_agency", "cheese", "chocolate", "coffee", "health_food"
    ),
    "tourism" to arrayOf("zoo", "aquarium", "gallery", "museum", "alpine_hut", "camp_site", "caravan_site"),
).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ")
