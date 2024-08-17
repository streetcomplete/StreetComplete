package de.westnordost.streetcomplete.quests.service_building

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestForm

class AddServiceBuildingTypeForm : AGroupedImageListQuestForm<ServiceBuildingType, ServiceBuildingType>() {

    override val topItems = listOf(
        ServiceBuildingType.MINOR_SUBSTATION,
        ServiceBuildingType.GAS_PRESSURE_REGULATION,
        ServiceBuildingType.VENTILATION_SHAFT,
        ServiceBuildingType.WATER_WELL,
        ServiceBuildingType.HEATING,
    ).toItems()

    override val allItems = ServiceBuildingTypeCategory.values().toItems()

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.groupCellLayoutId = R.layout.cell_labeled_icon_select_with_description_group
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        element.tags["operator"]?.let { setTitle(resources.getString((questType as OsmElementQuestType<*>).getTitle(element.tags)) + " ($it)") }
    }

    override fun onClickOk(value: ServiceBuildingType) {
        applyAnswer(value)
    }
}
