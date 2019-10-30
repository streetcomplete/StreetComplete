package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox
import java.util.concurrent.FutureTask

class AddPlaceName(
    private val overpassServer: OverpassMapDataDao,
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<PlaceNameAnswer> {

    private val filter by lazy { FiltersParser().parse("""
        nodes, ways, relations with 
        (
          shop and shop !~ no|vacant
          or craft
          or office
          or tourism = information and information = office 
          or """.trimIndent() +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "name only" tab.

        mapOf(
            "amenity" to arrayOf(
                // common
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub", // eat & drink
                "cinema", "planetarium", "casino", "arts_centre",                                                      // amenities
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",                    // civic
                "bank", /*atm,*/ "bureau_de_change", "money_transfer", "post_office", "marketplace", "internet_cafe",  // commercial
                "car_wash", "car_rental", "boat_rental", "fuel",                                                       // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",                                              // health

                //name only
                "theatre", "studio",                                                               // culture
                "events_venue", "conference_centre", "exhibition_centre", "music_venue",            // events
                "prison", "police", "fire_station", "ranger_station",                               // civic
                "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", // social
                "ferry_terminal",                                                                   // transport
                "hospital",                                                                         // health care
                "place_of_worship", "monastery",                                                    // religious
                "kindergarten", "school", "college", "university", "research_institute",            // education
                "driving_school", "dive_centre", "language_school", "music_school",                 // learning
                "brothel", "gambling", "love_hotel", "stripclub",                                   // bad stuff
                "animal_boarding", "animal_shelter", "animal_breeding"                              // animals
            ),
            "tourism" to arrayOf(
                // common
                "zoo", "aquarium", "theme_park", "gallery", "museum",

                // name only
                "attraction",                                                                                                           // attractions
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site", "chalet"   // accommodations
                // and tourism=information, see above
            ),
            "leisure" to arrayOf(
                //common
                "fitness_centre", "dance", "golf_course", "water_park",
                "miniature_golf", "bowling_alley", "horse_riding",  "amusement_arcade",
                "adult_gaming_centre", "tanning_salon",

                //name only
                "nature_reserve", "sports_centre", "dance", "golf_course",
                "stadium", "marina"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n  or ") + "\n" + """
        )
        and !name and !brand and noname != yes
        """.trimIndent()
    )}

    override val commitMessage = "Determine place names"
    override val icon = R.drawable.ic_quest_label

    override fun getTitle(tags: Map<String, String>) = R.string.quest_placeName_title_name
    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>) = arrayOf(featureName.value)

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        val overpassQuery = bbox.toGlobalOverpassBBox() + "\n" + filter.toOverpassQLString() + getQuestPrintStatement()
        return overpassServer.getAndHandleQuota(overpassQuery) { element, geometry ->
            if(element.tags != null) {
                // only show places without names as quests for which a feature name is available
                if (featureDictionaryFuture.get().byTags(element.tags).find().isNotEmpty()) {
                    handler.handle(element, geometry)
                }
            }
        }
    }

    override fun isApplicableTo(element: Element) = filter.matches(element)

    override fun createForm() = AddPlaceNameForm()

    override fun applyAnswerTo(answer: PlaceNameAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoPlaceNameSign -> changes.add("noname", "yes")
            is PlaceName -> changes.add("name", answer.name)
        }
    }
}
