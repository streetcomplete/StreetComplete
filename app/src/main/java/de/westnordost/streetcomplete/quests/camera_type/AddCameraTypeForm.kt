package de.westnordost.streetcomplete.quests.camera_type

import android.os.Bundle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconItem

class AddCameraTypeForm : AImageListQuestComposeForm<CameraType, CameraType>() {

    override val items = CameraType.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override val itemContent = @androidx.compose.runtime.Composable { item: ImageListItem<CameraType>, index: Int, onClick: () -> Unit, role: Role ->
        SelectableIconItem(
            item = item.item,
            isSelected = item.checked,
            onClick = onClick,
            role = role
        ) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<CameraType>) {
        applyAnswer(selectedItems.single())
    }
}
