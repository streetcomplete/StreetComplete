package de.westnordost.streetcomplete.quests.building_levels

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Form
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddBuildingLevelsForm : AbstractOsmQuestForm<BuildingLevels>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val key = "AddBuildingLevelsForm"
        val lastPicked = remember {
            prefs.getLastPicked<BuildingLevels>(key)
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

        QuestForm(
            answers = Form(
                isComplete = levels != null && (roofLevelsAreOptional || roofLevels != null),
                hasChanges = levels != null || roofLevels != null,
                onClickOk = {
                    val answer = BuildingLevels(levels!!, roofLevels)
                    prefs.addLastPicked(key, answer)
                    applyAnswer(answer)
                }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_buildingLevels_answer_multipleLevels)) { showMultipleLevelsHint = true }
            )
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
}
