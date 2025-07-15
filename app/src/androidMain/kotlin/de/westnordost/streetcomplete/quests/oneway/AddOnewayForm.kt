package de.westnordost.streetcomplete.quests.oneway

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconCell
import de.westnordost.streetcomplete.util.math.getOrientationAtCenterLineInDegrees

class AddOnewayForm : AImageListQuestForm<OnewayAnswer, OnewayAnswer>() {

    override val items get() =
        OnewayAnswer.entries.map { it.asItem(requireContext(), wayRotation - mapRotation) }

    override val itemsPerRow = 3

    private var mapRotation: Float = 0f
    private var wayRotation: Float = 0f

    override val itemContent =
        @Composable { item: ImageListItem<OnewayAnswer>, index: Int, onClick: () -> Unit, role: Role ->
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
        currentItems.value = OnewayAnswer.entries.mapIndexed { index, item -> ImageListItem(item.asItem(requireContext(), wayRotation - mapRotation), currentItems.value[index].checked) }
    }

    override fun onClickOk(selectedItems: List<OnewayAnswer>) {
        applyAnswer(selectedItems.first())
    }
}
