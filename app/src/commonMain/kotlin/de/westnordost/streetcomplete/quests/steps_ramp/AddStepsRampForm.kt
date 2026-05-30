package de.westnordost.streetcomplete.quests.steps_ramp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.*
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.STROLLER
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.WHEELCHAIR
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddStepsRampForm(
    onAnswer: (QuestAnswer<StepsRampAnswer>) -> Unit
) {
    val items = StepsRamp.entries
    var selectedItems by rememberSerializable { mutableStateOf<Set<StepsRamp>>(emptySet()) }

    var confirmWheelchairRampIsSeparate by remember { mutableStateOf(false) }

    QuestForm(
        isComplete = selectedItems.isNotEmpty(),
        onClickOk = {
            if (selectedItems.contains(WHEELCHAIR)) {
                confirmWheelchairRampIsSeparate = true
            } else {
                onAnswer(Answer(
                    StepsRampAnswer(
                        bicycleRamp = selectedItems.contains(BICYCLE),
                        strollerRamp = selectedItems.contains(STROLLER),
                        wheelchairRamp = WheelchairRampStatus.NO
                    )
                ))
            }
        },
        onAnswer = onAnswer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                Text(stringResource(Res.string.quest_multiselect_hint))
            }
            ItemsSelectGrid(
                columns = SimpleGridCells.Fixed(2),
                items = StepsRamp.entries,
                selectedItems = selectedItems,
                onSelect = { item, selected ->
                    // "no ramp" is exclusive to the other options
                    selectedItems = if (selected) {
                        if (item == StepsRamp.NONE) setOf(item)
                        else selectedItems + item - StepsRamp.NONE
                    } else {
                        selectedItems - item
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                itemContent = {
                    ImageWithLabel(painterResource(it.icon), stringResource(it.title))
                }
            )
        }
    }

    if (confirmWheelchairRampIsSeparate) {
        WheelchairRampIsSeparateDialog(
            onDismissRequest = { confirmWheelchairRampIsSeparate = false },
            onAnswer = { isSeparate ->
                onAnswer(Answer(
                    StepsRampAnswer(
                        bicycleRamp = selectedItems.contains(BICYCLE),
                        strollerRamp = selectedItems.contains(STROLLER),
                        wheelchairRamp =
                            if (isSeparate) WheelchairRampStatus.SEPARATE
                            else WheelchairRampStatus.YES
                    )
                ))
            }
        )
    }
}

@Composable
private fun WheelchairRampIsSeparateDialog(
    onDismissRequest: () -> Unit,
    onAnswer: (isSeparate: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.quest_generic_confirmation_no))
            }
            TextButton(onClick = { onDismissRequest(); onAnswer(false) }) {
                Text(stringResource(Res.string.quest_steps_ramp_separate_wheelchair_decline))
            }
            TextButton(onClick = { onDismissRequest(); onAnswer(true) }) {
                Text(stringResource(Res.string.quest_steps_ramp_separate_wheelchair_confirm))
            }
        },
        modifier = modifier,
        text = { Text(stringResource(Res.string.quest_steps_ramp_separate_wheelchair)) }
    )
}
