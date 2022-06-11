package de.westnordost.streetcomplete.quests.postbox_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.POSTMAN

class AddPostboxRef : OsmFilterQuestType<PostboxRefAnswer>() {

    override val elementFilter = "nodes with amenity = post_box and !ref and !ref:signed"
    override val changesetComment = "Add postbox refs"
    override val wikiLink = "Tag:amenity=post_box"
    override val icon = R.drawable.ic_quest_mail_ref
    override val isDeleteElementEnabled = true
    override val questTypeAchievements = listOf(POSTMAN)

    override fun isEnabled(countryInfo: CountryInfo) = countryInfo.postBoxesHaveRef

    override fun getTitle(tags: Map<String, String>) = R.string.quest_postboxRef_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = post_box")

    override fun createForm() = AddPostboxRefForm()

    override fun applyAnswerTo(answer: PostboxRefAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is NoRefVisible -> tags["ref:signed"] = "no"
            is Ref ->          tags["ref"] = answer.ref
        }
    }
}
