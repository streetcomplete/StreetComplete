package de.westnordost.streetcomplete.screens.main.overlays

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.RowOverlaySelectionBinding
import de.westnordost.streetcomplete.overlays.Overlay

/** Adapter for the list in which the user can select which overlay he wants to use */
class OverlaySelectionAdapter : RecyclerView.Adapter<OverlaySelectionAdapter.ViewHolder>() {

    var onSelectedOverlay: ((Overlay?) -> Unit)? = null

    var overlays: List<Overlay> = emptyList()
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    var selectedOverlay: Overlay? = null
        set(value) {
            if (field == value) return
            field = value
            onSelectedOverlay?.invoke(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(RowOverlaySelectionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(if (position == 0) null else overlays[position - 1])
    }

    override fun getItemCount() = 1 + overlays.size

    inner class ViewHolder(private val binding: RowOverlaySelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(with: Overlay?) {
            binding.radioButton.setText(with?.title ?: R.string.overlay_none)
            binding.radioButton.setOnCheckedChangeListener(null)
            binding.radioButton.isChecked = with == selectedOverlay
            binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedOverlay = with
            }
        }
    }
}
