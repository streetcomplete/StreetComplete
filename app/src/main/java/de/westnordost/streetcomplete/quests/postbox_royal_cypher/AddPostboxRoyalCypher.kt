package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.POSTMAN

class AddPostboxRoyalCypher : OsmFilterQuestType<PostboxRoyalCypher>() {

    override val elementFilter = "nodes with amenity = post_box and !royal_cypher"
    override val changesetComment = "Add postbox royal cypher"
    override val wikiLink = "Key:royal_cypher"
    override val icon = R.drawable.ic_quest_crown
    override val isDeleteElementEnabled = true
    override val enabledInCountries = NoCountriesExcept(
        // United Kingdom and some former nations of the British Empire, members of the Commonwealth of Nations and British overseas territories etc
        "GB", "GI", "CY", "HK", "MT", "NZ", "LK",
        // territories with agency postal services provided by the British Post Office
        "KW", "BH", "MA"
    )

    override val questTypeAchievements = listOf(POSTMAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_postboxRoyalCypher_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = post_box")

    override fun createForm() = AddPostboxRoyalCypherForm()

    override fun applyAnswerTo(answer: PostboxRoyalCypher, tags: Tags, timestampEdited: Long) {
        tags["royal_cypher"] = answer.osmValue
    }
}
