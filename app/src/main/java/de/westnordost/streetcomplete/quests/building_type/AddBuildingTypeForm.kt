package de.westnordost.streetcomplete.quests.building_type

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class AddBuildingTypeForm : AGroupedImageListQuestAnswerFragment<BuildingType?, BuildingType>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_buildingType_answer_multiple_types) { showMultipleTypesHint() },
        AnswerItem(R.string.quest_buildingType_answer_construction_site) { applyAnswer(BuildingType.CONSTRUCTION) }
    )

    override val topItems = topBuildingTypes.toItems()

    override val allItems = buildingCategories.toItems()

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.groupCellLayoutId = R.layout.cell_labeled_icon_select_with_description_group
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onClickOk(value: BuildingType?) {
        // we can be sure that `value` is not null here because
        // AGroupedImageListQuestAnswerFragment.onClickOk checks for that case
        applyAnswer(value!!)
    }

    private fun showMultipleTypesHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_buildingType_answer_multiple_types_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }
}
