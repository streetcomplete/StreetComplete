package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.containsAny

class AddSmoking : OsmFilterQuestType<SmokingAllowed>() {

    private fun elementFilterBasicFragment(prefix: String? = null): String {
        val p = if (prefix != null) "$prefix:" else ""
        return """
              {p}amenity ~ bar|cafe|pub|biergarten|restaurant|food_court|nightclub|stripclub
              or {p}leisure ~ outdoor_seating
              or ({p}amenity ~ fast_food|ice_cream or {p}shop ~ ice_cream|deli|bakery|coffee|tea|wine)
        """.trimIndent()
    }

    /* note: outdoor_seating/indoor_seating extra clause ONLY applies to last group in
       elementFilterBasicFragment(), and not to whole of it */
    override val elementFilter = """
            nodes, ways, relations with
            (
                ${elementFilterBasicFragment()} and (outdoor_seating != no or indoor_seating != no)
            )
            and takeaway != only
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
        getMapData().filter("""
            nodes, ways, relations with
            (
                ${elementFilterBasicFragment()} or
                ${elementFilterBasicFragment("disused")} or
                ${IS_SHOP_OR_DISUSED_SHOP_EXPRESSION} or
            )
        """)

    override fun createForm() = SmokingAllowedAnswerForm()

    override fun applyAnswerTo(answer: SmokingAllowed, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("smoking", answer.osmValue)
    }

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(listOf("name", "brand", "operator"))

}
