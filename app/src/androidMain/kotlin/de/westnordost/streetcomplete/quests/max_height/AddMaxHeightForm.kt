package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable

class AddMaxHeightForm : AbstractOsmQuestForm<MaxHeightAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    private lateinit var height: MutableState<Length?>

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxheight_answer_noSign) { confirmNoSign() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (element.type == ElementType.WAY) {
            setHint(
                getString(
                    R.string.quest_maxheight_split_way_hint,
                    getString(R.string.quest_generic_answer_differs_along_the_way)
                )
            )
        }

        binding.composeViewBase.content { Surface {
            height = rememberSerializable { mutableStateOf(null) }

            MaxHeightForm(
                length = height.value,
                selectableUnits = countryInfo.lengthUnits,
                onChange = {
                    height.value = it
                    checkIsFormComplete()
                },
                countryCode = countryInfo.countryCode,
                modifier = Modifier.fillMaxWidth()
            )
        } }
    }

    override fun isFormComplete() = height.value != null

    override fun onClickOk() {
        if (userSelectedUnrealisticHeight()) {
            confirmUnusualInput { applyMaxHeightFormAnswer() }
        } else {
            applyMaxHeightFormAnswer()
        }
    }

    private fun userSelectedUnrealisticHeight(): Boolean {
        val m = height.value?.toMeters() ?: return false
        return m > 6 || m < 1.8
    }

    private fun applyMaxHeightFormAnswer() {
        applyAnswer(MaxHeight(height.value!!))
    }

    private fun confirmNoSign() {
        activity?.let {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_generic_confirmation_title)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    applyAnswer(
                        NoMaxHeightSign
                    )
                }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }

    private fun confirmUnusualInput(callback: () -> (Unit)) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.quest_generic_confirmation_title)
                .setMessage(R.string.quest_maxheight_unusualInput_confirmation_description)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }
}
