package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.MaxHeightForm
import de.westnordost.streetcomplete.ui.util.content

class AddMaxHeightForm : AbstractOsmQuestForm<MaxHeightAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    private lateinit var height: MutableState<Length?>

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxheight_answer_noSign) { confirmNoSign() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content {
            height = remember { mutableStateOf(null) }

            MaxHeightForm(
                selectableUnits = countryInfo.lengthUnits,
                onLengthChanged = {
                    height.value = it
                    checkIsFormComplete()
                },
                maxFeetDigits = 2,
                maxMeterDigits = Pair(2, 2),
                countryInfo = countryInfo
            )
        }

        if (element.type == ElementType.WAY) {
            setHint(
                getString(
                    R.string.quest_maxheight_split_way_hint,
                    getString(R.string.quest_generic_answer_differs_along_the_way)
                )
            )
        }
    }

    override fun isFormComplete() = true

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
