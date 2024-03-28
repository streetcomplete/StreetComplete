package de.westnordost.streetcomplete.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.overlays.custom.getCustomOverlayIndices
import de.westnordost.streetcomplete.overlays.custom.getIndexedCustomOverlayPref
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.view.ArrayImageAdapter

@SuppressLint("SetTextI18n") // this is about element type, don't want translation here
@Suppress("KotlinConstantConditions") // because this is simply incorrect...
fun showOverlayCustomizer(
    index: Int,
    ctx: Context,
    prefs: ObservableSettings,
    questTypeRegistry: QuestTypeRegistry,
    onChanged: (Boolean) -> Unit, // true if overlay is currently set custom overlay
    onDeleted: (Boolean) -> Unit, // true if overlay was currently set custom overlay
) {
    var d: AlertDialog? = null
    val padding = ctx.resources.dpToPx(4).toInt()


    val title = EditText(ctx).apply {
        setHint(R.string.name_label)
        setText(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), ""))
    }
    val iconList = LinkedHashSet<Int>(questTypeRegistry.size).apply {
        add(R.drawable.ic_custom_overlay)
        questTypeRegistry.forEach { add(it.icon) }
    }.toList()
    val iconSpinner = Spinner(ctx).apply {
        adapter = ArrayImageAdapter(ctx, iconList, 48)
        val selectedIcon = ctx.resources.getIdentifier(prefs.getString(
            getIndexedCustomOverlayPref(
                Prefs.CUSTOM_OVERLAY_IDX_ICON, index), "ic_custom_overlay"), "drawable", ctx.packageName)
        setSelection(iconList.indexOf(selectedIcon))
        dropDownWidth = ctx.resources.dpToPx(48).toInt()
        layoutParams = ViewGroup.LayoutParams(ctx.resources.dpToPx(100).toInt(), ctx.resources.dpToPx(48).toInt())
    }
    val filterText = TextView(ctx).apply {
        text = "${ctx.getString(R.string.custom_overlay_filter_info)} ℹ️"
        setPadding(padding, 2 * padding, padding, 0)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        setTextColor(ContextCompat.getColor(ctx, R.color.accent))
        setOnClickListener {
            val dialog = AlertDialog.Builder(ctx)
                .setMessage(R.string.custom_overlay_filter_message)
                .setPositiveButton(R.string.close, null)
                .setNeutralButton("link") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, "https://github.com/Helium314/SCEE/blob/modified/CONTRIBUTING_A_NEW_QUEST.md#element-selection".toUri())
                    try {
                        ContextCompat.startActivity(ctx, intent, null)
                    } catch (_: Exception) { }
                }
                .create()
            dialog.show()
        }
    }
    val overlayFilter = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, index), "").split(" with ").takeIf { it.size == 2 }
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
    tag.doAfterTextChanged { text ->
        if (text == null || text.count { it == '(' } != text.count { it == ')' }) {
            d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
            return@doAfterTextChanged
        }
        try {
            filterString().toElementFilterExpression()
            d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
        }
        catch (e: Exception) { // for some reason catching import de.westnordost.streetcomplete.data.elementfilter.ParseException is not enough (#386), though I cannot reproduce it
            Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = tag.text.isEmpty()
        }
    }
    val colorText = TextView(ctx).apply {
        text = "${ctx.getString(R.string.custom_overlay_color_info)} ℹ️"
        setPadding(padding, 2 * padding, padding, 0)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        setTextColor(ContextCompat.getColor(ctx, R.color.accent))
        setOnClickListener {
            val dialog = AlertDialog.Builder(ctx)
                .setMessage(R.string.custom_overlay_color_message)
                .setPositiveButton(R.string.close, null)
                .create()
            dialog.show()
        }
    }
    val color = EditText(ctx).apply {
        setHint(R.string.custom_overlay_color_hint)
        setText(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, index), ""))
        doAfterTextChanged {text ->
            if (text == null || text.count { it == '(' } != text.count { it == ')' }) {
                d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                return@doAfterTextChanged
            }
            try {
                text.toString().toRegex()
                d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
            }
            catch (e: Exception) {
                Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
            }
        }
    }
    val highlightMissingSwitch = SwitchCompat(ctx).apply {
        setText(R.string.custom_overlay_highlight_missing)
        isChecked = prefs.getBoolean(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_HIGHLIGHT_MISSING_DATA, index), true)
    }
    val dashFilter = EditText(ctx).apply {
        setHint(R.string.custom_overlay_dash_filter_hint)
        setText(prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_DASH_FILTER, index), ""))
        doAfterTextChanged { text ->
            if (text.isNullOrBlank()) {
                d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
                return@doAfterTextChanged
            }

            if (text.count { it == '(' } != text.count { it == ')' }) {
                d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                return@doAfterTextChanged
            }
            try {
                "ways with $text".toElementFilterExpression()
                d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
            }
            catch (e: Exception) {
                Toast.makeText(ctx, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = tag.text.isEmpty()
            }
        }
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
        addView(highlightMissingSwitch)
        addView(dashFilter)
    }
    val indices = getCustomOverlayIndices(prefs).sorted()
    val b = AlertDialog.Builder(ctx)
        .setTitle(R.string.custom_overlay_title)
        .setViewWithDefaultPadding(ScrollView(ctx).apply { addView(linearLayout) })
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            // update prefs and enable this overlay
            prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, index), filterString())
            prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, index), color.text.toString())
            prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), title.text.toString())
            prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, index), ctx.resources.getResourceEntryName(iconList[iconSpinner.selectedItemPosition]))
            prefs.putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_DASH_FILTER, index), dashFilter.text.toString())
            prefs.putBoolean(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_HIGHLIGHT_MISSING_DATA, index), highlightMissingSwitch.isChecked)
            if (index !in indices) { // add if it's new, and select it immediately
                prefs.putString(Prefs.CUSTOM_OVERLAY_INDICES, (indices + index).joinToString(","))
                prefs.putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, index)
            }
            onChanged(index == prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)) // todo: also if overlay is new?
        }
    if (index in indices)
        b.setNeutralButton(R.string.delete_confirmation) { _, _ ->
            val overlayName = prefs.getString(
                getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, index), ctx.getString(
                    R.string.custom_overlay_title))
            AlertDialog.Builder(ctx)
                .setMessage(ctx.getString(R.string.custom_overlay_delete, overlayName))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_confirmation) { _, _ ->
                    val isActive = prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0) == index
                    prefs.keys.forEach { if (it.startsWith("custom_overlay_${index}_")) prefs.remove(it) }
                    prefs.putString(Prefs.CUSTOM_OVERLAY_INDICES, indices.filterNot { it == index }.joinToString(","))
                    if (isActive)
                        prefs.putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
                    onDeleted(isActive)
                }
                .show()
        }
    d = b.create()
    d.show()
}

// creates dummy overlays for the custom overlay, so they can be displayed to the user
// title is invalid resId 0
// name and wikiLink are the overlay index as stored in shared preferences
// changesetComment is the overlay title
fun getFakeCustomOverlays(prefs: ObservableSettings, ctx: Context, onlyIfExpertMode: Boolean = true): List<Overlay> {
    if (onlyIfExpertMode && !prefs.getBoolean(Prefs.EXPERT_MODE, false)) return emptyList()
    return prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0").split(",").mapNotNull { index ->
        val i = index.toIntOrNull() ?: return@mapNotNull null
        object : Overlay {
            override fun getStyledElements(mapData: MapDataWithGeometry) = emptySequence<Pair<Element, Style>>()
            override fun createForm(element: Element?) = null
            override val changesetComment = prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_NAME, i), "")
                .ifBlank { ctx.getString(R.string.custom_overlay_title) } // displayed overlay name
            override val icon = ctx.resources.getIdentifier(
                prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, i), "ic_custom_overlay"),
                "drawable", ctx.packageName
            ).takeIf { it != 0 } ?: R.drawable.ic_custom_overlay
            override val title = 0 // use invalid resId placeholder, the adapter needs to be aware of this
            override val name = index // allows to uniquely identify an overlay
            override val wikiLink = index
            override fun equals(other: Any?): Boolean {
                return if (other !is Overlay) false
                    else wikiLink == other.wikiLink // we only care about index!
            }
        }
    }
}
