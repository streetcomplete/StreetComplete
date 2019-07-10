package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil
import java.util.concurrent.FutureTask

class AddPlaceName(
    private val overpassServer: OverpassMapDataDao,
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<PlaceNameAnswer> {

    private val filter by lazy { FiltersParser().parse(
        "nodes, ways, relations with !name and !brand and noname != yes " +
        " and (shop and shop !~ no|vacant or tourism = information and information = office " +
        " or " +
        mapOf(
            "amenity" to arrayOf(
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub",                  // eat & drink
                "cinema", "theatre", "planetarium", "arts_centre", "studio",                                                            // culture
                "events_venue", "conference_centre", "exhibition_centre", "music_venue",                                                // events
                "townhall", "prison", "courthouse", "embassy", "police", "fire_station", "ranger_station",                              // civic
                "bank", "bureau_de_change", "money_transfer", "post_office", "library", "marketplace", "internet_cafe",                 // commercial
                "community_centre", "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", "youth_centre", // social
                "car_wash", "car_rental", "boat_rental", "fuel", "ferry_terminal",                                                      // transport
                "dentist", "doctors", "clinic", "pharmacy", "hospital",                                                                 // health care
                "place_of_worship", "monastery",                                                                                        // religious
                "kindergarten", "school", "college", "university", "research_institute",                                                // education
                "driving_school", "dive_centre", "language_school", "music_school",                                                     // learning
                "casino", "brothel", "gambling", "love_hotel", "stripclub",                                                             // bad stuff
                "animal_boarding", "animal_shelter", "animal_breeding", "veterinary"                                                    // animals
            ),
            "tourism" to arrayOf(
                "attraction", "zoo", "aquarium", "theme_park", "gallery", "museum",                                          // attractions
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site"  // accommodations
                // and tourism=information, see above
            ),
            "leisure" to arrayOf(
                "nature_reserve", "sports_centre", "fitness_centre", "dance", "golf_course",
                "water_park", "miniature_golf", "stadium", "marina", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon", "horse_riding"
            ),
            "office" to arrayOf(
                "insurance", "estate_agent", "travel_agent"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString(" or ") +
        ")"
    )}

    override val commitMessage = "Determine place names"
    override val icon = R.drawable.ic_quest_label

    override fun getTitle(tags: Map<String, String>) = R.string.quest_placeName_title_name

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        val overpassQuery = filter.toOverpassQLString(bbox) + OverpassQLUtil.getQuestPrintStatement()
        return overpassServer.getAndHandleQuota(overpassQuery) { element, geometry ->
            if(element.tags != null) {
                // only show places without names as quests for which a feature name is available
                if (featureDictionaryFuture.get().byTags(element.tags).find().isNotEmpty()) {
                    handler.handle(element, geometry);
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
