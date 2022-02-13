package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.isKindOfShopExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.bench_backrest.BenchBackrestAnswer

class AddSeating : OsmFilterQuestType<Seating>() {
    override val elementFilter = """
        nodes, ways with amenity ~ restaurant|cafe|fast_food|ice_cream|food_court|pub|bar
        and !outdoor_seating
    """
    override val changesetComment = "Add seating info"
    override val wikiLink = "Key:outdoor_seating"
    override val icon = R.drawable.ic_terrace_seating_512
    override val isReplaceShopEnabled = true

    override val questTypeAchievements = listOf(QuestTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_seating_name_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes, ways with
            amenity = bar
            or amenity = pub
            or amenity = food_court
            or amenity = ice_cream
            or amenity = restaurant
            or amenity = cafe
            or amenity = fast_food
        """)

    override fun createForm() = AddSeatingForm()

    override fun applyAnswerTo(answer: Seating, tags: Tags, timestampEdited: Long) {
        when (answer) {
            Seating.NO -> {
                tags["takeaway"] = "only"
                tags["outdoor_seating"] = "no"
                tags["indoor_seating"] = "no"
            }
            Seating.ONLY_OUTDOOR -> {
                tags["outdoor_seating"] = "yes"
                tags["indoor_seating"] = "no"
            }
            Seating.ONLY_INDOOR -> {
                tags["outdoor_seating"] = "no"
                tags["indoor_seating"] = "yes"
            }
            Seating.INDOOR_AND_OUTDOOR -> {
                tags["outdoor_seating"] = "yes"
                tags["indoor_seating"] = "yes"
            }
        }
    }
}
