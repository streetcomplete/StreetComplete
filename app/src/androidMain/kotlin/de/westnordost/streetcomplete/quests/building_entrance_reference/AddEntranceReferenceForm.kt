package de.westnordost.streetcomplete.quests.building_entrance_reference

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable

class AddEntranceReferenceForm : AbstractOsmQuestForm<EntranceReferenceAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_entrance_reference_nothing_signed) { onNothingSigned() },
    )

    private lateinit var entranceReference: MutableState<EntranceReference?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            entranceReference = rememberSerializable { mutableStateOf(lastEntranceReference?.clear()) }

            EntranceReferenceForm(
                value = entranceReference.value,
                onValueChange = {
                    entranceReference.value = it
                    checkIsFormComplete()
                },
            )
        } }
    }

    /* ----------------------------------- Commit answer ---------------------------------------- */

    private fun onNothingSigned() {
        applyAnswer(EntranceReferenceAnswer.NotSigned)
    }

    override fun onClickOk() {
        lastEntranceReference = entranceReference.value
        applyAnswer(entranceReference.value!!)
    }

    override fun isFormComplete(): Boolean =
        entranceReference.value?.isComplete() == true

    override fun isRejectingClose(): Boolean =
        entranceReference.value != null

    companion object {
        private var lastEntranceReference: EntranceReference? = null
    }
}
