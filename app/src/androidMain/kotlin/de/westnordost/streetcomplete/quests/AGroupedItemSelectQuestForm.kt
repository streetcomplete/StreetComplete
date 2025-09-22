package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_select_hint_most_specific
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.item_select.GroupedItemSelectColumn
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

/**
 * Abstract class for quests with a grouped list of images and one to select.
 *
 * Saving and restoring state is not implemented
 */
abstract class AGroupedItemSelectQuestForm<G: Group<I>, I, T> : AbstractOsmQuestForm<T>() {

    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    override val defaultExpanded = false

    /** all items to display. May not be accessed before onCreate */
    protected abstract val groups: List<G>
    /** items to display that are shown on the top. May not be accessed before onCreate */
    protected abstract val topItems: List<I>
    private lateinit var actualTopItems: List<I>
    protected val selectedGroup: MutableState<G?> = mutableStateOf(null)
    protected val selectedItem: MutableState<I?> = mutableStateOf(null)

    private lateinit var itemsByString: Map<String, I>

    @Composable protected abstract fun GroupContent(item: G)

    @Composable protected abstract fun ItemContent(item: I)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = groups.flatMap { it.children }.associateBy { it.toString() }
        actualTopItems = getInitialItems()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_select_hint_most_specific))
                }
                GroupedItemSelectColumn(
                    groups = groups,
                    topItems = actualTopItems,
                    selectedItem = selectedItem.value,
                    selectedGroup = selectedGroup.value,
                    onSelect = { group, item ->
                        selectedGroup.value = group
                        selectedItem.value = item
                        checkIsFormComplete()
                    },
                    groupContent = { GroupContent(it) },
                    itemContent = { ItemContent(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } }
    }

    private fun getInitialItems(): List<I> =
        prefs.getLastPicked(this::class.simpleName!!)
            .map { itemsByString[it] }
            .takeFavorites(n = 6, first = 1, pad = topItems)

    override fun onClickOk() {
        val group = selectedGroup.value
        val groupItem = group?.item
        val item = selectedItem.value
        if (groupItem != null) {
            context?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.quest_generic_item_confirmation)
                    .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                    .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                        prefs.addLastPicked(this::class.simpleName!!, groupItem.toString())
                        onClickOk(groupItem)
                    }
                    .show()
            }
        } else if (item != null) {
            prefs.addLastPicked(this::class.simpleName!!, item.toString())
            onClickOk(item)
        }
    }

    abstract fun onClickOk(value: I)

    override fun isFormComplete() = selectedItem.value != null || selectedGroup.value?.item != null
}
