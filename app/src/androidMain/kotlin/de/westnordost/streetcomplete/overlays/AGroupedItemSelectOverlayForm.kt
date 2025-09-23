package de.westnordost.streetcomplete.overlays

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.util.content

/** Abstract base class for any overlay form in which the user selects a grouped item */
abstract class AGroupedItemSelectOverlayForm<G: Group<I>, I> : AbstractOverlayForm() {

    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    /** all items to display. May not be accessed before onCreate */
    protected abstract val groups: List<G>
    /** items to display that are shown as last picked answers. May not be accessed before onCreate */
    protected open val lastPickedItems: List<I> = emptyList()

    private lateinit var itemsByString: Map<String, I>

    protected val selectedItem: MutableState<I?> = mutableStateOf(null)

    @Composable protected abstract fun GroupContent(item: G)

    @Composable protected abstract fun ItemContent(item: I)

    @Composable protected abstract fun LastPickedItemContent(item: I)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = groups.flatMap { it.children }.associateBy { it.toString() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            GroupedItemSelectOverlayForm(
                groups = groups,
                groupContent = { GroupContent(it) },
                itemContent = { ItemContent(it) },
                selectedItem = selectedItem.value,
                lastPickedItems = lastPickedItems,
                lastPickedItemContent = { LastPickedItemContent(it) },
                onSelectItem = {
                    selectedItem.value = it
                    checkIsFormComplete()
                }
            )
        } }
        checkIsFormComplete()
    }

    override fun isFormComplete() = selectedItem.value != null

    override fun onClickOk() {
        val value = selectedItem.value ?: return
        onClickOk(value)
    }

    protected abstract fun onClickOk(selectedItem: I)
}
