package de.westnordost.streetcomplete.quests.incline_direction

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconCell
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees

class AddBicycleInclineForm : AImageListQuestForm<Incline, BicycleInclineAnswer>() {
    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bicycle_incline_up_and_down) { confirmUpAndDown() }
    )

    override val items get() =
        Incline.entries.map { it.asItem(requireContext(), wayRotation - mapRotation) }

    override val itemsPerRow = 2

    private var mapRotation: Float = 0f
    private var wayRotation: Float = 0f

    override val itemContent =
        @Composable { item: ImageListItem<Incline>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item to (wayRotation - mapRotation)) {
                SelectableIconCell(
                    item = item.item,
                    isSelected = item.checked,
                    onClick = onClick,
                    role = role
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wayRotation = (geometry as ElementPolylinesGeometry).getOrientationAtCenterLineInDegrees()
    }

    override fun onMapOrientation(rotation: Double, tilt: Double) {
        mapRotation = rotation.toFloat()
        currentItems.value = Incline.entries.mapIndexed { index, item -> ImageListItem(item.asItem(requireContext(), wayRotation - mapRotation), currentItems.value[index].checked) }
    }

    private fun confirmUpAndDown() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(
                UpdAndDownHopsAnswer) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun onClickOk(selectedItems: List<Incline>) {
        applyAnswer(RegularBicycleInclineAnswer(selectedItems.first()))
    }
}
