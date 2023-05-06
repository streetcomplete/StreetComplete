package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddKosher : OsmFilterQuestType<DietAvailabilityAnswer>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ restaurant|cafe|fast_food|ice_cream|food_court and food != no
          or amenity ~ pub|nightclub|biergarten|bar and food = yes
          or shop ~ butcher|supermarket|ice_cream|convenience
        )
        and (
          !diet:kosher
          or diet:kosher != only and diet:kosher older today -4 years
        )
    """
    override val changesetComment = "Specify whether places are kosher"
    override val wikiLink = "Key:diet:kosher"
    override val icon = R.drawable.ic_quest_kosher
    override val isReplaceShopEnabled = true
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside_regional_warning

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_kosher_name_title2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_kosher)

    override fun applyAnswerTo(answer: DietAvailabilityAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is DietAvailability -> tags.updateWithCheckDate("diet:kosher", answer.osmValue)
            NoFood -> tags["food"] = "no"
        }
    }
}
