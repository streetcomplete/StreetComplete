package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.TextItem
import de.westnordost.streetcomplete.ui.common.TextItemRadioGroup
import de.westnordost.streetcomplete.ui.util.content

abstract class AListQuestForm<T> : AbstractOsmQuestForm<T>() {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    protected abstract val items: List<TextItem<T>>
    protected lateinit var checkedItem: MutableState<TextItem<T>?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content {
            checkedItem = remember { mutableStateOf<TextItem<T>?>(null) }
            Surface {
                TextItemRadioGroup(
                    options = items,
                    onSelectionChange = {
                        checkedItem.value = it
                        checkIsFormComplete()
                    },
                    currentOption = checkedItem.value,
                )
            }
        }
    }

    override fun onClickOk() {
        applyAnswer(checkedItem.value!!.value)
    }

    override fun isFormComplete() = checkedItem.value != null
}
