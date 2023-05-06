package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestNameSuggestionBinding
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

abstract class ANameWithSuggestionsForm<T> : AbstractOsmQuestForm<T>() {

    final override val contentLayoutResId = R.layout.quest_name_suggestion
    private val binding by contentViewBinding(QuestNameSuggestionBinding::bind)

    protected val name get() = binding.nameInput.nonBlankTextOrNull

    abstract val suggestions: List<String>?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suggestions?.let {
            binding.nameInput.setAdapter(ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, it
            ))
        }

        binding.nameInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun isFormComplete() = name != null
}
