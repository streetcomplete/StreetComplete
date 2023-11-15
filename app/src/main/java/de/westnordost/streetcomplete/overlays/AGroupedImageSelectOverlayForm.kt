package de.westnordost.streetcomplete.overlays

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentOverlayImageSelectBinding
import de.westnordost.streetcomplete.util.LastPickedValuesStore
import de.westnordost.streetcomplete.util.mostCommonWithin
import de.westnordost.streetcomplete.util.padWith
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.GroupedImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

/** Abstract base class for any overlay form in which the user selects a grouped item */
abstract class AGroupedImageSelectOverlayForm<I> : AbstractOverlayForm() {
    // mostly copy-pasta from AImageSelectOverlayForm / AGroupedImageListQuestForm :-(

    final override val contentLayoutResId = R.layout.fragment_overlay_image_select
    private val binding by contentViewBinding(FragmentOverlayImageSelectBinding::bind)

    protected open val itemsPerRow = 3

    /** all items to display. May not be accessed before onCreate */
    protected abstract val allItems: List<GroupableDisplayItem<I>>
    /** items to display that are shown on the top. May not be accessed before onCreate */
    protected abstract val topItems: List<GroupableDisplayItem<I>>

    private lateinit var favs: LastPickedValuesStore<GroupableDisplayItem<I>>

    protected open val cellLayoutId: Int = R.layout.cell_labeled_icon_select_with_description
    protected open val groupCellLayoutId: Int = R.layout.cell_labeled_icon_select_with_description_group

    private lateinit var itemsByString: Map<String, GroupableDisplayItem<I>>

    var selectedItem: GroupableDisplayItem<I>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    override fun onAttach(ctx: Context) {
        super.onAttach(ctx)
        favs = LastPickedValuesStore(
            PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext),
            key = javaClass.simpleName,
            serialize = { it.value.toString() },
            deserialize = { itemsByString[it] }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = allItems.mapNotNull { it.items }.flatten().associateBy { it.value.toString() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectButton.root.setOnClickListener {
            val items = getInitialItems() + allItems
            GroupedImageListPickerDialog(requireContext(), items, groupCellLayoutId, cellLayoutId, itemsPerRow) { item ->
                if (item != selectedItem) {
                    selectedItem = item
                    checkIsFormComplete()
                }
            }.show()
        }

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectButton.selectedCellView, true)
        binding.selectButton.selectedCellView.children.first().background = null

        updateSelectedCell()
    }

    private fun getInitialItems(): List<GroupableDisplayItem<I>> =
        favs.get().mostCommonWithin(6, historyCount = 50, first = 1).padWith(topItems).toList()

    private fun updateSelectedCell() {
        val item = selectedItem
        binding.selectButton.selectTextView.isGone = item != null
        binding.selectButton.selectedCellView.isGone = item == null
        if (item != null) {
            ItemViewHolder(binding.selectButton.selectedCellView).bind(item)
        }
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        selectedItem = inState.getString(SELECTED)?.let { itemsByString[it] }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SELECTED, selectedItem?.value?.toString())
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete() = selectedItem?.value != null

    companion object {
        private const val SELECTED = "selected"
    }
}
