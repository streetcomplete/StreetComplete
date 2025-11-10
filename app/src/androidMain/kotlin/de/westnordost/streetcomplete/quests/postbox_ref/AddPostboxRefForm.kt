package de.westnordost.streetcomplete.quests.postbox_ref

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.util.content

class AddPostboxRefForm : AbstractOsmQuestForm<PostboxRefAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_ref_answer_noRef) { confirmNoRef() }
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
        applyAnswer(PostboxRef(ref.value))
    }

    private fun confirmNoRef() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoVisiblePostboxRef) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = ref.value.isNotEmpty()
}
