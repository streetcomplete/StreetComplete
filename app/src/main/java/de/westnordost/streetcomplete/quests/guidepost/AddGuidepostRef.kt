package de.westnordost.streetcomplete.quests.guidepost

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
class AddGuidepostRef : OsmFilterQuestType<GuidepostRefAnswer>() {

    override val elementFilter = """
        nodes with
            (information = guidepost or guidepost) and guidepost != simple
        and !ref and noref != yes and !~"ref:.*" and hiking = yes

    """
    override val changesetComment = "Specify guidepost refs"
    override val wikiLink = "Tag:information=guidepost"
    override val icon = R.drawable.ic_quest_guidepost_ref
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_genericRef_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with information ~ guidepost|map")
    override val highlightedElementsRadius: Double get() = 200.0

    override val defaultDisabledMessage: Int = R.string.quest_guidepost_disabled_msg

    override fun createForm() = AddGuidepostRefForm()

    override fun applyAnswerTo(answer: GuidepostRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoVisibleGuidepostRef -> tags["ref:signed"] = "no"
            is GuidepostRef ->          tags["ref"] = answer.ref
        }
    }
}
