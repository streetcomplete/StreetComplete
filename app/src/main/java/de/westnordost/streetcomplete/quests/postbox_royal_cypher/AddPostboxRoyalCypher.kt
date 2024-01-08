package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags

class AddPostboxRoyalCypher : OsmFilterQuestType<PostboxRoyalCypher>() {

    override val elementFilter = "nodes with amenity = post_box and !royal_cypher"
    override val changesetComment = "Specify postbox royal cyphers"
    override val wikiLink = "Key:royal_cypher"
    override val icon = R.drawable.ic_quest_crown
    override val isDeleteElementEnabled = true
    override val achievements = listOf(POSTMAN)
    override val enabledInCountries = NoCountriesExcept(
        // United Kingdom and some former nations of the British Empire, members of the Commonwealth of Nations and British overseas territories etc.
        "GB", "GI", "CY", "HK", "MT", "LK",
        // territories with agency postal services provided by the British Post Office
        "KW", "BH", "MA"
        // Not New Zealand: https://wiki.openstreetmap.org/w/index.php?title=Talk:StreetComplete/Quests&oldid=2599288#Quests_in_New_Zealand
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_postboxRoyalCypher_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = post_box")

    override fun createForm() = AddPostboxRoyalCypherForm()

    override fun applyAnswerTo(answer: PostboxRoyalCypher, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["royal_cypher"] = answer.osmValue
    }
}
