package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

/**
 * Abstract class for quests with a list of images and several to select.
 */
abstract class AItemsSelectQuestForm<I, T> : AbstractOsmQuestForm<T>() {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    private val prefs: Preferences by inject()

    private var isDisplayingPrevious = false

    protected open var preselectedItems: Set<I> = emptySet()

    protected open val itemsPerRow = 4

    /** return true to move last picked items to the front. On by default. Only respected if the
     *  items do not all fit into one line */
    protected open val moveFavoritesToFront = true
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<I>
    private lateinit var reorderedItems: List<I>
    protected lateinit var selectedItems: MutableState<Set<I>>

    protected abstract val serializer: KSerializer<I>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reorderedItems = if (items.size > itemsPerRow && moveFavoritesToFront) {
            moveFavouritesToFront(items)
        } else {
            items
        }
    }

    @Composable protected abstract fun ItemContent(item: I)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (preselectedItems.isNotEmpty()) {
            isDisplayingPrevious = true
        }

        binding.composeViewBase.content { Surface {
            selectedItems = remember { mutableStateOf(preselectedItems) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_multiselect_hint))
                }
                ItemsSelectGrid(
                    columns = SimpleGridCells.Fixed(itemsPerRow),
                    items = reorderedItems,
                    selectedItems = selectedItems.value,
                    onSelect = ::onSelect,
                    modifier = Modifier.fillMaxWidth()
                ) { ItemContent(it) }
            }
        }
            checkIsFormComplete()
        }
    }

    open fun onSelect(item: I, selected: Boolean) {
        selectedItems.value =
            if (selected) { selectedItems.value + item } else { selectedItems.value - item }
        checkIsFormComplete()
    }

    override fun onClickOk() {
        val values = selectedItems.value
        if (values.isNotEmpty()) {
            prefs.addLastPicked(ListSerializer(serializer), this::class.simpleName!!, values.toList())
            onClickOk(values)
        }
    }

    protected abstract fun onClickOk(selectedItems: Set<I>)

    override fun isFormComplete() = !isDisplayingPrevious && selectedItems.value.isNotEmpty()

    private fun moveFavouritesToFront(originalList: List<I>): List<I> {
        val favourites = prefs.getLastPicked(ListSerializer(serializer), this::class.simpleName!!)
            .takeFavorites(n = itemsPerRow)
        return (favourites + originalList).distinct()
    }

    override val buttonPanelAnswers get() =
        if (isDisplayingPrevious) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) {
                    isDisplayingPrevious = false
                    updateButtonPanel()
                },
                AnswerItem(R.string.quest_generic_hasFeature_yes) {
                    applyAnswer(preselectedItems as T)
                }
            )
        } else {
            emptyList()
        }
}
