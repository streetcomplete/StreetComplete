package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.length.Length
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddMaxHeightForm(
    on: (QuestAction<MaxHeightAnswer>) -> Unit,
    element: Element,
    countryInfo: CountryInfo
) {
    var height by rememberSerializable { mutableStateOf<Length?>(null) }

    var confirmUnusualInput by remember { mutableStateOf(false) }
    var confirmNoSign by remember { mutableStateOf(false) }

    QuestForm(
        on = on,
        isComplete = height != null,
        onClickOk = {
            if (isUnrealisticHeight(height!!)) {
                confirmUnusualInput = true
            } else {
                on(Answer(MaxHeight(height!!)))
            }
        },
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_maxheight_answer_noSign)) {
                confirmNoSign = true
            }
        ) },
        hintText =
            if (element.type == ElementType.WAY) {
                stringResource(Res.string.quest_maxheight_split_way_hint,
                    stringResource(Res.string.quest_generic_answer_differs_along_the_way)
                )
            } else {
                null
            },
    ) {
        MaxHeightForm(
            length = height,
            selectableUnits = countryInfo.lengthUnits,
            onChange = { height = it },
            locale = countryInfo.languageTag?.let { Locale(it) } ?: Locale.current,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (confirmNoSign) {
        AreYouSureDialog(
            onDismissRequest = { confirmNoSign = false },
            onConfirmed = { on(Answer(NoMaxHeightSign)) }
        )
    }
    if (confirmUnusualInput) {
        AreYouSureDialog(
            onDismissRequest = { confirmUnusualInput = false },
            onConfirmed = { on(Answer(MaxHeight(height!!))) },
            text = { Text(stringResource(Res.string.quest_maxheight_unusualInput_confirmation_description)) }
        )
    }
}

private fun isUnrealisticHeight(length: Length): Boolean {
    val m = length.toMeters()
    return m > 6 || m < 1.8
}
