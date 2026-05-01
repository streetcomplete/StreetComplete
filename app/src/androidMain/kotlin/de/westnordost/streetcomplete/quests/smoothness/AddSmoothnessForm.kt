package de.westnordost.streetcomplete.quests.smoothness

import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.util.content
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
        val items = remember {
            Smoothness.entries.filter { it.getImage(surfaceTag) != null }
        }
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
            serializer = serializer(),
            favoriteKey = "AddSmoothnessForm",
            moveFavoritesToFront = false,
            otherAnswers = buildList {
                add(Answer(stringResource(Res.string.quest_smoothness_wrong_surface)) { surfaceWrong() })
                if (element.couldBeSteps()) {
                    add(Answer(stringResource(Res.string.quest_generic_answer_is_actually_steps)) {
                        applyAnswer(IsActuallyStepsAnswer)
                    })
                }
                add(Answer(stringResource(Res.string.quest_smoothness_obstacle)) { showObstacleHint() })
            }
        )
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
        } }

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes_leave_note) { _, _ -> composeNote() }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(WrongSurfaceAnswer) }
            .show()
    }
}
