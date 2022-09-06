package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.VEG
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddVegan : OsmFilterQuestType<DietAvailabilityAnswer>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity = ice_cream
          or diet:vegetarian ~ yes|only and
          (
            amenity ~ restaurant|cafe|fast_food|food_court and food != no
            or amenity ~ pub|nightclub|biergarten|bar and food = yes
          )
        )
        and (
          !diet:vegan
          or diet:vegan != only and diet:vegan older today -4 years
        )
    """
    override val changesetComment = "Survey whether places have vegan food"
    override val wikiLink = "Key:diet"
    override val icon = R.drawable.ic_quest_restaurant_vegan
    override val isReplaceShopEnabled = true
    override val achievements = listOf(VEG, CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_vegan_title2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_vegan)

    override fun applyAnswerTo(answer: DietAvailabilityAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is DietAvailability -> tags.updateWithCheckDate("diet:vegan", answer.osmValue)
            NoFood -> tags["food"] = "no"
        }
    }
}
