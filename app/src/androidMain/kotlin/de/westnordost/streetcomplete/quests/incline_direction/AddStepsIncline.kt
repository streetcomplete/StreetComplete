package de.westnordost.streetcomplete.quests.incline_direction

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddStepsIncline : OsmFilterQuestType<Incline>() {

    override val elementFilter = """
        ways with highway = steps
         and (!indoor or indoor = no)
         and area != yes
         and access !~ private|no
         and !incline
    """
    override val changesetComment = "Specify which way leads up for steps"
    override val wikiLink = "Key:incline"
    override val icon = R.drawable.quest_steps
    override val title = Res.string.quest_steps_incline_title
    override val achievements = listOf(PEDESTRIAN)
    override val hint = Res.string.quest_arrow_tutorial

    @Composable
    override fun Form(onAnswer: (Incline) -> Unit) {
        AddInclineForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Incline, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) =
        answer.applyTo(tags)
}
