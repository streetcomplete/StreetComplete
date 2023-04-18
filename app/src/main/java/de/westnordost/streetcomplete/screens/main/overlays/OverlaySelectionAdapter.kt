package de.westnordost.streetcomplete.screens.main.overlays

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.RowOverlaySelectionBinding
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.util.ktx.dpToPx

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
            // To match other preference dialogs, also invoke callback when nothing changed to allow
            // dismissing the selection dialog when the active overlay is selected again.
            onSelectedOverlay?.invoke(value)
            if (field == value) return
            field = value
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
            val ctx = binding.root.context
            binding.overlayTitle.setText(with?.title ?: R.string.overlay_none)
            val icon = with?.icon?.let { ctx.getDrawable(it) }
            icon?.setBounds(0, 0, ctx.dpToPx(32).toInt(), ctx.dpToPx(32).toInt())
            binding.overlayTitle.setCompoundDrawables(icon, null, null, null)
            binding.overlayTitle.compoundDrawablePadding = ctx.dpToPx(4).toInt()
            binding.radioButton.isChecked = with == selectedOverlay
            binding.root.setOnClickListener { selectedOverlay = with }
        }
    }
}
