package de.westnordost.streetcomplete.quests.contact

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.core.widget.doAfterTextChanged

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestContactBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm

class AddContactWebsiteForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_contact
    private val binding by contentViewBinding(QuestContactBinding::bind)

    private val contact get() = binding.nameInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI

        binding.nameInput.setText(prefill)
        binding.nameInput.doAfterTextChanged {
            val s = binding.nameInput.selectionStart
            if (binding.nameInput.text.toString().any { it.isUpperCase() }) {
                binding.nameInput.setText(binding.nameInput.text.toString().lowercase())
                binding.nameInput.setSelection(s)
            }
            checkIsFormComplete()
        }
    }

    override fun onClickOk() {
        prefill = if (contact.contains("//"))
            contact.substringBefore("//") + "//"
        else
            ""
        applyAnswer(contact)
    }


    override fun isFormComplete() = contact.isNotEmpty() && contact != prefill && contact.contains('.')

    companion object {
        private var prefill = "http://"
    }

}
