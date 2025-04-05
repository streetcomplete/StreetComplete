package de.westnordost.streetcomplete.quests.building_material

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddBuildingMaterialForm : AImageListQuestForm<BuildingMaterial, BuildingMaterial>() {

    override val items = BuildingMaterial.entries.map { it.asItem() }

    override val itemsPerRow = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select
    }

    override fun onClickOk(selectedItems: List<BuildingMaterial>) {
        applyAnswer(selectedItems.single())
    }
}
