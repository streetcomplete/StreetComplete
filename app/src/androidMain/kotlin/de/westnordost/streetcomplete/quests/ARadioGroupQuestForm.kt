package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.RadioGroup
import de.westnordost.streetcomplete.ui.util.content

abstract class ARadioGroupQuestForm<I : T, T> : AbstractOsmQuestForm<T>() {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    protected abstract val items: List<I>
    protected lateinit var checkedItem: MutableState<I?>

    @Composable protected abstract fun BoxScope.ItemContent(item: I)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content { Surface {
            checkedItem = remember { mutableStateOf(null) }
            RadioGroup(
                options = items,
                onSelectionChange = {
                    checkedItem.value = it
                    checkIsFormComplete()
                },
                selectedOption = checkedItem.value,
                itemContent = { ItemContent(it) }
            )
        } }
    }

    override fun onClickOk() {
        applyAnswer(checkedItem.value!!)
    }

    override fun isFormComplete() = checkedItem.value != null
}
