package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameter.Unit.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Form
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddFireHydrantDiameterForm(
    onAnswer: (FireHydrantDiameterAnswer) -> Unit
) {
    var diameter by rememberSaveable() { mutableStateOf<Int?>(null) }

    var confirmNoSign by remember { mutableStateOf(false) }
    var confirmUnusualInput by remember { mutableStateOf(false) }

    fun createAnswer(diameter: Int): FireHydrantDiameter {
        val unit = if (countryInfo.countryCode == "GB" && diameter <= 25) Inch else Millimeter
        return FireHydrantDiameter(diameter, unit)
    }

    QuestForm(
        answers = Form(
            isComplete = diameter != null,
            onClickOk = {
                val answer = createAnswer(diameter!!)
                if (answer.isUnusual()) {
                    confirmUnusualInput = true
                } else {
                    onAnswer(answer)
                }
            }
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_generic_answer_noSign)) { confirmNoSign = true }
        )
    ) {
        HydrantDiameterForm(
            value = diameter,
            onValueChange = { diameter = it },
            countryCode = countryInfo.countryCode,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (confirmNoSign) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNoSign = false },
            onConfirmed = { onAnswer(FireHydrantDiameterAnswer.NoSign) }
        )
    }
    if (confirmUnusualInput) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmUnusualInput = false },
            onConfirmed = { onAnswer(createAnswer(diameter!!)) },
            text = {
                val range = onAnswer(diameter!!).unit.usualRange()
                Text(stringResource(Res.string.quest_fireHydrant_diameter_unusualInput_confirmation_description2, range.first, range.last))
            }
        )
    }
}
