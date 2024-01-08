package de.westnordost.streetcomplete.quests.defibrillator

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLocationBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddDefibrillatorLocationForm : AbstractOsmQuestForm<String>() {

    override val contentLayoutResId = R.layout.quest_location
    private val binding by contentViewBinding(QuestLocationBinding::bind)

    private val location get() = binding.locationInput.nonBlankTextOrNull

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.locationInput.doAfterTextChanged { checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(location!!)
    }

    override fun isFormComplete() = location != null
}
