package de.westnordost.streetcomplete.quests.building_type

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.view.Item

import de.westnordost.streetcomplete.quests.building_type.BuildingType.*

class AddBuildingTypeForm : GroupedImageListQuestAnswerFragment() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_buildingType_answer_multiple_types) { showMultipleTypesHint() },
        OtherAnswer(R.string.quest_buildingType_answer_construction_site) { applyConstructionSiteAnswer() }
    )

    override val topItems = listOf(
        DETACHED, APARTMENTS, HOUSE, GARAGE, SHED, HUT
    )

    override val allItems = listOf(
        Item(RESIDENTIAL, listOf(
            DETACHED, APARTMENTS, SEMI_DETACHED, TERRACE, FARM, HOUSE,
            HUT, BUNGALOW, HOUSEBOAT, STATIC_CARAVAN, DORMITORY
        )),
        Item(COMMERCIAL, listOf(
            OFFICE, INDUSTRIAL, RETAIL, WAREHOUSE, KIOSK, HOTEL, STORAGE_TANK
        )),
        Item(CIVIC, listOf(
            SCHOOL, UNIVERSITY, HOSPITAL, KINDERGARTEN, SPORTS_CENTRE, TRAIN_STATION,
            TRANSPORTATION, COLLEGE, GOVERNMENT, STADIUM
        )),
        Item(RELIGIOUS, listOf(
            CHURCH, CATHEDRAL, CHAPEL, MOSQUE, TEMPLE, PAGODA, SYNAGOGUE, SHRINE
        )),
        Item(null, R.drawable.ic_building_car, R.string.quest_buildingType_cars, 0, listOf(
            GARAGE, GARAGES, CARPORT, PARKING
        )),
        Item(null, R.drawable.ic_building_farm, R.string.quest_buildingType_farm, 0, listOf(
            FARM, FARM_AUXILIARY, GREENHOUSE, STORAGE_TANK
        )),
        Item(null, R.drawable.ic_building_other, R.string.quest_buildingType_other, 0, listOf(
            SHED, ROOF, SERVICE, HUT, TOILETS, HANGAR, BUNKER
        ))
    )

    override val itemsPerRow = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.groupCellLayoutId = R.layout.cell_labeled_icon_select_with_description_group
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_with_description
    }

    override fun applyAnswer(value: String) {
        val answer = Bundle()
        if (value.startsWith("man_made=")) {
            val manMade = value.split("=")[1]
            answer.putString(MAN_MADE, manMade)
        } else {
            answer.putString(BUILDING, value)
        }
        applyAnswer(answer)
    }

    private fun applyConstructionSiteAnswer() {
        applyAnswer(bundleOf(BUILDING to "construction"))
    }

    private fun showMultipleTypesHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_buildingType_answer_multiple_types_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    companion object {
        const val BUILDING = "building"
        const val MAN_MADE = "man_made"
    }
}
