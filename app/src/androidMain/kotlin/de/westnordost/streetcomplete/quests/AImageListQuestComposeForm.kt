package de.westnordost.streetcomplete.quests


import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.image_select.ImageList
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableImageItem
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import org.koin.android.ext.android.inject

abstract class AImageListQuestComposeForm<I, T> : AbstractOsmQuestForm<T>() {
    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    private val prefs: Preferences by inject()
    override val defaultExpanded = false

    protected open val descriptionResId: Int? = null

    protected open val itemsPerRow = 4
    /** return -1 for any number. Default: 1  */
    protected open val maxSelectableItems = 1
    /** return true to move last picked items to the front. On by default. Only respected if the
     *  items do not all fit into one line */
    protected open val moveFavoritesToFront = true
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<DisplayItem<I>>
    protected var currentItems: MutableState<List<ImageListItem<I>>> = mutableStateOf(emptyList())
    /**
     * Return the composable used for a list item
     */
    protected open val itemContent = @Composable { item: ImageListItem<I>, index: Int, onClick: () -> Unit, role: Role ->
        key(item.item) {
            SelectableImageItem(
                item = item.item,
                isSelected = item.checked,
                onClick = onClick,
                modifier = Modifier.fillMaxSize(),
                role = role
            )
        }
    }

    protected open val onItemSelected: (List<DisplayItem<I>>) -> Unit = { newItems ->
        checkIsFormComplete()
    }
    private lateinit var itemsByString: Map<String, DisplayItem<I>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = items.associateBy { it.value.toString() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // TODO: deal with favourites
        super.onViewCreated(view, savedInstanceState)
        currentItems.value = items.map { ImageListItem(it, false) }
        refreshComposeView()
    }
    protected fun refreshComposeView() {
        binding.composeViewBase.setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column {
                    descriptionResId?.let {
                        Text(
                            text = stringResource(it),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = stringResource(
                            if (maxSelectableItems == 1) R.string.quest_select_hint
                            else R.string.quest_multiselect_hint
                        ),
                        modifier = Modifier.padding(bottom = 8.dp).wrapContentSize()
                    )
                    ImageList(
                        imageItems = currentItems.value,
                        onClick = if ( maxSelectableItems == 1) onClickSingleItem else onClickMultiItem,
                        modifier = Modifier.fillMaxHeight(),
                        itemsPerRow = itemsPerRow,
                        itemRole = if (maxSelectableItems == 1)  Role.RadioButton else Role.Checkbox,
                        itemContent = { item, index, onClick, role ->
                            key(item.item to items) {
                                itemContent(item, index, onClick, role)
                            }
                        })
                }
            }
        }
    }

    val onClickSingleItem = { targetIndex: Int, targetItem: ImageListItem<I> ->
        currentItems.value = currentItems.value.mapIndexed { currentIndex, currentItem ->
            if (targetIndex == currentIndex)
                ImageListItem(currentItem.item, !currentItem.checked)
            else
                ImageListItem(currentItem.item, false)
        }
        onItemSelected(currentItems.value.filter { it.checked }.map { it.item })
    }

   val onClickMultiItem = { targetIndex: Int, targetItem: ImageListItem<I> ->
       if (targetItem.checked || maxSelectableItems <= 0 || currentItems.value.count { it.checked } < maxSelectableItems) {
           currentItems.value = currentItems.value.mapIndexed { currentIndex, currentItem ->
               if (targetIndex == currentIndex)
                   ImageListItem(currentItem.item, !currentItem.checked)
               else
                   currentItem
           }
           onItemSelected(currentItems.value.filter { it.checked }.map { it.item })
       }
    }
    override fun isFormComplete() = currentItems.value.filter { it.checked }.map { it.item }.isNotEmpty()
    override fun onClickOk() {
        if (currentItems.value.filter { it.checked }.map { it.item }.isNotEmpty()) {
            prefs.addLastPicked(this::class.simpleName!!, currentItems.value.filter { it.checked }.map { it.item }.map { it.value.toString() })
            onClickOk(currentItems.value.filter { it.checked }.map { it.item }.map { it.value!! })
        }
    }
    protected abstract fun onClickOk(selectedItems: List<I>)

}
