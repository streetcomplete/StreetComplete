package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowCamera : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          man_made = surveillance
    """
    override val changesetComment = "Change camera"
    override val wikiLink = "Tag:surveillance:type"
    override val icon = R.drawable.ic_quest_poi_camera
    override val dotColor = "mediumvioletred"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_camera

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_camera_title

    override fun getTitleArgs(tags: Map<String, String>) =
        arrayOf(tags.entries.toString())

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
    }
}
