package de.westnordost.streetcomplete.quests.handrail

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddHandrail : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways with highway = steps
         and (!indoor or indoor = no)
         and access !~ private|no
         and (!conveying or conveying = no)
         and (
           !handrail and !handrail:center and !handrail:left and !handrail:right
           or handrail = no and handrail older today -4 years
           or handrail older today -8 years
           or older today -8 years
         )
    """

    override val changesetComment = "Specify whether steps have handrails"
    override val wikiLink = "Key:handrail"
    override val icon = R.drawable.quest_steps_handrail
    override val title = Res.string.quest_handrail_title
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR)

    @Composable
    override fun Form(onAnswer: (Boolean) -> Unit) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("handrail", answer.toYesNo())
        if (!answer) {
            tags.remove("handrail:left")
            tags.remove("handrail:right")
            tags.remove("handrail:center")
        }
    }
}
