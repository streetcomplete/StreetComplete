package de.westnordost.streetcomplete.quests.building_type

import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.quests.AGroupedItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBuildingTypeForm : AGroupedItemSelectQuestForm<BuildingTypeCategory, BuildingType, BuildingType>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_buildingType_answer_multiple_types) { showMultipleTypesHint() },
        AnswerItem(R.string.quest_buildingType_answer_construction_site) { applyAnswer(BuildingType.CONSTRUCTION) }
    )

    override val topItems = BuildingType.topSelectableValues

    override val groups = BuildingTypeCategory.entries

    @Composable override fun GroupContent(item: BuildingTypeCategory) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = item.description?.let { stringResource(it) }
        )
    }

    @Composable override fun ItemContent(item: BuildingType) {
        ImageWithDescription(
            painter = painterResource(item.icon),
            title = stringResource(item.title),
            description = item.description?.let { stringResource(it) }
        )
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
