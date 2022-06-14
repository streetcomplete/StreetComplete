package de.westnordost.streetcomplete.quests.camera_type

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.camera_type.CameraType.DOME
import de.westnordost.streetcomplete.quests.camera_type.CameraType.FIXED
import de.westnordost.streetcomplete.quests.camera_type.CameraType.PANNING
import de.westnordost.streetcomplete.view.image_select.Item

class AddCameraTypeForm : AImageListQuestForm<CameraType, CameraType>() {

    override val items = listOf(
        Item(DOME, R.drawable.ic_camera_type_dome, R.string.quest_camera_type_dome),
        Item(FIXED, R.drawable.ic_camera_type_fixed, R.string.quest_camera_type_fixed),
        Item(PANNING, R.drawable.ic_camera_type_panning, R.string.quest_camera_type_panning)
    )

    override val itemsPerRow = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<CameraType>) {
        applyAnswer(selectedItems.single())
    }
}
