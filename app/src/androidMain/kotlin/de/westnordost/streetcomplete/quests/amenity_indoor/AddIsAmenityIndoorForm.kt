package de.westnordost.streetcomplete.quests.amenity_indoor

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.amenity_indoor.IsAmenityIndoorAnswer.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddIsAmenityIndoorForm(
    onAnswer: (IsAmenityIndoorAnswer) -> Unit,
){
    QuestForm(
        answers = listOf(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(OUTDOOR) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(INDOOR) }
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_isAmenityIndoor_outside_covered)) { onAnswer(COVERED) }
        )
    )
}
