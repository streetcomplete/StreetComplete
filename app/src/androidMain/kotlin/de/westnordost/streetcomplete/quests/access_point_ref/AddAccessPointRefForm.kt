package de.westnordost.streetcomplete.quests.access_point_ref

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddAccessPointRefForm : AbstractOsmQuestForm<AccessPointRefAnswer>() {
    @Composable
    override fun Content() {
        var ref by rememberSaveable { mutableStateOf("") }
        var confirmAssemblyPoint by remember { mutableStateOf(false) }
        var confirmNoRef by remember { mutableStateOf(false) }

        QuestForm(
            answers = Confirm(
                isComplete = ref.isNotEmpty(),
                onClick = { applyAnswer(AccessPointRef(ref)) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_ref_answer_noRef)) {
                    confirmNoRef = true
                },
                Answer(stringResource(Res.string.quest_accessPointRef_answer_assembly_point)) {
                    confirmAssemblyPoint = true
                }
            )
        ) {
            TextField(
                value = ref,
                onValueChange = { ref = it },
                textStyle = MaterialTheme.typography.extraLargeInput,
            )
        }

        if (confirmAssemblyPoint) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmAssemblyPoint = false },
                onConfirmed = { applyAnswer(IsAssemblyPointAnswer) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stringResource(Res.string.quest_accessPointRef_detailed_answer_impossible_confirmation))
                        Image(
                            painter = painterResource(Res.drawable.assembly_point),
                            contentDescription = stringResource(Res.string.quest_accessPointRef_answer_assembly_point),
                        )
                    }
                }
            )
        }
        if (confirmNoRef) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoRef = false },
                onConfirmed = { applyAnswer(NoVisibleAccessPointRef) }
            )
        }
    }
}
