package de.westnordost.streetcomplete.quests.guidepost_sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddGuidepostSports : OsmFilterQuestType<GuidepostSportsAnswer>() {

    override val elementFilter =
        """
        nodes with
          tourism = information
          and information ~ guidepost|route_marker
          and !hiking and !bicycle and !mtb and !climbing and !horse and !nordic_walking and !ski and !inline_skates and !running
          and !disused
          and !guidepost
    """

    override val changesetComment = "Specify what kind of guidepost"
    override val wikiLink = "Tag:information=guidepost"
    override val icon = R.drawable.ic_quest_guidepost_sport
    override val isDeleteElementEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_guidepost_sports_title

    override fun createForm() = AddGuidepostSportsForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with tourism = information and information ~ guidepost|route_marker")

    override fun applyAnswerTo(answer: GuidepostSportsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer is IsSimpleGuidepost) {
            applySimpleGuidepostAnswer(tags)
        } else if (answer is SelectedGuidepostSports) {
            answer.selectedSports.forEach { tags[it.key] = "yes" }
        }
    }
    private fun applySimpleGuidepostAnswer(tags: Tags) {
        tags["guidepost"] = "simple"
    }
}
