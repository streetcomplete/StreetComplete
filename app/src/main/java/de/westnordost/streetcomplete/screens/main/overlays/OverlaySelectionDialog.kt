package de.westnordost.streetcomplete.screens.main.overlays

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
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
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.databinding.DialogOverlaySelectionBinding
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.view.setHtml
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Dialog in which the user selects which overlay to display */
class OverlaySelectionDialog(context: Context) : AlertDialog(context), KoinComponent {

    private val selectedOverlayController: SelectedOverlayController by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val prefs: SharedPreferences by inject()

    private val binding = DialogOverlaySelectionBinding.inflate(LayoutInflater.from(context))
    private var selectedOverlay: Overlay? = selectedOverlayController.selectedOverlay

    init {
        val adapter = OverlaySelectionAdapter()
        val expertMode = prefs.getBoolean(Prefs.EXPERT_MODE, false)
        adapter.overlays = overlayRegistry.filterNot { !expertMode && it is CustomOverlay }
        adapter.selectedOverlay = selectedOverlayController.selectedOverlay
        adapter.onSelectedOverlay = { selectedOverlay = it }
        binding.overlaysList.adapter = adapter
        binding.overlaysList.layoutManager = LinearLayoutManager(context)

        setTitle(R.string.select_overlay)

        setButton(BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
            selectedOverlayController.selectedOverlay = selectedOverlay
            dismiss()
        }

        if (expertMode)
            setButton(BUTTON_NEGATIVE, context.resources.getText(R.string.custom_overlay_title)) { _,_ ->
                showOverlayCustomizer(context)
                dismiss()
            }

        setView(binding.root)
    }

    @SuppressLint("SetTextI18n") // this is about element type, don't want translation here
    private fun showOverlayCustomizer(c: Context) {
        var d: AlertDialog? = null

        val overlayFilter = prefs.getString(Prefs.CUSTOM_OVERLAY_FILTER, "")?.split(" with ")?.takeIf { it.size == 2 }
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
            setHint(R.string.custom_overlay_hint)
            setText(overlayFilter?.get(1) ?: "")
        }
        val coloringText = TextView(c).apply {
            setText(R.string.custom_overlay_color_message)
        }
        val color = EditText(c).apply {
            setHint(R.string.custom_overlay_color_hint)
            setText(prefs.getString(Prefs.CUSTOM_OVERLAY_COLOR_KEY, "")!!)
            doAfterTextChanged {
                try {
                    it.toString().toRegex()
                    d?.getButton(BUTTON_POSITIVE)?.isEnabled = true
                }
                catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                d?.getButton(BUTTON_POSITIVE)?.apply { isEnabled = tag.text.isEmpty() }
            }
        }
        val linearLayout = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { setHtml(resources.getString(R.string.custom_overlay_message)) })
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
                    putString(Prefs.CUSTOM_OVERLAY_FILTER, filterString())
                    putString(Prefs.CUSTOM_OVERLAY_COLOR_KEY, color.text.toString())
                }
                if (selectedOverlay?.name == CustomOverlay::class.simpleName)
                    selectedOverlayController.apply { // trigger reload
                        val temp = selectedOverlay
                        selectedOverlay = null
                        selectedOverlay = temp
                    }
            }
            .create()
        d.show()
    }
}
