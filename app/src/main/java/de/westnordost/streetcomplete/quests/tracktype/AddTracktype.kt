package de.westnordost.streetcomplete.quests.tracktype

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddTracktype : OsmFilterQuestType<Tracktype>() {

    override val elementFilter = """
        ways with highway = track
        and (
          !tracktype
          or tracktype != grade1 and tracktype older today -6 years
          or surface ~ ${ANYTHING_UNPAVED.joinToString("|")} and tracktype older today -6 years
          or tracktype older today -8 years
        )
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    /* ~paved tracks are less likely to change the surface type */
    override val changesetComment = "Specify tracktypes"
    override val wikiLink = "Key:tracktype"
    override val icon = R.drawable.ic_quest_tractor
    override val achievements = listOf(CAR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tracktype_title

    override fun createForm() = AddTracktypeForm()

    override fun applyAnswerTo(answer: Tracktype, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("tracktype", answer.osmValue)
    }
}
