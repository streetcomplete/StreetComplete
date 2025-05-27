package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddGlutenFree : OsmFilterQuestType<DietAvailabilityAnswer>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ restaurant|cafe|fast_food|food_court and food != no
          or amenity ~ pub|nightclub|biergarten|bar and food = yes
          or shop ~ supermarket|convenience|deli
          or tourism ~ alpine_hut and food != no
        )
        and (
          !diet:gluten_free
          or diet:gluten_free != only and diet:gluten_free older today -4 years
        )
    """
    override val changesetComment = "Specify whether places are gluten-free"
    override val wikiLink = "Key:diet:gluten_free"
    override val icon = R.drawable.ic_quest_glutenfree
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override val hint = R.string.quest_dietType_explanation_glutenfree

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_glutenfree_name_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = AddDietTypeForm()

    override fun applyAnswerTo(answer: DietAvailabilityAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is DietAvailability -> tags.updateWithCheckDate("diet:gluten_free", answer.osmValue)
            NoFood -> tags["food"] = "no"
        }
    }
}
