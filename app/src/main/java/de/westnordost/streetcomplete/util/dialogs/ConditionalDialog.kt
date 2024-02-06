package de.westnordost.streetcomplete.util.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHours
import de.westnordost.streetcomplete.quests.opening_hours.TimeRangePickerDialog
import de.westnordost.streetcomplete.quests.opening_hours.WeekdaysPickerDialog
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.util.ktx.showKeyboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("KotlinConstantConditions") // because this is simply incorrect...
@SuppressLint("SetTextI18n") // this is the value, and should absolutely not be translated
fun showAddConditionalDialog(context: Context, keys: List<String>, values: List<String>?, valueInputType: Int?, onClickOk: (String, String) -> Unit) {
    var key = ""
    var value = ""
    val conditions = mutableMapOf<String, String>() // key is time, weight, length,... and values are the limitation strings
    var dialog: AlertDialog? = null

    fun isOk(text: String): Boolean =
        key.isNotBlank()
            && ((values != null && text.substringBefore(" @") in values) || text.substringBefore("@").isNotBlank())
            && text.contains('@')
            && text.count { c -> c == '('} == 1 && text.count { c -> c == ')'} == 1
            && "()" !in text

    val valueEditText = EditText(context).apply {
        doAfterTextChanged {
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isOk(it.toString())
        }
    }

    fun createFullValue() {
        valueEditText.setText("$value @ (${conditions.values.joinToString(" AND ")})")
    }

    val keySpinner = AppCompatSpinner(context).apply {
        adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, keys)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long, ) {
                key = keys[position]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
    }

    val valueView = if (values == null)
            EditText(context).apply {
                hint = "value"
                valueInputType?.let { inputType = it }
                doAfterTextChanged {
                    value = it.toString()
                    createFullValue()
                }
            }
        else
            AppCompatSpinner(context).apply {
                adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, values)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long, ) {
                        value = values[position]
                        createFullValue()
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) { }
                }
            }

    fun numericBox(type: String, textResId: Int): View {
        val box = CheckBox(context)
        var conditionText = ""
        val switch = SwitchCompat(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.4f)
            isEnabled = false
            text = "<"
            setOnCheckedChangeListener { _, b ->
                text = if (b) ">" else "<"
                conditions[type] = "$type$text$conditionText"
                createFullValue()
            }
        }
        box.apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f)
            setText(textResId)
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    // allow selecting < and >? just let the user type it manually for now
                    var textDialog: AlertDialog? = null
                    val text = EditText(context).apply {
                        hint = type
                        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        doAfterTextChanged { textDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = it?.toString()?.toFloatOrNull() != null }
                    }
                    textDialog = AlertDialog.Builder(context)
                        .setView(text)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            conditionText = text.text.toString()
                            conditions[type] = "$type${switch.text}${text.text}"
                            createFullValue()
                        }
                        .setOnCancelListener { isChecked = false }
                        .create()
                    textDialog.setOnShowListener {
                        dialog?.lifecycleScope?.launch {
                            delay(20) // without this, the keyboard sometimes isn't showing
                            text.requestFocus()
                            text.showKeyboard()
                        }
                        textDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                    }
                    textDialog.show()
                } else {
                    conditions.remove(type)
                    createFullValue()
                }
                switch.isEnabled = checked
            }
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(box)
            addView(switch)
        }
    }

    val timeBox = CheckBox(context).apply {
        setText(de.westnordost.streetcomplete.R.string.access_time_limit)
        setOnCheckedChangeListener { _, checked ->
            if (checked && "time" !in conditions) {
                // todo: use user preferred locale?
                val dW = WeekdaysPickerDialog.show(context, null, /*countryInfo.userPreferredLocale*/ context.resources.configuration.locale) { weekdays ->
                    val dT = TimeRangePickerDialog(
                        context,
                        context.getString(de.westnordost.streetcomplete.R.string.time_limited_from),
                        context.getString(de.westnordost.streetcomplete.R.string.time_limited_to),
                        TimeRange(8 * 60, 18 * 60, false),
                        DateFormat.is24HourFormat(context)
                    ) { timeRange ->
                        val oh = listOf(OpeningWeekdaysRow(weekdays, timeRange)).toOpeningHours()
                        conditions["time"] = oh.toString()
                        createFullValue()
                    }
                    dT.setOnDismissListener { isChecked = !conditions["time"].isNullOrBlank() }
                    dT.show()
                }
                dW.setOnDismissListener { isChecked = !conditions["time"].isNullOrBlank() }
                dW.show()
            } else if (!checked) {
                conditions.remove("time")
                createFullValue()
            }
        }
    }
    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(keySpinner)
        addView(valueView)
        // todo: more numeric things? there is stay, but this is not really numeric... has hours / minutes
        //  though this could be translated and only take minutes?
        addView(numericBox("weight", de.westnordost.streetcomplete.R.string.access_weight_limit))
        addView(numericBox("length", de.westnordost.streetcomplete.R.string.access_length_limit))
        addView(numericBox("width", de.westnordost.streetcomplete.R.string.access_width_limit))
        addView(timeBox)
        addView(valueEditText)
    }
    dialog = AlertDialog.Builder(context)
        .setViewWithDefaultPadding(layout)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            val fullValue = valueEditText.text.toString()
            if (isOk(fullValue))
                onClickOk("$key:conditional", fullValue)
        }
        .setNegativeButton(android.R.string.cancel, null)
        .create()
    dialog.show()
}

// similar, but with some access tags instead of numeric restrictions
// todo: maybe no key list necessary, and maybe no value list too?
fun showOtherConditionalDialog(context: Context, keys: List<String>, values: List<String>?, valueInputType: Int?, onClickOk: (String, String) -> Unit) {
    var key = ""
    var value = ""
    val conditions = mutableMapOf<String, String>() // key is time, weight, length,... and values are the limitation strings
    var dialog: AlertDialog? = null

    fun isOk(text: String): Boolean =
        key.isNotBlank()
            && ((values != null && text.substringBefore(" @") in values) || text.substringBefore("@").isNotBlank())
            && text.contains('@')
            && text.count { c -> c == '('} == 1 && text.count { c -> c == ')'} == 1
            && "()" !in text

    val valueEditText = EditText(context).apply {
        doAfterTextChanged {
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = isOk(it.toString())
        }
    }

    fun createFullValue() {
        valueEditText.setText("$value @ (${conditions.values.joinToString(" AND ")})")
    }

    val keySpinner = AppCompatSpinner(context).apply {
        adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, keys)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long, ) {
                key = keys[position]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
    }

    val valueView = if (values == null)
        EditText(context).apply {
            hint = "value, leave empty for none" // todo: string resource
            valueInputType?.let { inputType = it }
            doAfterTextChanged {
                value = it.toString().ifBlank { "none" }
                createFullValue()
            }
            value = "none"
        }
    else
        AppCompatSpinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, values)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long, ) {
                    value = values[position]
                    createFullValue()
                }
                override fun onNothingSelected(p0: AdapterView<*>?) { }
            }
        }

    val accessSpinner = AppCompatSpinner(context).apply {
        val v = listOf(context.getString(R.string.quest_select_hint), "destination", "delivery", "agricultural", "forestry", "private")
        adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, v)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long, ) {
                if (position == 0)
                    conditions.remove("access")
                else
                    conditions["access"] = v[position]
                createFullValue()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
    }

    val timeBox = CheckBox(context).apply {
        setText(de.westnordost.streetcomplete.R.string.access_time_limit)
        setOnCheckedChangeListener { _, checked ->
            if (checked && "time" !in conditions) {
                val dW = WeekdaysPickerDialog.show(context, null, /*countryInfo.userPreferredLocale*/ context.resources.configuration.locale) { weekdays ->
                    val dT = TimeRangePickerDialog(
                        context,
                        context.getString(de.westnordost.streetcomplete.R.string.time_limited_from),
                        context.getString(de.westnordost.streetcomplete.R.string.time_limited_to),
                        TimeRange(8 * 60, 18 * 60, false),
                        DateFormat.is24HourFormat(context)
                    ) { timeRange ->
                        val oh = listOf(OpeningWeekdaysRow(weekdays, timeRange)).toOpeningHours()
                        conditions["time"] = oh.toString()
                        createFullValue()
                    }
                    dT.setOnDismissListener { isChecked = !conditions["time"].isNullOrBlank() }
                    dT.show()
                }
                dW.setOnDismissListener { isChecked = !conditions["time"].isNullOrBlank() }
                dW.show()
            } else if (!checked) {
                conditions.remove("time")
                createFullValue()
            }
        }
    }
    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(keySpinner)
        addView(valueView)
        addView(timeBox)
        addView(accessSpinner)
        addView(valueEditText)
    }
    dialog = AlertDialog.Builder(context)
        .setViewWithDefaultPadding(layout)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            val fullValue = valueEditText.text.toString()
            if (isOk(fullValue))
                onClickOk("$key:conditional", fullValue)
        }
        .setNegativeButton(android.R.string.cancel, null)
        .create()
    dialog.show()
    createFullValue()
}
