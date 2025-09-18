package de.westnordost.streetcomplete.overlays

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
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

    @Composable protected abstract fun BoxScope.ItemContent(item: I)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .defaultMinSize(minHeight = 160.dp)
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                ItemSelectButton(
                    columns = SimpleGridCells.Fixed(itemsPerRow),
                    items = selectableItems,
                    onSelected = {
                        selectedItem.value = it
                        checkIsFormComplete()
                    },
                    selectedItem = selectedItem.value,
                    itemContent = { ItemContent(it) },
                )
            }
        } }
/*

        binding.lastPickedButton.isGone = lastPickedItem == null
        binding.lastPickedButton.setImage(lastPickedItem?.image)
        binding.lastPickedButton.setOnClickListener {
            selectedItem = lastPickedItem
            binding.lastPickedButton.isGone = true
            checkIsFormComplete()
        }
        */
    }

    override fun isFormComplete() =
        selectedItem.value != null && selectedItem.value in selectableItems
}
