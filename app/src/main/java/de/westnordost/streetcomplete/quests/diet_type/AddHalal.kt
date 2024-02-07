package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedShop
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddHalal : OsmFilterQuestType<DietAvailabilityAnswer>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ restaurant|cafe|fast_food|ice_cream|food_court and food != no
          or shop ~ butcher|supermarket|ice_cream|convenience
        )
        and diet:vegan != only
        and (
          !diet:halal
          or diet:halal != only and diet:halal older today -4 years
        )
    """
    override val changesetComment = "Specify whether places are halal"
    override val wikiLink = "Key:diet:halal"
    override val icon = R.drawable.ic_quest_halal
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside_regional_warning

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_halal_name_title2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedShop() }

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_halal)

    override fun applyAnswerTo(answer: DietAvailabilityAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is DietAvailability -> tags.updateWithCheckDate("diet:halal", answer.osmValue)
            NoFood -> tags["food"] = "no"
        }
    }
}
