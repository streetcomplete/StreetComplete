package de.westnordost.streetcomplete.screens.main.overlays

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.core.content.edit
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
import de.westnordost.streetcomplete.view.setHtml
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
            if (it?.title == 0) {
                prefs.edit { putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, it.wikiLink!!.toInt()) }
                if (selectedOverlayController.selectedOverlay is CustomOverlay)
                    selectedOverlayController.selectedOverlay = null // trigger reload
                selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
            } else
                selectedOverlay = it
        }
        binding.overlaysList.adapter = adapter
        binding.overlaysList.layoutManager = LinearLayoutManager(context)

        setTitle(R.string.select_overlay)

        setButton(BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
            selectedOverlayController.selectedOverlay = selectedOverlay
            dismiss()
        }

        if (prefs.getBoolean(Prefs.EXPERT_MODE, false))
            setButton(BUTTON_NEGATIVE, context.resources.getText(R.string.custom_overlay_title)) { _,_ ->
                showOverlayCustomizer()
                dismiss()
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
    private fun showOverlayCustomizer() {
        val c = ctx
        var d: AlertDialog? = null
        val i = prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)

        val title = EditText(c).apply {
            setHint(R.string.name_label)
            setText(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, i), ""))
        }
        val overlayFilter = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, i), "")?.split(" with ")?.takeIf { it.size == 2 }
        val nodes = CheckBox(c).apply {
            text = "nodes"
            isChecked = overlayFilter?.get(0)?.contains("nodes") ?: true
        }
        val ways = CheckBox(c).apply {
            text = "ways"
            isChecked = overlayFilter?.get(0)?.contains("ways") ?: true
        }
        val relations = CheckBox(c).apply {
            text = "relations"
            isChecked = overlayFilter?.get(0)?.contains("relations") ?: true
        }
        val tag = EditText(c).apply {
            setHint(R.string.element_selection_button)
            setText(overlayFilter?.get(1) ?: "")
        }
        val coloringText = TextView(c).apply {
            setText(R.string.custom_overlay_color_message)
        }
        val color = EditText(c).apply {
            setHint(R.string.custom_overlay_color_hint)
            setText(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, i), "")!!)
            doAfterTextChanged {
                try {
                    it.toString().toRegex()
                    d?.getButton(BUTTON_POSITIVE)?.isEnabled = true
                }
                catch (e: Exception) {
                    Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    d?.getButton(BUTTON_POSITIVE)?.isEnabled = false
                }
            }
        }
        fun filterString(): String {
            val types = listOfNotNull(
                if (nodes.isChecked) "nodes" else null,
                if (ways.isChecked) "ways" else null,
                if (relations.isChecked) "relations" else null,
            ).joinToString(", ")
            return "$types with ${tag.text}"
        }
        tag.addTextChangedListener {
            try {
                filterString().toElementFilterExpression()
                d?.getButton(BUTTON_POSITIVE)?.apply { isEnabled = true }
            }
            catch (e: Exception) { // for some reason catching import de.westnordost.streetcomplete.data.elementfilter.ParseException is not enough (#386), though I cannot reproduce it
                Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                d?.getButton(BUTTON_POSITIVE)?.apply { isEnabled = tag.text.isEmpty() }
            }
        }
        val linearLayout = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            addView(title)
            addView(TextView(ctx).apply { setHtml(resources.getString(R.string.custom_overlay_message)) })
            addView(tag)
            addView(nodes)
            addView(ways)
            addView(relations)
            addView(coloringText)
            addView(color)
            setPadding(30,10,30,10)
        }
        d = Builder(c)
            .setTitle(R.string.custom_overlay_title)
            .setView(ScrollView(c).apply { addView(linearLayout) })
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.edit {
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, i), filterString())
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, i), color.text.toString())
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, i), title.text.toString())
                }
                if (selectedOverlay?.name == CustomOverlay::class.simpleName)
                    selectedOverlayController.apply { // trigger reload
                        val temp = selectedOverlay
                        selectedOverlay = null
                        selectedOverlay = temp
                    }
            }
            .setNeutralButton(R.string.custom_overlay_manage) { _, _ ->
                showCustomOverlayManager()
            }
            .create()
        d.show()
    }

    private fun showCustomOverlayManager() {
        val c = ctx
        val indices = prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0")!!.split(",").mapNotNull { it.toIntOrNull() }.ifEmpty { listOf(0) }.sorted()
        var d: AlertDialog? = null
        val layout = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL }
        layout.setPadding(40, 20, 40, 20)
        indices.forEach { idx ->
            val l = LinearLayout(c)
            val text = TextView(c)
            text.text = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, idx), c.getString(R.string.custom_overlay_title))
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            text.setOnClickListener {
                prefs.edit { putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, idx) }
                showOverlayCustomizer()
                d?.dismiss()
            }
            l.addView(ImageView(c).apply {
                setImageResource(R.drawable.ic_delete_24dp)
                isEnabled = idx != 0
                setOnClickListener {
                    AlertDialog.Builder(c)
                        .setMessage(c.getString(R.string.custom_overlay_delete, text.text))
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
                        }
                        .show()
                }
            })
            l.addView(text)
            l.setPadding(0, 10, 0, 10)
            layout.addView(l)
        }
        layout.addView(LinearLayout(c).apply {
            addView(ImageView(c).apply { setImageResource(R.drawable.ic_add_24dp) })
            addView(TextView(c).apply {
                text = "add" // todo
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            })
            setOnClickListener {
            prefs.edit {
                val newIdx = indices.last() + 1
                putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, newIdx)
                putString(Prefs.CUSTOM_OVERLAY_INDICES, (indices + newIdx).joinToString(","))
            }
            showOverlayCustomizer()
            d?.dismiss()
        }

        })

        d = AlertDialog.Builder(c)
            .setView(layout)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        d.show()
    }
}
