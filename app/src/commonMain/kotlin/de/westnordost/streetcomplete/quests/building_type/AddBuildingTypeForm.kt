package de.westnordost.streetcomplete.quests.building_type

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.GroupedItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBuildingTypeForm(
    on: (QuestAction<BuildingType>) -> Unit
) {
    var showMultipleTypesHint by remember { mutableStateOf(false) }

    GroupedItemSelectQuestForm(
        groups = BuildingTypeCategory.entries,
        topItems = BuildingType.topSelectableValues,
        groupContent = { group ->
            ImageWithDescription(
                painter = painterResource(group.icon),
                title = stringResource(group.title),
                description = group.description?.let { stringResource(it) },
                imageSize = DpSize(48.dp, 48.dp)
            )
        },
        itemContent = { item ->
            ImageWithDescription(
                painter = painterResource(item.icon),
                title = stringResource(item.title),
                description = item.description?.let { stringResource(it) },
                imageSize = DpSize(48.dp, 48.dp),
            )
        },
        on = on,
        favoriteKey = "AddBuildingTypeForm",
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_buildingType_answer_multiple_types)) {
                showMultipleTypesHint = true
            },
            AnswerItem(stringResource(Res.string.quest_buildingType_answer_construction_site)) {
                on(Answer(BuildingType.CONSTRUCTION))
            }
        ) }
    )

    if (showMultipleTypesHint) {
        InfoDialog(
            onDismissRequest = { showMultipleTypesHint = false },
            text = { Text(stringResource(Res.string.quest_buildingType_answer_multiple_types_description)) }
        )
    }
}
