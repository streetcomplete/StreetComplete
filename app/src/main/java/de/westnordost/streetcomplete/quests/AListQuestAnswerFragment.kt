package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import android.view.View.generateViewId
import android.widget.RadioButton
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestGenericRadioListBinding


abstract class AListQuestAnswerFragment<T> : AbstractQuestFormAnswerFragment<T>() {

    final override val contentLayoutResId = R.layout.quest_generic_radio_list
    private val binding by contentViewBinding(QuestGenericRadioListBinding::bind)

    override val defaultExpanded = false

    protected abstract val items: List<TextItem<T>>

    private val radioButtonIds = HashMap<Int, TextItem<T>>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (item in items) {
            val viewId = generateViewId()
            radioButtonIds[viewId] = item

            val radioButton = RadioButton(view.context)
            radioButton.setText(item.titleId)
            radioButton.id = viewId
            binding.radioButtonGroup.addView(radioButton)
        }
        binding.radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(radioButtonIds.getValue(binding.radioButtonGroup.checkedRadioButtonId).value)
    }

    override fun isFormComplete() = binding.radioButtonGroup.checkedRadioButtonId != -1
}

data class TextItem<T>(val value: T, val titleId: Int)
