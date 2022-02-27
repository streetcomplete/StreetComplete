package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.containsAny

class AddSmoking : OsmFilterQuestType<SmokingAllowed>() {

    private val elementFilterBasic = """
        nodes, ways with
        (
          amenity ~ bar|cafe|pub|biergarten|restaurant|food_court|nightclub|stripclub
          or leisure ~ outdoor_seating
          or amenity ~ fast_food|ice_cream and (outdoor_seating != no or indoor_seating != no)
        )
        and takeaway != only
    """

    override val elementFilter = elementFilterBasic + """
        and (!smoking or smoking older today -8 years)
    """

    override val changesetComment = "Add smoking status"
    override val wikiLink = "Key:smoking"
    override val icon = R.drawable.ic_quest_smoke
    override val isReplaceShopEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside_regional_warning

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) =
        if (hasProperName(tags))
            R.string.quest_smoking_name_type_title
        else
            R.string.quest_smoking_no_name_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"] ?: tags["operator"]
        return arrayOfNotNull(name, featureName.value)
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(elementFilterBasic)

    override fun createForm() = SmokingAllowedAnswerForm()

    override fun applyAnswerTo(answer: SmokingAllowed, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("smoking", answer.osmValue)
    }

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(listOf("name", "brand", "operator"))

}
