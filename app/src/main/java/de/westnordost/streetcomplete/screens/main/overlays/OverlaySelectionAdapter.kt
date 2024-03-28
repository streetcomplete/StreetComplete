package de.westnordost.streetcomplete.screens.main.overlays

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.graphics.drawable.updateBounds
import androidx.core.view.isVisible
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.showOverlayCustomizer

/** Adapter for the list in which the user can select which overlay he wants to use */
class OverlaySelectionAdapter(
    private val overlays: List<Overlay>,
    private val prefs: ObservableSettings,
    private val questTypeRegistry: QuestTypeRegistry,
    ) : BaseAdapter() {


    override fun getCount() = 1 + overlays.size + if (prefs.getBoolean(Prefs.EXPERT_MODE, false)) 1 else 0

    override fun getItem(position: Int) = if (position > 0 && position <= overlays.size) overlays[position - 1] else null

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val overlay = getItem(position)
        val context = parent.context
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.row_overlay_selection, parent, false)
        val textView = view.findViewById(R.id.overlay_text) as TextView
        val isAdd = overlay == null && position != 0
        val titleResId = overlay?.title ?: if (isAdd) R.string.custom_overlay_add_button else R.string.overlay_none
        val bound = context.resources.dpToPx(38).toInt()
        if (titleResId != 0) { // normal overlay
            textView.setText(titleResId)
        } else { // custom overlay
            textView.text = overlay?.changesetComment // the custom title
            view.findViewById<ImageButton>(R.id.customButton)?.apply {
                isVisible = true // show settings icon
                setOnClickListener {
                    // wikiLink is used for index
                    showOverlayCustomizer(overlay!!.wikiLink!!.toInt(), context, prefs, questTypeRegistry, {
                        // changed -> always select
                        (parent as AdapterView<*>).performItemClick(textView, position, position.toLong())
                    }, {
                        // deleted -> set to 0 (none)
                        // ideally only if it was active, but then popup wouldn't be dismissed
                        (parent as AdapterView<*>).performItemClick(textView, 0, 0L)
                    } )
                }
            }
            textView.setOnClickListener {
                (parent as AdapterView<*>).performItemClick(textView, position, position.toLong())
            }
        }

        val icon = context.getDrawable(overlay?.icon ?: if (isAdd) R.drawable.ic_add_24dp else R.drawable.space_24dp)
        icon?.updateBounds(right = bound, bottom = bound)
        textView.setCompoundDrawables(icon, null, null, null)
        textView.compoundDrawablePadding = context.resources.dpToPx(8).toInt()
        return view
    }
}
