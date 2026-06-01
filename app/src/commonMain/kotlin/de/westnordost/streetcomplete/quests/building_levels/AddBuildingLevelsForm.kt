package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddBuildingLevelsForm(
    on: (QuestAction<BuildingLevels>) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
    preferences: Preferences = koinInject(),
) {
    val key = "AddBuildingLevelsForm"
    val lastPicked = remember {
        preferences.getLastPicked<BuildingLevels>(key)
            .takeFavorites(n = 5, history = 15, first = 1)
            .sortedWith(compareBy<BuildingLevels> { it.levels }.thenBy { it.roofLevels })
    }

    var levels by rememberSaveable {
        mutableStateOf(element.tags["building:levels"]?.toIntOrNull()?.takeIf { it >= 0 })
    }
    var roofLevels by rememberSaveable {
        mutableStateOf(element.tags["roof:levels"]?.toIntOrNull()?.takeIf { it >= 0 })
    }
    var showMultipleLevelsHint by remember { mutableStateOf(false) }

    val roofLevelsAreOptional = remember {
        val roofShape = element.tags["roof:shape"]
        val hasNonFlatRoofShape = roofShape != null && roofShape != "flat"
        countryInfo.roofsAreUsuallyFlat && !hasNonFlatRoofShape
    }

    val isBuildingPart = element.tags.containsKey("building:part")

    QuestForm(
        title = stringResource(
            if (isBuildingPart) Res.string.quest_buildingLevels_title_buildingPart2
            else Res.string.quest_buildingLevels_title2
        ),
        isComplete = levels != null && (roofLevelsAreOptional || roofLevels != null),
        hasChanges = levels != null || roofLevels != null,
        onClickOk = {
            val answer = BuildingLevels(levels!!, roofLevels)
            preferences.addLastPicked(key, answer)
            on(Answer(answer))
        },
        on = on,
        otherAnswers = listOf(
            AnswerItem(stringResource(Res.string.quest_buildingLevels_answer_multipleLevels)) { showMultipleLevelsHint = true }
        ),
    ) {
        BuildingLevelsForm(
            levels = levels,
            onLevelsChange = { levels = it },
            roofLevels = roofLevels,
            onRoofLevelsChange = { roofLevels = it },
            previousBuildingLevels = lastPicked
        )
    }

    if (showMultipleLevelsHint) {
        InfoDialog(
            onDismissRequest = { showMultipleLevelsHint = false },
            text = { Text(stringResource(Res.string.quest_buildingLevels_answer_description)) }
        )
    }
}
