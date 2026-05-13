package de.westnordost.streetcomplete.quests.sport

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.quests.sport.Sport.MULTI
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddSportForm(
    onAnswer: (Set<Sport>) -> Unit
) {
    val items = remember {
        val order = countryInfo.popularSports
            .withIndex()
            .associate { it.value to it.index }
        (Sport.entries - MULTI).sortedBy { order[it.osmValue] ?: Integer.MAX_VALUE }
    }
    var confirmManySports by remember { mutableStateOf<Set<Sport>?>(null) }

    ItemsSelectQuestForm(
        items = items,
        itemsPerRow = 4,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        onClickOk = { selectedItems ->
            if (selectedItems.size > 3) {
                confirmManySports = selectedItems
            } else {
                onAnswer(selectedItems)
            }
        },
        favoriteKey = "AddSportForm",
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_sport_answer_multi)) {
                onAnswer(setOf(MULTI))
            }
        )
    )

    confirmManySports?.let { sports ->
        ConfirmManySportsDialog(
            onDismissRequest = { confirmManySports = null },
            onSpecificSports = { onAnswer(sports) },
            onGeneralPurpose = { onAnswer(setOf(MULTI)) },
            sports = sports
        )
    }
}

@Composable
private fun ConfirmManySportsDialog(
    onDismissRequest: () -> Unit,
    onSpecificSports: () -> Unit,
    onGeneralPurpose: () -> Unit,
    sports: Collection<Sport>,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            TextButton(onClick = {
                onGeneralPurpose()
                onDismissRequest()
            }) {
                Text(stringResource(Res.string.quest_manySports_confirmation_generic))
            }
            TextButton(onClick = {
                onSpecificSports()
                onDismissRequest()
            }) {
                Text(stringResource(Res.string.quest_manySports_confirmation_specific))
            }
        },
        title = { Text(stringResource(Res.string.quest_sport_manySports_confirmation_title)) },
        text = { Text(stringResource(Res.string.quest_sport_manySports_confirmation_description)) },
        modifier = modifier
    )
}

