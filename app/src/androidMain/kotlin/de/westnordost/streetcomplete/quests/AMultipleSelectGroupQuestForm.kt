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
import de.westnordost.streetcomplete.ui.common.MultipleSelectGroup
import de.westnordost.streetcomplete.ui.util.content
import kotlin.collections.plus
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_NO
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_ONLY
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_YES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import org.jetbrains.compose.resources.stringResource

abstract class AMultipleSelectGroupQuestForm<T, I : T> : AbstractOsmQuestForm<Set<T>>() {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    protected abstract val items: List<I>
    protected val selectedOptions: MutableState<Set<I>> = mutableSetOf(emptySet())
    @Composable protected abstract fun BoxScope.ItemContent(item: I)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeViewBase.content { Surface {
            selectedOptions = remember { mutableStateOf(emptySet()) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_multiselect_hint))
                }
                MultipleSelectGroup(
                    options = items,
                    onSelectionChange = { option: I ->
                        selectedOptions.value = if (selectedOptions.value.contains(option)) {
                            selectedOptions.value - option
                        } else {
                            selectedOptions.value + option
                        }
                        checkIsFormComplete()
                        updateButtonPanel()
                    },
                    selectedOptions = selectedOptions.value,
                    itemContent = { ItemContent(it) }
                )
            }
        } }
    }

    override fun onClickOk() {
        applyAnswer(selectedOptions.value)
    }

    override val buttonPanelAnswers: List<AnswerItem> get() =
        if (selectedOptions.value.isEmpty()) {
            listOf(AnswerItem(R.string.overlay_none) { applyAnswer(emptySet()) })
        } else {
            emptyList()
        }

    override fun isFormComplete() = !selectedOptions.value.isEmpty()
}
