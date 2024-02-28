package de.westnordost.streetcomplete.overlays.surface

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.hasSurfaceLanes
import de.westnordost.streetcomplete.osm.surface.shouldBeDescribed
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

/** Manages UI for inputting the surface and conditionally the surface description / note.
 *  Tapping on the [selectButton] opens a chooser dialog for the [selectableSurfaces]. The selected
 *  surface is shown in the [selectedCellView] and if nothing is selected (so far), the
 *  [selectTextView] is shown.
 *  If the user chose a generic surface or there is already a note, the [noteInput] is shown. */
class SurfaceAndNoteViewController(
    private val selectButton: ViewGroup,
    private val noteInput: EditText,
    private val selectedCellView: ViewGroup,
    private val selectTextView: TextView,
    selectableSurfaces: List<Surface>
) {
    var value: SurfaceAndNote?
        set(value) {
            selectedSurfaceItem = value?.surface?.asItem()
            noteText = value?.note
        }
        get() {
            val surface = selectedSurfaceItem?.value
            val note = noteText
            return if (surface == null && note == null) null else SurfaceAndNote(surface, note)
        }

    private var selectedSurfaceItem: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
            updateNoteVisibility()
        }

    private var noteText: String?
        set(value) {
            noteInput.setText(value)
            updateNoteVisibility()
        }
        get() = noteInput.nonBlankTextOrNull

    private val cellLayoutId: Int = R.layout.cell_labeled_icon_select
    private val items: List<DisplayItem<Surface>> = selectableSurfaces.toItems()

    var onInputChanged: (() -> Unit)? = null

    init {
        noteInput.doAfterTextChanged { onInputChanged?.invoke() }

        selectButton.setOnClickListener {
            collectSurfaceData { surface: Surface, note: String? ->
                selectedSurfaceItem = surface.asItem()
                noteText = note
                onInputChanged?.invoke()
            }
        }

        LayoutInflater.from(selectButton.context).inflate(cellLayoutId, selectedCellView, true)
        selectButton.children.first().background = null
    }

    private fun updateSelectedCell() {
        val item = selectedSurfaceItem
        selectTextView.isGone = item != null
        selectedCellView.isGone = item == null
        if (item != null) {
            ItemViewHolder(selectedCellView).bind(item)
        }
    }

    private fun updateNoteVisibility() {
        noteInput.isGone = noteInput.nonBlankTextOrNull == null && selectedSurfaceItem?.value?.shouldBeDescribed != true
    }

    private fun collectSurfaceData(callback: (Surface, String?) -> Unit) {
        ImageListPickerDialog(selectButton.context, items, cellLayoutId, 2) { item ->
            val value = item.value
            if (value != null && value.shouldBeDescribed && !hasSurfaceLanes(element.tags)) {
                DescribeGenericSurfaceDialog(selectButton.context) { description ->
                    callback(item.value!!, description)
                }.show()
            } else {
                callback(item.value!!, null)
            }
        }.show()
    }
}
