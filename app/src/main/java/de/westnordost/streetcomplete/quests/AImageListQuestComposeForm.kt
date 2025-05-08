package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.MultiImageList
import de.westnordost.streetcomplete.ui.common.image_select.RadioImageList
import de.westnordost.streetcomplete.ui.common.image_select.SelectableImageItem
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import org.koin.android.ext.android.inject
import kotlin.collections.map

/**
 * Abstract class for quests with a list of images and one or several to select.
 *
 * I is the type of each item in the image list (a simple model object). In MVC, this would
 * be the view model.
 *
 * T is the type of the answer object (also a simple model object) created by the quest
 * form and consumed by the quest type. In MVC, this would be the model.
 */
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
    protected lateinit var currentItems: MutableState<List<DisplayItem<I>>>

    protected open val itemContent = @androidx.compose.runtime.Composable { item: ImageListItem<I>, index: Int, onClick: () -> Unit, role: Role ->
            SelectableImageItem(
                item = item.item,
                isSelected = item.checked,
                onClick = onClick,
                role = role
            )
    }

    private lateinit var itemsByString: Map<String, DisplayItem<I>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = items.associateBy { it.value.toString() }
    }
    protected lateinit var selectedItems: MutableState<List<DisplayItem<I>>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // TODO: deal with favourites
        super.onViewCreated(view, savedInstanceState)
        refreshComposeView()
    }

    protected fun refreshComposeView() {
        binding.composeViewBase.content {
            currentItems = remember { mutableStateOf(items) }
            selectedItems = remember { mutableStateOf(listOf()) }
            Surface {
                Column {
                    if (descriptionResId != null) {
                        Text(
                            text = stringResource(descriptionResId!!),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                    if (maxSelectableItems == 1) {
                        Text(
                            text = stringResource(R.string.quest_select_hint)
                        )
                        print("Cur " + currentItems.value[0])
                        RadioImageList(
                            items = currentItems.value,
                            itemsPerRow = itemsPerRow,
                            onSelect = { newItems ->
                                selectedItems.value = newItems
                                checkIsFormComplete()
                            },
                            itemContent = itemContent
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.quest_multiselect_hint)
                        )
                        MultiImageList(
                            items = currentItems.value,
                            onSelect = { newItems ->
                                selectedItems.value = newItems
                                checkIsFormComplete()
                            },
                            itemsPerRow = itemsPerRow,
                            itemContent = itemContent

                        )
                    }
                }
            }
        }
    }

    override fun isFormComplete() = selectedItems.value.isNotEmpty()

    override fun onClickOk() {
        if (selectedItems.value.isNotEmpty()) {
            prefs.addLastPicked(this::class.simpleName!!, selectedItems.value.map { it.value.toString() })
            onClickOk(selectedItems.value.map { it.value!! })
        }
    }
    protected abstract fun onClickOk(selectedItems: List<I>)
}
