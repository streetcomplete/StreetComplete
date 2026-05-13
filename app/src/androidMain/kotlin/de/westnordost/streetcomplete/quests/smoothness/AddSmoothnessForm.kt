package de.westnordost.streetcomplete.quests.smoothness

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddSmoothnessForm(
    onAnswer: (SmoothnessAnswer) -> Unit,
) {
    val surfaceTag = element.tags["surface"]
    val items = remember { Smoothness.entries.filter { it.getImage(surfaceTag) != null } }

    var showObstacleHint by remember { mutableStateOf(false) }
    var confirmSurface by remember { mutableStateOf<Surface?>(null) }

    ItemSelectQuestForm(
        items = items,
        itemsPerRow = 1,
        itemContent = { item ->
            Box {
                ImageWithDescription(
                    painter = item.getImage(surfaceTag)?.let { painterResource(it) },
                    title = stringResource(item.title),
                    description = item.getDescription(surfaceTag)?.let { stringResource(it) }
                )
                Image(
                    painter = painterResource(item.icon),
                    contentDescription = item.emoji,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        onClickOk = { onAnswer(SmoothnessValueAnswer(it)) },
        otherAnswers = listOfNotNull(
            Answer(stringResource(Res.string.quest_smoothness_wrong_surface)) {
                confirmSurface = surfaceTag?.let { parseSurface(it) }
            },
            if (element.couldBeSteps()) {
                Answer(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                    onAnswer(IsActuallyStepsAnswer)
                }
            } else null,
            Answer(stringResource(Res.string.quest_smoothness_obstacle)) {
                showObstacleHint = true
            }
        )
    )

    if (showObstacleHint) {
        InfoDialog(
            onDismissRequest = { showObstacleHint = false },
            text = { Text(stringResource(Res.string.quest_smoothness_obstacle_hint)) }
        )
    }
    confirmSurface?.let { surface ->
        ConfirmSurfaceDialog(
            onDismissRequest = { confirmSurface = null },
            surface = surface,
            onConfirmSurface = { composeNote() },
            onWrongSurface = { onAnswer(WrongSurfaceAnswer) }
        )
    }
}
