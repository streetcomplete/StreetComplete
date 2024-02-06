package de.westnordost.streetcomplete.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.databinding.DialogAccessManagerBinding
import de.westnordost.streetcomplete.databinding.RowAccessBinding
import de.westnordost.streetcomplete.util.dialogs.showAddConditionalDialog
import de.westnordost.streetcomplete.util.ktx.dpToPx

class AccessManagerDialog(context: Context, tags: Map<String, String>, onClickOk: (StringMapChangesBuilder) -> Unit) : AlertDialog(context) {
    private val binding = DialogAccessManagerBinding.inflate(LayoutInflater.from(context))
    private val originalAccessTags = tags.filterKeys { key -> accessKeys.any { it == key || key.startsWith("$it:") } }
    private val newAccessTags = LinkedHashMap(originalAccessTags)

    init {
        binding.addConditionalButton.setOnClickListener {
            showAddConditionalDialog(context, accessKeys.toList(), listOf("yes", "no", "delivery", "destination"), null) { k, v ->
                newAccessTags[k] = v
                createAccessTagViews()
            }
        }
        binding.addButton.setOnClickListener { showAddAccessDialog(context) }
        createAccessTagViews()
        setMessage(context.getString(R.string.access_manager_message))
        setView(binding.root)
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ -> }
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            val builder = StringMapChangesBuilder(tags)
            newAccessTags.forEach {
                if (originalAccessTags[it.key] != it.value)
                    builder[it.key] = it.value
            }
            originalAccessTags.keys.forEach {
                if (it !in newAccessTags)
                    builder.remove(it)
            }
            onClickOk(builder)
        }
    }

    private fun updateOkButton() {
        getButton(BUTTON_POSITIVE).isEnabled = originalAccessTags != newAccessTags
    }

    private fun createAccessTagViews() {
        binding.accessTags.removeAllViews()
        newAccessTags.forEach { binding.accessTags.addView(accessView(it.key, it.value)) }
    }

    private fun accessView(key: String, value: String): View {
        val view = RowAccessBinding.inflate(LayoutInflater.from(context))
        view.keyText.text = key
        val values = if (value in accessValues) accessValues else (arrayOf(value) + accessValues)
        view.valueSpinner.adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_dropdown_item_1line, values)
        view.valueSpinner.setSelection(accessValues.indexOf(value))
        view.valueSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = values[position]
                newAccessTags[key] = selected
                updateOkButton()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { } // just do nothing? or remove tag?
        }
        view.deleteButton.setOnClickListener {
            newAccessTags.remove(key)
            createAccessTagViews()
        }
        view.root.setPadding(0, context.dpToPx(4).toInt(), 0, context.dpToPx(4).toInt())
        return view.root
    }

    // maybe reduce height, but need a simple solution...
    private fun showAddAccessDialog(context: Context) {
        Builder(context)
            .setTitle("key")
            .setSingleChoiceItems(accessKeys, -1) { di, i ->
                Builder(context)
                    .setTitle("value")
                    .setSingleChoiceItems(accessValues, -1) { di2, j ->
                        newAccessTags[accessKeys[i]] = accessValues[j]
                        createAccessTagViews()
                        di2.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                di.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

val accessKeys = arrayOf( // sorted by number of uses
    "access", // 18m
    "foot", // 7m
    "bicycle", // 7m
    "bus", // 3.5m
    "motor_vehicle", // 2m
    "horse", // 1.6m
    "hgv", // 790k
    "motorcar", // 590k
    "motorcycle", // 580k
    "vehicle", // 350k
    "moped", // 235k
    "mofa", // 200k
    "golf_cart", // 158k
    "psv", // 115k
    "hazmat", // 87k
    "dog", // 80k
    "bdouble", // 60k
    "ski", // 60k
    "goods", // 41k
    "taxi", // 23k
    "carriage", // 20k
    "hov", // 20k
    "disabled", // 13.5k
    "tourist_bus", // 13k
    "atv", // 12k
    "hand_cart", // 6.8k
    "inline_skates", // 5k
    "speed_pedelec", // 3.7k
    "motorhome", // 3.5k
    "trailer", // 2.7k
    "ohv", // 2.4k
    "caravan", // 2k
    "coach", // 1.7k
    "carpool", // 1.5k
    "hgv_articulated", // 1k
    "small_electric_vehicle", // 800
    "auto_rickshaw", // 625
    "electric_bicycle", // 335
    "cycle_rickshaw", // 78
    "nev", // 62
    "kick_scooter", // 60
)

val accessValues = arrayOf(
    "yes",
    "no",
    "private",
    "permissive",
    "permit",
    "destination",
    "delivery",
    "customers",
    "designated", // not for access
    "use_sidepath", // usually for foot / bicycle
    "dismount", // bicycle
    "agricultural",
    "forestry",
    "discouraged", // really required explicit sign
    //"variable", doesn't make sense without supporting access:lanes
)
