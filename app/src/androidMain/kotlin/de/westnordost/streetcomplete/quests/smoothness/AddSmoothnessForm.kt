package de.westnordost.streetcomplete.quests.smoothness

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddSmoothnessForm : AbstractOsmQuestForm<SmoothnessAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
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
            onClickOk = { applyAnswer(SmoothnessValueAnswer(it)) },
            prefs = prefs,
            favoriteKey = "AddSmoothnessForm",
            moveFavoritesToFront = false,
            otherAnswers = listOfNotNull(
                Answer(stringResource(Res.string.quest_smoothness_wrong_surface)) {
                    confirmSurface = surfaceTag?.let { parseSurface(it) }
                },
                if (element.couldBeSteps()) {
                    Answer(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                        applyAnswer(IsActuallyStepsAnswer)
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
                onWrongSurface = { applyAnswer(WrongSurfaceAnswer) }
            )
        }
    }
}

@Composable
private fun ConfirmSurfaceDialog(
    onDismissRequest: () -> Unit,
    surface: Surface,
    onConfirmSurface: () -> Unit,
    onWrongSurface: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        buttons = {
            TextButton(onClick = { onConfirmSurface(); onDismissRequest() }) {
                Text(stringResource(Res.string.quest_generic_hasFeature_yes_leave_note))
            }
            TextButton(onClick = { onWrongSurface(); onDismissRequest() }) {
                Text(stringResource(Res.string.quest_generic_hasFeature_no))
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(Res.string.quest_smoothness_surface_value))
                ImageWithLabel(
                    painter = surface.icon?.let { painterResource(it) },
                    label = stringResource(surface.title),
                )
                Text(stringResource(Res.string.quest_address_answer_no_housenumber_message2b))
            }
        }
    )
}
