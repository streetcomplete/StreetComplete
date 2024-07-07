package de.westnordost.streetcomplete.overlays.mtb_scale

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class MtbScaleViewController(
    private val selectButton: ViewGroup,
    private val selectedCellView: ViewGroup,
    private val selectTextView: TextView,
) {
    var value: MtbScale?
        set(value) {
            selectedMtbScaleItem = value?.asItem()
        }
        get() {
            return selectedMtbScaleItem?.value
        }

    private var selectedMtbScaleItem: DisplayItem<MtbScale>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    private val cellLayoutId: Int = R.layout.cell_labeled_icon_select
    private val dialogCellLayoutId: Int = R.layout.cell_labeled_icon_select_mtb_scale
    private val items: List<DisplayItem<MtbScale>> = MtbScale.entries.toItems()

    var onInputChanged: (() -> Unit)? = null

    init {
        selectButton.setOnClickListener {
            collectMtbScaleData { sacScale: MtbScale ->
                selectedMtbScaleItem = sacScale.asItem()
                onInputChanged?.invoke()
            }
        }

        LayoutInflater.from(selectButton.context).inflate(cellLayoutId, selectedCellView, true)
        selectButton.children.first().background = null
    }

    private fun updateSelectedCell() {
        val item = selectedMtbScaleItem
        selectTextView.isGone = item != null
        selectedCellView.isGone = item == null
        if (item != null) {
            ItemViewHolder(selectedCellView).bind(item)
        }
    }

    private fun collectMtbScaleData(callback: (MtbScale) -> Unit) {
        ImageListPickerDialog(selectButton.context, items, dialogCellLayoutId, 1) { item ->
            callback(item.value!!)
        }.show()
    }
}
