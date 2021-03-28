package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.TextChangedWatcher
import kotlinx.android.synthetic.main.quest_name_suggestion.*

abstract class ANameWithSuggestionsForm<T> : AbstractQuestFormAnswerFragment<T>() {

    override val contentLayoutResId = R.layout.quest_name_suggestion

    protected val name get() = nameInput?.text?.toString().orEmpty().trim()

    abstract val suggestions: List<String>?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suggestions?.let {
            nameInput.setAdapter(ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, it
            ))
        }

        nameInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override fun isFormComplete() = name.isNotEmpty()
}
