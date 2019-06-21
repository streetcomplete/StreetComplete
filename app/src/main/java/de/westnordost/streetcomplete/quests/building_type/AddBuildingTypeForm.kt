package de.westnordost.streetcomplete.quests.building_type

import android.os.Bundle
import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.building_type.BuildingType.*

class AddBuildingTypeForm : AGroupedImageListQuestAnswerFragment<String,String>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_buildingType_answer_multiple_types) { showMultipleTypesHint() },
        OtherAnswer(R.string.quest_buildingType_answer_construction_site) { applyAnswer("construction") }
    )

    override val topItems = listOf(
        DETACHED, APARTMENTS, HOUSE, GARAGE, SHED, HUT
    ).toItems()

    override val allItems = listOf(
        RESIDENTIAL, COMMERCIAL, CIVIC, RELIGIOUS, FOR_CARS, FOR_FARMS, OTHER
    ).toItems()

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.groupCellLayoutId = R.layout.cell_labeled_icon_select_with_description_group
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun onClickOk(value: String) {
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
