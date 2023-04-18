package de.westnordost.streetcomplete.screens.main.overlays

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.SpinnerAdapter
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
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.databinding.DialogOverlaySelectionBinding
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.overlays.custom.getCustomOverlayIndices
import de.westnordost.streetcomplete.overlays.custom.getIndexedCustomOverlayPref
import de.westnordost.streetcomplete.util.ktx.dpToPx
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Dialog in which the user selects which overlay to display */
class OverlaySelectionDialog(context: Context) : AlertDialog(context), KoinComponent {

    private val selectedOverlayController: SelectedOverlayController by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val prefs: SharedPreferences by inject()
    private val ctx = context
    private val adapter = OverlaySelectionAdapter()

    init {
        val currentOverlay = selectedOverlayController.selectedOverlay

        val fakeOverlays = getFakeCustomOverlays()
        adapter.overlays = overlayRegistry.filterNot { it is CustomOverlay } + fakeOverlays
        adapter.selectedOverlay = if (currentOverlay is CustomOverlay)
            fakeOverlays.singleOrNull { it.wikiLink == prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0).toString() }
        else
            currentOverlay
        adapter.onSelectedOverlay = { so ->
            var selectedOverlay = so
            if (selectedOverlay?.title == 0) {
                prefs.edit { putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, selectedOverlay!!.wikiLink!!.toInt()) }
                // set the actual custom overlay instead of the fake one
                selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
            }
            if (currentOverlay != selectedOverlay || selectedOverlay is CustomOverlay)
                selectedOverlayController.selectedOverlay = selectedOverlay // only set same overlay if it's custom, as setting same one now reloads
            dismiss()
        }
        adapter.onCustomizeOverlay = { showOverlayCustomizer(it) }

        val binding = DialogOverlaySelectionBinding.inflate(LayoutInflater.from(context))

        binding.overlaysList.adapter = adapter
        binding.overlaysList.layoutManager = LinearLayoutManager(context)

        setTitle(R.string.select_overlay)

        setButton(BUTTON_NEGATIVE, context.resources.getText(android.R.string.cancel)) { _, _ ->
            dismiss()
        }

        if (prefs.getBoolean(Prefs.EXPERT_MODE, false))
            setButton(BUTTON_NEUTRAL, context.getText(R.string.custom_overlay_add_button)) { _,_ ->
                val newIdx = if (prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0").isNullOrBlank()) 0
                    else getCustomOverlayIndices(prefs).max() + 1
                showOverlayCustomizer(newIdx)
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
                override val icon = ctx.resources.getIdentifier(
                    prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, i), "ic_custom_overlay"),
                    "drawable", ctx.packageName
                ).takeIf { it != 0 } ?: R.drawable.ic_custom_overlay
                override val title = 0 // use invalid resId placeholder, the adapter needs to be aware of this
                override val wikiLink = it // index
            }
        }
    }

    @SuppressLint("SetTextI18n") // this is about element type, don't want translation here
    @Suppress("KotlinConstantConditions") // because this is simply incorrect...
    private fun showOverlayCustomizer(index: Int) {
        var d: AlertDialog? = null

        val title = EditText(ctx).apply {
            setHint(R.string.name_label)
            setText(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), ""))
        }
        val filterText = TextView(ctx).apply {
            setText(R.string.custom_overlay_filter_info)
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
            if (it == null || it.count { it == '(' } != it.count { it == ')' }) {
                d?.getButton(BUTTON_POSITIVE)?.isEnabled = false
                return@addTextChangedListener
            }
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
            setText(R.string.custom_overlay_color_info)
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
                if (it == null || it.count { it == '(' } != it.count { it == ')' }) {
                    d?.getButton(BUTTON_POSITIVE)?.isEnabled = false
                    return@doAfterTextChanged
                }
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
        val iconList = LinkedHashSet<Int>(questTypeRegistry.size).apply {
            add(R.drawable.ic_custom_overlay)
            questTypeRegistry.forEach { add(it.icon) }
        }.toList()
        val iconSpinner = Spinner(ctx).apply {
            adapter = ArrayImageAdapter(ctx, iconList)
            val selectedIcon = ctx.resources.getIdentifier(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, index), "ic_custom_overlay"), "drawable", ctx.packageName)
            setSelection(iconList.indexOf(selectedIcon))
            dropDownWidth = ctx.dpToPx(48).toInt()
            layoutParams = ViewGroup.LayoutParams(ctx.dpToPx(100).toInt(), ctx.dpToPx(48).toInt())
        }
        val linearLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(LinearLayout(ctx).apply {
                addView(iconSpinner)
                addView(title)
            })
            addView(filterText)
            addView(tag)
            addView(nodes)
            addView(ways)
            addView(relations)
            addView(colorText)
            addView(color)
            setPadding(30,10,30,10)
        }
        val indices = getCustomOverlayIndices(prefs).sorted()
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
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, index), ctx.resources.getResourceEntryName(iconList[iconSpinner.selectedItemPosition]))
                    if (index !in indices) // add if it's new
                        putString(Prefs.CUSTOM_OVERLAY_INDICES, (indices + index).joinToString(","))
                    putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, index)
                }
                // switch to overlay if we're editing current one or if it's new
                if (index !in indices || (index == prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0) && selectedOverlayController.selectedOverlay is CustomOverlay)) {
                    selectedOverlayController.selectedOverlay = null
                    selectedOverlayController.selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
                    dismiss()
                } else {
                    // otherwise reload overlay list because of icon and name
                    adapter.overlays = overlayRegistry.filterNot { it is CustomOverlay } + getFakeCustomOverlays()
                }
            }
        if (index in indices)
            b.setNeutralButton(R.string.delete_confirmation) { _, _ ->
                val overlayName = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), ctx.getString(R.string.custom_overlay_title))
                Builder(ctx)
                    .setMessage(ctx.getString(R.string.custom_overlay_delete, overlayName))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.delete_confirmation) { _, _ ->
                        prefs.edit {
                            remove(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index))
                            remove(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, index))
                            remove(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, index))
                            remove(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, index))
                            putString(Prefs.CUSTOM_OVERLAY_INDICES, indices.filterNot { it == index }.joinToString(","))
                            if (prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0) == index) {
                                putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
                                if (selectedOverlayController.selectedOverlay is CustomOverlay) {
                                    selectedOverlayController.selectedOverlay = null
                                    //adapter.selectedOverlay = null // can't change overlay, as any change will dismiss overlay selection dialog...
                                }
                            }
                        }
                        adapter.overlays = overlayRegistry.filterNot { it is CustomOverlay } + getFakeCustomOverlays()
                    }
                    .show()
            }
        d = b.create()
        d.show()
    }
}

private class ArrayImageAdapter(context: Context, private val items: List<Int>) : ArrayAdapter<Int>(context, android.R.layout.select_dialog_item, items), SpinnerAdapter {
    val params = ViewGroup.LayoutParams(context.dpToPx(48).toInt(), context.dpToPx(48).toInt())
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View { // for non-dropdown
        val view = super.getView(position, convertView, parent)
        val tv = view.findViewById<TextView>(android.R.id.text1)
        tv.text = ""
        tv.background = context.getDrawable(items[position])
        tv.layoutParams = params
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = (convertView as? ImageView) ?: ImageView(context)
        v.setImageResource(items[position])
        v.layoutParams = params
        return v
    }
}
