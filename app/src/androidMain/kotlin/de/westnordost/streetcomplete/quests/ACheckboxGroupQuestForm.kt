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
import de.westnordost.streetcomplete.ui.common.CheckboxGroup
import de.westnordost.streetcomplete.ui.util.content
import kotlin.collections.plus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import org.jetbrains.compose.resources.stringResource

abstract class ACheckboxGroupQuestForm<I, T> : AbstractOsmQuestForm<T>() {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    protected abstract val items: List<I>
    private lateinit var selectedOptions: MutableState<Set<I>>
    @Composable protected abstract fun BoxScope.ItemContent(item: I)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content { Surface {
            selectedOptions = remember { mutableStateOf(emptySet()) }

            val onSelectionChange = { option: I, selected: Boolean ->
                selectedOptions.value = if (selected) {
                    selectedOptions.value - option
                } else {
                    selectedOptions.value + option
                }
                checkIsFormComplete()
                updateButtonPanel()
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_multiselect_hint))
                }
                CheckboxGroup(
                    options = items,
                    onSelectionChange = onSelectionChange,
                    selectedOptions = selectedOptions.value,
                    itemContent = { ItemContent(it) }
                )
            }
        } }
    }

    abstract fun onClickOk(items: Set<I>)

    override fun onClickOk() {
        onClickOk(selectedOptions.value)
    }

    override val buttonPanelAnswers: List<AnswerItem> get() =
        if (selectedOptions.value.isEmpty()) {
            listOf(AnswerItem(R.string.overlay_none) { onClickOk(emptySet<I>()) })
        } else {
            emptyList()
        }

    override fun isFormComplete() = !selectedOptions.value.isEmpty()
}
