package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.CountInput

abstract class AddCountInput : AbstractOsmQuestForm<Int>() {

    abstract val iconId: Int

    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private lateinit var capacity: MutableState<Int>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.setContent {
            capacity = rememberSaveable { mutableIntStateOf(element.tags["capacity"]?.toIntOrNull() ?: 0) }
            CountInput(
                count = capacity.value,
                onCountChange = {
                    capacity.value = it
                    checkIsFormComplete()
                },
                iconPainter = painterResource(iconId)
            )
        }
    }

    override fun isFormComplete() = capacity.value > 0

    override fun onClickOk() {
        applyAnswer(capacity.value)
    }
}
