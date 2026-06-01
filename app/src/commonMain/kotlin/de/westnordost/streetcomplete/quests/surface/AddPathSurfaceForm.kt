package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPathSurfaceForm(
    on: (QuestAction<SurfaceOrIsStepsAnswer>) -> Unit,
    element: Element,
) {
    ItemSelectQuestForm(
        items = Surface.selectableValuesForWays,
        itemContent = { item ->
            ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
        },
        on = {
            on(when (it) {
                is Answer<Surface> -> Answer(SurfaceAnswer(it.value))
                is Action -> it
            })
        },
        favoriteKey = "AddPathSurfaceForm",
        otherAnswers = { listOfNotNull(
            if (element.couldBeSteps()) {
                AnswerItem(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                    on(Answer(IsActuallyStepsAnswer))
                }
            } else null,
            if (element.tags["indoor"] != "yes") {
                AnswerItem(stringResource(Res.string.quest_generic_answer_is_indoors)) {
                    on(Answer(IsIndoorsAnswer))
                }
            } else null,
        ) }
    )
}
