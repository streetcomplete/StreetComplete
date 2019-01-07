package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import android.widget.EditText

import de.westnordost.streetcomplete.util.TextChangedWatcher

abstract class TextInputQuestAnswerFragment : AbstractQuestFormAnswerFragment() {

    private val inputString: String get() = editText.text.toString()

    protected abstract val editText: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editText.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override fun onClickOk() {
        val answer = Bundle()
        answer.putString(INPUT, inputString)
        applyAnswer(answer)
    }

    override fun isFormComplete() = inputString.isNotEmpty()

    companion object {
        const val INPUT = "input"
    }
}
