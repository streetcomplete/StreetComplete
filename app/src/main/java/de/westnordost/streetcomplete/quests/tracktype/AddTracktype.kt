package de.westnordost.streetcomplete.quests.tracktype

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi

class AddTracktype(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways with highway=track and !tracktype
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val commitMessage = "Add tracktype"
    override val wikiLink = "Key:tracktype"
    override val icon = R.drawable.ic_quest_tractor
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tracktype_title

    override fun createForm() = AddTracktypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("tracktype", answer)
    }
}
