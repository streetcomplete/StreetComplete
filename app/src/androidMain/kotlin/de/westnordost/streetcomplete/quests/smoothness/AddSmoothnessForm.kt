package de.westnordost.streetcomplete.quests.smoothness

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithDescription
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder
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
        val inflater = LayoutInflater.from(requireContext())
        val inner = inflater.inflate(R.layout.dialog_quest_smoothness_wrong_surface, null, false)
        ItemViewHolder(inner.findViewById(R.id.item_view)).bind(surface.asItem())

        AlertDialog.Builder(requireContext())
            .setView(inner)
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
