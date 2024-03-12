package de.westnordost.streetcomplete.screens.main.overlays

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.graphics.drawable.updateBounds
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.util.ktx.dpToPx

/** Adapter for the list in which the user can select which overlay he wants to use */
class OverlaySelectionAdapter(private val overlays: List<Overlay>) : BaseAdapter() {

    override fun getCount() = 1 + overlays.size

    override fun getItem(position: Int) = if (position > 0) overlays[position - 1] else null

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val overlay = getItem(position)
        val context = parent.context
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.row_overlay_selection, parent, false)
        val textView = view as TextView
        textView.setText(overlay?.title ?: R.string.overlay_none)
        val icon = context.getDrawable(overlay?.icon ?: R.drawable.space_24dp)
        val bound = context.resources.dpToPx(38).toInt()
        icon?.updateBounds(right = bound, bottom = bound)
        textView.setCompoundDrawables(icon, null, null, null)
        textView.compoundDrawablePadding = context.resources.dpToPx(8).toInt()
        return textView
    }
}
