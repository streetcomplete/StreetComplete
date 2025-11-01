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
import de.westnordost.streetcomplete.resources.quest_roofShape_select_one
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

/**
 * Abstract class for quests with a list of images and one to select.
 */
abstract class AItemSelectQuestForm<I, T> : AbstractOsmQuestForm<T>() {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    private val prefs: Preferences by inject()

    protected open val itemsPerRow = 4

    /** return true to move last picked items to the front. On by default. Only respected if the
     *  items do not all fit into one line */
    protected open val moveFavoritesToFront = true
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<I>
    private lateinit var actualItems: List<I>
    protected lateinit var selectedItem: MutableState<I?>

    protected abstract val serializer: KSerializer<I>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actualItems = if (items.size > itemsPerRow && moveFavoritesToFront) {
            moveFavouritesToFront(items)
        } else items
    }

    @Composable protected abstract fun ItemContent(item: I)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            selectedItem = remember { mutableStateOf(null) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_roofShape_select_one))
                }
                ItemSelectGrid(
                    columns = SimpleGridCells.Fixed(itemsPerRow),
                    items = actualItems,
                    selectedItem = selectedItem.value,
                    onSelect = {
                        selectedItem.value = it
                        checkIsFormComplete()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { ItemContent(it) }
            }
        } }
    }

    override fun onClickOk() {
        val value = selectedItem.value ?: return
        prefs.addLastPicked(ListSerializer(serializer), this::class.simpleName!!, value)
        onClickOk(value)
    }

    protected abstract fun onClickOk(selectedItem: I)

    override fun isFormComplete() = selectedItem.value != null

    private fun moveFavouritesToFront(originalList: List<I>): List<I> {
        val favourites = prefs.getLastPicked(ListSerializer(serializer), this::class.simpleName!!)
            .takeFavorites(n = itemsPerRow)
        return (favourites + originalList).distinct()
    }
}
