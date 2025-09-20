package de.westnordost.streetcomplete.overlays

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectButton
import de.westnordost.streetcomplete.ui.util.content

/** Abstract base class for any overlay form in which the user selects an image item */
abstract class AItemSelectOverlayForm<I> : AbstractOverlayForm() {

    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    protected open val itemsPerRow = 2
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<I>
    /** items to that are selectable. May not be accessed before onCreate */
    protected open val selectableItems: List<I> get() = items
    /** item to display as last picked answer. May not be accessed before onCreate */
    protected open val lastPickedItem: I? = null

    protected val selectedItem: MutableState<I?> = mutableStateOf(null)

    @Composable protected abstract fun ItemContent(item: I)

    @Composable protected open fun LastPickedItemContent(item: I) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            var hasPicked by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 160.dp),
                contentAlignment = Alignment.Center,
            ) {
                ItemSelectButton(
                    columns = SimpleGridCells.Fixed(itemsPerRow),
                    items = selectableItems,
                    onSelected = {
                        selectedItem.value = it
                        hasPicked = true
                        checkIsFormComplete()
                    },
                    selectedItem = selectedItem.value,
                    modifier = Modifier.padding(bottom = 48.dp),
                    itemContent = { ItemContent(it) },
                )
                lastPickedItem?.let { lastPickedItem ->
                    if (!hasPicked) {
                        Button2(
                            onClick = {
                                selectedItem.value = lastPickedItem
                                hasPicked = true
                                checkIsFormComplete()
                            },
                            style = ButtonStyle.Outlined,
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
                            modifier = Modifier.align(Alignment.BottomCenter).height(40.dp)
                        ) {
                            LastPickedItemContent(lastPickedItem)
                        }
                    }
                }
            }
        } }
    }

    override fun isFormComplete() =
        selectedItem.value != null && selectedItem.value in selectableItems
}
