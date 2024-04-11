package de.westnordost.streetcomplete.quests.building_type

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.toItems
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddBuildingTypeForm : AGroupedImageListQuestForm<BuildingType, BuildingType>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_buildingType_answer_multiple_types) { showMultipleTypesHint() },
        AnswerItem(R.string.quest_buildingType_answer_construction_site) { applyAnswer(BuildingType.CONSTRUCTION) }
    )

    override val topItems = BuildingType.topSelectableValues.toItems()

    override val allItems = BuildingTypeCategory.entries.toItems()

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.groupCellLayoutId = R.layout.cell_labeled_icon_select_with_description_group
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onClickOk(value: BuildingType) {
        applyAnswer(value)
    }

    private fun showMultipleTypesHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_buildingType_answer_multiple_types_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }
}
