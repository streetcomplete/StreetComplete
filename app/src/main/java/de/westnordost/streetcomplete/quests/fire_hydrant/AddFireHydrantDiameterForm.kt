package de.westnordost.streetcomplete.quests.fire_hydrant

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFireHydrantDiameterBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddFireHydrantDiameterForm : AbstractQuestFormAnswerFragment<Int>() {

    override val contentLayoutResId = R.layout.quest_fire_hydrant_diameter
    private val binding by contentViewBinding(QuestFireHydrantDiameterBinding::bind)

    private val diameter get() = binding.diameterInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.diameterInput.addTextChangedListener(TextChangedWatcher {
            checkIsFormComplete()
        })
    }

    override fun isFormComplete() = diameter.isNotEmpty() && diameter.toInt() > 0

    override fun onClickOk() {
        applyAnswer(diameter.toInt())
    }
}
