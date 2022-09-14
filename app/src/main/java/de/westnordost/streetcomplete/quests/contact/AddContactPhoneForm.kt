package de.westnordost.streetcomplete.quests.contact

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.core.widget.doAfterTextChanged

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestContactBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm

class AddContactPhoneForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_contact
    private val binding by contentViewBinding(QuestContactBinding::bind)

    private val contact get() = binding.nameInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameInput.inputType = InputType.TYPE_CLASS_PHONE

        binding.nameInput.setText(prefill)
        binding.nameInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun onClickOk() {
        prefill = if (contact.contains(" ") && contact.substringBefore(" ").length <= 5)
                contact.substringBefore(" ") + " "
            else
                "+"
        applyAnswer(contact)
    }


    override fun isFormComplete() = contact.isNotEmpty() && binding.nameInput.text?.toString().orEmpty() != prefill

    companion object {
        private var prefill = "+"
    }
}
