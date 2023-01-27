package de.westnordost.streetcomplete.quests.postbox_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.POSTMAN
import de.westnordost.streetcomplete.osm.Tags

class AddPostboxRef : OsmFilterQuestType<PostboxRefAnswer>() {

    override val elementFilter = """
        nodes with
        amenity = post_box
        and !ref and noref != yes and ref:signed != no and !~"ref:.*"
    """
    override val changesetComment = "Specify postbox refs"
    override val wikiLink = "Tag:amenity=post_box"
    override val icon = R.drawable.ic_quest_mail_ref
    override val isDeleteElementEnabled = true
    override val achievements = listOf(POSTMAN)
    // source: https://commons.wikimedia.org/wiki/Category:Post_boxes_by_country
    override val enabledInCountries = NoCountriesExcept(
        "FR", "GB", "GG", "IM", "JE", "MT", "IE", "SG", "CZ", "SK", "CH", "US"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_genericRef_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = post_box")

    override fun createForm() = AddPostboxRefForm()

    override fun applyAnswerTo(answer: PostboxRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoVisiblePostboxRef -> tags["ref:signed"] = "no"
            is PostboxRef ->          tags["ref"] = answer.ref
        }
    }
}
