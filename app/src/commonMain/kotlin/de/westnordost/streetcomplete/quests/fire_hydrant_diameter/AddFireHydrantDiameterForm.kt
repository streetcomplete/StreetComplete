package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddFireHydrantDiameterForm(
    on: (QuestAction<FireHydrantDiameterAnswer>) -> Unit,
    countryInfo: CountryInfo
) {
    var diameter by rememberSerializable { mutableStateOf<FireHydrantDiameter?>(null) }

    var confirmNoSign by remember { mutableStateOf(false) }
    var confirmUnusualInput by remember { mutableStateOf(false) }

    QuestForm(
        on = on,
        isComplete = diameter != null,
        onClickOk = {
            if (diameter!!.isUnusual()) {
                confirmUnusualInput = true
            } else {
                on(Answer(diameter!!))
            }
        },
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_generic_answer_noSign)) {
                confirmNoSign = true
            }
        ) }
    ) {
        HydrantDiameterForm(
            value = diameter,
            onValueChange = { diameter = it },
            countryCode = countryInfo.countryCode,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (confirmNoSign) {
        AreYouSureDialog(
            onDismissRequest = { confirmNoSign = false },
            onConfirmed = { on(Answer(FireHydrantDiameterAnswer.NoSign)) }
        )
    }
    if (confirmUnusualInput) {
        AreYouSureDialog(
            onDismissRequest = { confirmUnusualInput = false },
            onConfirmed = { on(Answer(diameter!!)) },
            text = {
                val range = diameter!!.unit.usualRange()
                Text(stringResource(Res.string.quest_fireHydrant_diameter_unusualInput_confirmation_description2, range.first, range.last))
            }
        )
    }
}
