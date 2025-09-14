package de.westnordost.streetcomplete.quests.camera_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCameraTypeForm : AItemSelectQuestForm<CameraType, CameraType>() {

    override val items = CameraType.entries
    override val itemsPerRow = 3

    @Composable override fun ItemContent(item: CameraType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: CameraType) {
        applyAnswer(selectedItem)
    }
}
