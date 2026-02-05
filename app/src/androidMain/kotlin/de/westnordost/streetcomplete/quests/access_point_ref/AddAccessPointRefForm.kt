package de.westnordost.streetcomplete.quests.access_point_ref

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.assembly_point
import de.westnordost.streetcomplete.resources.quest_accessPointRef_answer_assembly_point
import de.westnordost.streetcomplete.resources.quest_accessPointRef_detailed_answer_impossible_confirmation
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddAccessPointRefForm : AbstractOsmQuestForm<AccessPointRefAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_ref_answer_noRef) { confirmNoRef() },
        AnswerItem(R.string.quest_accessPointRef_answer_assembly_point) { confirmAssemblyPoint() }
    )

    private val ref: MutableState<String> = mutableStateOf("")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content { Surface {
            TextField(
                value = ref.value,
                onValueChange = {
                    ref.value = it
                    checkIsFormComplete()
                },
                textStyle = MaterialTheme.typography.extraLargeInput,
            )
        } }
    }

    override fun onClickOk() {
        applyAnswer(AccessPointRef(ref.value))
    }

    private fun confirmNoRef() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoVisibleAccessPointRef) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    private fun confirmAssemblyPoint() {
        val dialogBinding = ComposeViewBinding.inflate(layoutInflater)
        dialogBinding.composeViewBase.content { Surface(Modifier.padding(24.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_accessPointRef_detailed_answer_impossible_confirmation))
                }
                Image(
                    painter = painterResource(Res.drawable.assembly_point),
                    contentDescription = stringResource(Res.string.quest_accessPointRef_answer_assembly_point),
                    modifier = Modifier.size(200.dp),
                )
            }
        } }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IsAssemblyPointAnswer) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = ref.value.isNotEmpty()
}
