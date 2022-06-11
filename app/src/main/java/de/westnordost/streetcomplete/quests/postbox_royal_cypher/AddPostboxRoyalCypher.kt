package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.POSTMAN

class AddPostboxRoyalCypher : OsmFilterQuestType<PostboxRoyalCypher>() {

    override val elementFilter = "nodes with amenity = post_box and !royal_cypher"
    override val changesetComment = "Add postbox royal cypher"
    override val wikiLink = "Key:royal_cypher"
    override val icon = R.drawable.ic_quest_crown
    override val isDeleteElementEnabled = true
    override val questTypeAchievements = listOf(POSTMAN)

    override fun isEnabled(countryInfo: CountryInfo) = countryInfo.postBoxesHaveRoyalCypher
        && !countryInfo.countryCodes.contains("IE") // https://github.com/streetcomplete/StreetComplete/pull/2922#issuecomment-850363348

    override fun getTitle(tags: Map<String, String>) = R.string.quest_postboxRoyalCypher_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = post_box")

    override fun createForm() = AddPostboxRoyalCypherForm()

    override fun applyAnswerTo(answer: PostboxRoyalCypher, tags: Tags, timestampEdited: Long) {
        tags["royal_cypher"] = answer.osmValue
    }
}
