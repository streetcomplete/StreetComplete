package de.westnordost.streetcomplete.quests.diet_type

import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddDietTypeForm : AbstractOsmQuestForm<DietAvailabilityAnswer>() {

    @Composable
    override fun Content() {
        var confirmNoFood by remember { mutableStateOf(false) }

        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(DIET_NO) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(DIET_YES) },
                Answer(stringResource(Res.string.quest_hasFeature_only)) { applyAnswer(DIET_ONLY) },
            ),
            otherAnswers = listOfNotNull(
                if (element.tags["amenity"] == "cafe") {
                    Answer(stringResource(Res.string.quest_diet_answer_no_food)) { confirmNoFood = true }
                } else {
                    null
                }
            )
        ) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                Text(stringResource(Res.string.quest_dietType_explanation))
            }
        }

        if (confirmNoFood) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoFood = false },
                onConfirmed = { applyAnswer(NoFood) }
            )
        }
    }
}
