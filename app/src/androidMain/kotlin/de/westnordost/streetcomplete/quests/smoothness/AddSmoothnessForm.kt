package de.westnordost.streetcomplete.quests.smoothness

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_address_answer_no_housenumber_message2b
import de.westnordost.streetcomplete.resources.quest_smoothness_surface_value
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddSmoothnessForm : AImageListQuestForm<Smoothness, SmoothnessAnswer>() {

    private val surfaceTag get() = element.tags["surface"]
    override val items = Smoothness.entries

    override val itemsPerRow = 1
    override val moveFavoritesToFront = false

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_smoothness_wrong_surface) { surfaceWrong() },
        createConvertToStepsAnswer(),
        AnswerItem(R.string.quest_smoothness_obstacle) { showObstacleHint() }
    )

    @Composable override fun BoxScope.ItemContent(item: Smoothness) {
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
    }

    override fun onClickOk(selectedItems: List<Smoothness>) {
        applyAnswer(SmoothnessValueAnswer(selectedItems.single()))
    }

    private fun showObstacleHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_smoothness_obstacle_hint)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    private fun surfaceWrong() {
        val surfaceType = surfaceTag?.let { parseSurface(it) } ?: return
        showWrongSurfaceDialog(surfaceType)
    }

    private fun showWrongSurfaceDialog(surface: Surface) {
        val dialogBinding = ComposeViewBinding.inflate(layoutInflater)
        dialogBinding.composeViewBase.content { Surface(Modifier.padding(24.dp)) {
            Column {
                Text(stringResource(Res.string.quest_smoothness_surface_value))
                ImageWithLabel(
                    painter = surface.icon?.let { painterResource(it) },
                    label = stringResource(surface.title),
                )
                Text(stringResource(Res.string.quest_address_answer_no_housenumber_message2b))
            }
        } }

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes_leave_note) { _, _ -> composeNote() }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(WrongSurfaceAnswer) }
            .show()
    }

    private fun createConvertToStepsAnswer(): AnswerItem? =
        if (element.couldBeSteps()) {
            AnswerItem(R.string.quest_generic_answer_is_actually_steps) {
                applyAnswer(IsActuallyStepsAnswer)
            }
        } else {
            null
        }
}
