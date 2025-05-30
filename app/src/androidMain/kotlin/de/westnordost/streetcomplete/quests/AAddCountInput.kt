package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.CountInput

abstract class AAddCountInput : AbstractOsmQuestForm<Int>() {

    abstract val iconId: Int

    abstract val initialCount: Int

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private lateinit var count: MutableState<Int>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.setContent {
            count = rememberSaveable { mutableIntStateOf(initialCount) }
            CountInput(
                count = count.value,
                onCountChange = {
                    count.value = it
                    checkIsFormComplete()
                },
                iconPainter = painterResource(iconId)
            )
        }
    }

    override fun isFormComplete() = count.value > 0

    override fun onClickOk() {
        applyAnswer(count.value)
    }
}
