package de.westnordost.streetcomplete.overlays

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.takeFavorites
import org.koin.android.ext.android.inject
import kotlin.getValue

/** Abstract base class for any overlay form in which the user selects an image item */
abstract class AItemSelectOverlayForm<I> : AbstractOverlayForm() {

    final override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()

    protected open val itemsPerRow = 2
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<I>
    /** items to that are selectable. May not be accessed before onCreate */
    protected open val selectableItems: List<I> get() = items
    /** items to display as last picked answer. May not be accessed before onCreate */
    protected open val lastPickedItems: List<I> get() =
        prefs.getLastPicked(this::class.simpleName!!)
            .mapNotNull { itemsByString[it] }
            .takeFavorites(n = 5, first = 1)

    private lateinit var itemsByString: Map<String, I>

    protected val selectedItem: MutableState<I?> = mutableStateOf(null)

    @Composable protected abstract fun ItemContent(item: I)

    @Composable protected abstract fun LastPickedItemContent(item: I)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = items.associateBy { it.toString() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            ItemSelectOverlayForm(
                itemsPerRow = itemsPerRow,
                items = selectableItems,
                itemContent = { ItemContent(it) },
                selectedItem = selectedItem.value,
                lastPickedItems = lastPickedItems,
                lastPickedItemContent = { LastPickedItemContent(it) },
                onSelectItem = {
                    selectedItem.value = it
                    checkIsFormComplete()
                },
            )
        } }
        checkIsFormComplete()
    }

    override fun isFormComplete() =
        selectedItem.value != null && selectedItem.value in selectableItems

    override fun onClickOk() {
        val value = selectedItem.value ?: return
        prefs.addLastPicked(this::class.simpleName!!, value.toString())
        onClickOk(value)
    }

    protected abstract fun onClickOk(selectedItem: I)
}
