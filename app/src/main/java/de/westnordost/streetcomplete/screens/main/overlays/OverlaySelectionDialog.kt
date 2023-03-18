package de.westnordost.streetcomplete.screens.main.overlays

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.databinding.DialogOverlaySelectionBinding
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.overlays.custom.getIndexedCustomOverlayPref
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Dialog in which the user selects which overlay to display */
class OverlaySelectionDialog(context: Context) : AlertDialog(context), KoinComponent {

    private val selectedOverlayController: SelectedOverlayController by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val prefs: SharedPreferences by inject()
    private val ctx = context

    private val binding = DialogOverlaySelectionBinding.inflate(LayoutInflater.from(context))
    private var selectedOverlay: Overlay? = selectedOverlayController.selectedOverlay

    init {
        val adapter = OverlaySelectionAdapter()
        val fakeOverlays = getFakeCustomOverlays()
        adapter.overlays = overlayRegistry.filterNot { it is CustomOverlay } + fakeOverlays
        adapter.selectedOverlay = if (selectedOverlayController.selectedOverlay is CustomOverlay)
            fakeOverlays.singleOrNull { it.wikiLink == prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0).toString() }
        else
            selectedOverlayController.selectedOverlay
        adapter.onSelectedOverlay = {
            selectedOverlay = it
            getButton(BUTTON_NEUTRAL)?.isEnabled = selectedOverlay?.title == 0 // only enable custom overlay button if one is selected
        }
        setOnShowListener { getButton(BUTTON_NEUTRAL)?.isEnabled = (selectedOverlay is CustomOverlay || selectedOverlay?.title == 0) }
        binding.overlaysList.adapter = adapter
        binding.overlaysList.layoutManager = LinearLayoutManager(context)

        setTitle(R.string.select_overlay)

        setButton(BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
            if (selectedOverlay?.title == 0) {
                prefs.edit { putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, selectedOverlay!!.wikiLink!!.toInt()) }
                // set the actual custom overlay instead of the fake one
                selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
            }
            if (selectedOverlayController.selectedOverlay is CustomOverlay)
                selectedOverlayController.selectedOverlay = null // trigger reload (needed if switching between custom overlays)
            selectedOverlayController.selectedOverlay = selectedOverlay
            dismiss()
        }

        if (prefs.getBoolean(Prefs.EXPERT_MODE, false))
            setButton(BUTTON_NEUTRAL, context.resources.getText(R.string.custom_overlay_title)) { _,_ ->
                val currentCustomOverlay = prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
                val selectedCustomOverlay = selectedOverlay?.wikiLink?.toIntOrNull()
                showOverlayCustomizer(selectedCustomOverlay ?: currentCustomOverlay)
            }

        setView(binding.root)
    }

    private fun getFakeCustomOverlays(): List<Overlay> {
        if (!prefs.getBoolean(Prefs.EXPERT_MODE, false)) return emptyList()
        return prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0")!!.split(",").mapNotNull {
            val i = it.toIntOrNull() ?: return@mapNotNull null
            object : Overlay {
                override fun getStyledElements(mapData: MapDataWithGeometry) = emptySequence<Pair<Element, Style>>()
                override fun createForm(element: Element?) = null
                override val changesetComment = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, i), "")!!.ifBlank { ctx.getString(R.string.custom_overlay_title) } // displayed overlay name
                override val icon = R.drawable.ic_custom_overlay_poi
                override val title = 0 // use invalid resId placeholder, the adapter needs to be aware of this
                override val wikiLink = it // index
            }
        }
    }

    @SuppressLint("SetTextI18n") // this is about element type, don't want translation here
    private fun showOverlayCustomizer(index: Int) {
        var d: AlertDialog? = null

        val title = EditText(ctx).apply {
            setHint(R.string.name_label)
            setText(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), ""))
        }
        val filterText = TextView(ctx).apply {
            setText("filter info")
            setPadding(10, 10, 10, 5)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(ContextCompat.getColor(ctx, R.color.accent))
            setOnClickListener {
                val dialog = Builder(ctx)
                    .setMessage(R.string.custom_overlay_filter_message)
                    .setPositiveButton(R.string.close, null)
                    .setNeutralButton("link") { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/Helium314/SCEE/blob/modified/CONTRIBUTING_A_NEW_QUEST.md#element-selection".toUri())
                        try { startActivity(ctx, intent, null) } catch (_: Exception) { }
                    }
                    .create()
                dialog.show()
            }
        }
        val overlayFilter = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, index), "")?.split(" with ")?.takeIf { it.size == 2 }
        val tag = EditText(ctx).apply {
            setHint(R.string.element_selection_button)
            setText(overlayFilter?.get(1) ?: "")
        }
        val nodes = CheckBox(ctx).apply {
            text = "nodes"
            isChecked = overlayFilter?.get(0)?.contains("nodes") ?: true
        }
        val ways = CheckBox(ctx).apply {
            text = "ways"
            isChecked = overlayFilter?.get(0)?.contains("ways") ?: true
        }
        val relations = CheckBox(ctx).apply {
            text = "relations"
            isChecked = overlayFilter?.get(0)?.contains("relations") ?: true
        }
        fun filterString(): String {
            val types = listOfNotNull(
                if (nodes.isChecked) "nodes" else null,
                if (ways.isChecked) "ways" else null,
                if (relations.isChecked) "relations" else null,
            ).joinToString(", ")
            return "$types with ${tag.text}"
        }
        // need to add this after filterString, which needs to be added after tag
        tag.addTextChangedListener {
            try {
                filterString().toElementFilterExpression()
                d?.getButton(BUTTON_POSITIVE)?.apply { isEnabled = true }
            }
            catch (e: Exception) { // for some reason catching import de.westnordost.streetcomplete.data.elementfilter.ParseException is not enough (#386), though I cannot reproduce it
                Toast.makeText(this.ctx, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                d?.getButton(BUTTON_POSITIVE)?.apply { isEnabled = tag.text.isEmpty() }
            }
        }
        val colorText = TextView(ctx).apply {
            setText(R.string.custom_overlay_color_message)
            setText("color info")
            setPadding(10, 10, 10, 5)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(ContextCompat.getColor(ctx, R.color.accent))
            setOnClickListener {
                val dialog = Builder(ctx)
                    .setMessage(R.string.custom_overlay_color_message)
                    .setPositiveButton(R.string.close, null)
                    .create()
                dialog.show()
            }
        }
        val color = EditText(ctx).apply {
            setHint(R.string.custom_overlay_color_hint)
            setText(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, index), "")!!)
            doAfterTextChanged {
                try {
                    it.toString().toRegex()
                    d?.getButton(BUTTON_POSITIVE)?.isEnabled = true
                }
                catch (e: Exception) {
                    Toast.makeText(this@OverlaySelectionDialog.ctx, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    d?.getButton(BUTTON_POSITIVE)?.isEnabled = false
                }
            }
        }
        val linearLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(title)
            addView(filterText)
            addView(tag)
            addView(nodes)
            addView(ways)
            addView(relations)
            addView(colorText)
            addView(color)
            setPadding(30,10,30,10)
        }
        val indices = prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0")!!.split(",").mapNotNull { it.toIntOrNull() }.ifEmpty { listOf(0) }.sorted()
        val b = Builder(ctx)
            .setTitle(R.string.custom_overlay_title)
            .setView(ScrollView(ctx).apply { addView(linearLayout) })
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                // update prefs and enable this overlay
                prefs.edit {
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, index), filterString())
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, index), color.text.toString())
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), title.text.toString())
                    if (index !in indices) // add if it's new
                        putString(Prefs.CUSTOM_OVERLAY_INDICES, (indices + index).joinToString(","))
                    putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, index)
                }
                selectedOverlayController.selectedOverlay = null
                selectedOverlayController.selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
            }
        if (index in indices)
            b.setNeutralButton(R.string.custom_overlay_manage) { _, _ -> showCustomOverlayManager() }
        d = b.create()
        d.show()
    }

    private fun showCustomOverlayManager() {
        val indices = prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0")!!.split(",").mapNotNull { it.toIntOrNull() }.ifEmpty { listOf(0) }.sorted()
        var d: AlertDialog? = null
        val layout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        layout.setPadding(40, 20, 40, 20)
        indices.forEach { idx ->
            val l = LinearLayout(ctx)
            val text = TextView(ctx)
            text.text = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, idx), ctx.getString(R.string.custom_overlay_title))
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            text.setOnClickListener {
                prefs.edit { putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, idx) }
                showOverlayCustomizer(idx)
                d?.dismiss()
            }
            l.addView(ImageView(ctx).apply {
                setImageResource(R.drawable.ic_delete_24dp)
                isEnabled = idx != 0
                setOnClickListener {
                    Builder(ctx)
                        .setMessage(ctx.getString(R.string.custom_overlay_delete, text.text))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.delete_confirmation) { _, _ ->
                            prefs.edit {
                                remove(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, idx))
                                remove(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, idx))
                                remove(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, idx))
                                putString(Prefs.CUSTOM_OVERLAY_INDICES, indices.filterNot { it == idx }.joinToString(","))
                                if (prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0) == idx) {
                                    putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
                                    selectedOverlayController.selectedOverlay = null
                                }
                            }
                            d?.dismiss()
                            showCustomOverlayManager()
                        }
                        .show()
                }
            })
            l.addView(text)
            l.setPadding(0, 10, 0, 10)
            layout.addView(l)
        }
        layout.addView(LinearLayout(ctx).apply {
            addView(ImageView(ctx).apply { setImageResource(R.drawable.ic_add_24dp) })
            addView(TextView(ctx).apply {
                setText(R.string.custom_overlay_add)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            })
            setOnClickListener {
                val newIdx = indices.max() + 1
                showOverlayCustomizer(newIdx)
                d?.dismiss()
            }
        })

        d = Builder(ctx)
            .setTitle(R.string.custom_overlay_manage)
            .setView(layout)
            .setNegativeButton(R.string.close, null)
            .create()
        d.show()
    }
}
