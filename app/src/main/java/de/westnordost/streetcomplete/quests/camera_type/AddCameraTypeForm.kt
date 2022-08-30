package de.westnordost.streetcomplete.quests.camera_type

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddCameraTypeForm : AImageListQuestForm<CameraType, CameraType>() {

    override val items = CameraType.values().map { it.asItem() }
    override val itemsPerRow = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<CameraType>) {
        applyAnswer(selectedItems.single())
    }
}
