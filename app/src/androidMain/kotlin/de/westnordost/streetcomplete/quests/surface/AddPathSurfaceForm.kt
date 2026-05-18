package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPathSurfaceForm(
    onAnswer: (SurfaceOrIsStepsAnswer) -> Unit,
    element: Element,
) {
    ItemSelectQuestForm(
        items = Surface.selectableValuesForWays,
        itemContent = { item ->
            ImageWithLabel(item.icon?.let { painterResource(it) }, stringResource(item.title))
        },
        onClickOk = { onAnswer(SurfaceAnswer(it)) },
        favoriteKey = "AddPathSurfaceForm",
        otherAnswers = listOfNotNull(
            if (element.couldBeSteps()) {
                Answer(stringResource(Res.string.quest_generic_answer_is_actually_steps)) { onAnswer(IsActuallyStepsAnswer) }
            } else null,
            if (element.tags["indoor"] != "yes") {
                Answer(stringResource(Res.string.quest_generic_answer_is_indoors)) { onAnswer(IsIndoorsAnswer) }
            } else null,
        )
    )
}
