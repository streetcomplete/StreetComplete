package de.westnordost.streetcomplete.quests.list_quests

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestGenericRadioListBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.util.content

abstract class AListQuestForm<T> : AbstractOsmQuestForm<T>() {
    final override val contentLayoutResId = R.layout.quest_generic_radio_list
    private val binding by contentViewBinding(QuestGenericRadioListBinding::bind)
    override val defaultExpanded = false

    protected abstract val items: List<TextItem<T>>
    protected lateinit var checkedItem: MutableState<TextItem<T>?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.questGenericRadioListBase.content {
            checkedItem = remember { mutableStateOf<TextItem<T>?>(null) }
            Surface {
                ListQuestForm(items, {
                    checkedItem.value = it
                    checkIsFormComplete()
                }, checkedItem.value)
            }
        }
    }

    override fun onClickOk() {
        applyAnswer(checkedItem.value!!.value)
    }

    override fun isFormComplete() = checkedItem.value != null
}

data class TextItem<T>(val value: T, val titleId: Int)
