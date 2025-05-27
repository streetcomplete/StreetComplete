package de.westnordost.streetcomplete.quests.step_count

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.CountForm

class AddStepCountForm : AbstractOsmQuestForm<Int>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private lateinit var count: MutableState<Int>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.setContent {
            count = rememberSaveable { mutableIntStateOf(element.tags["step_count"]?.toIntOrNull() ?: 0) }
            CountForm(count = count.value, onCountChange = {
                count.value = it
                checkIsFormComplete()
            }, iconPainter = painterResource(R.drawable.ic_step))
        }
    }

    override fun isFormComplete() = count.value > 0

    override fun onClickOk() {
        applyAnswer(count.value)
    }
}
