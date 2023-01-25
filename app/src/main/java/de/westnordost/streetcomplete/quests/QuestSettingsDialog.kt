package de.westnordost.streetcomplete.quests

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.ParseException
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController

// restarts are typically necessary on changes of element selection because the filter is created by lazy
// quests settings should follow the pattern: qs_<quest_name>_<something>, e.g. "qs_AddLevel_more_levels"
// when to call reloadQuestTypes: if whatever is changed is not read from settings every time, or if dynamic quest creation is enabled

/** for setting values of a single key, comma separated */
fun singleTypeElementSelectionDialog(context: Context, prefs: SharedPreferences, pref: String, defaultValue: String, messageId: Int, needsReload: Boolean = true): AlertDialog {
    var dialog: AlertDialog? = null
    val textInput = EditText(context)
    textInput.addTextChangedListener {
        val button = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        button?.isEnabled = textInput.text.toString().let {
            it.lowercase().matches(valueRegex)
                && !it.trim().endsWith(',')
                && !it.contains(",,")
                && it.isNotEmpty() }
    }
    dialog = dialog(context, messageId, prefs.getString(pref, defaultValue)?.replace("|",", ") ?: "", textInput)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            prefs.edit().putString(pref, textInput.text.toString().split(",").joinToString("|") { it.trim() }).apply()
            if (needsReload)
                OsmQuestController.reloadQuestTypes()
        }
        .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
            prefs.edit().remove(pref).apply()
            if (needsReload)
                OsmQuestController.reloadQuestTypes()
        }
        .create()
    dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(pref) }
    return dialog
}

/** for setting values of a single number */
fun numberSelectionDialog(context: Context, prefs: SharedPreferences, pref: String, defaultValue: Int, messageId: Int): AlertDialog {
    var dialog: AlertDialog? = null
    val numberInput = EditText(context)
    numberInput.inputType = InputType.TYPE_CLASS_NUMBER
    numberInput.addTextChangedListener {
        val button = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        button?.isEnabled = numberInput.text.toString().let { it.toIntOrNull() != null }
    }
    numberInput.setPaddingRelative(30,10,30,10)
    numberInput.setText(prefs.getInt(pref, defaultValue).toString())
    dialog = AlertDialog.Builder(context)
        .setMessage(messageId)
        .setView(numberInput)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok) { _,_ ->
            numberInput.text.toString().toIntOrNull()?.let {
                prefs.edit().putInt(pref, it).apply()
                if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
                    OsmQuestController.reloadQuestTypes()
            }
        }
        .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
            prefs.edit().remove(pref).apply()
            if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
                OsmQuestController.reloadQuestTypes()
        }
        .create()
    dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(pref) }
    return dialog
}

/** For setting full element selection.
 *  This will check validity of input and only allow saving selection can be parsed.
 */
fun fullElementSelectionDialog(context: Context, prefs: SharedPreferences, pref: String, messageId: Int, defaultValue: String? = null): AlertDialog {
    var dialog: AlertDialog? = null
    val textInput = EditText(context)
    val checkPrefix = if (pref.endsWith("_full_element_selection")) "" else "nodes with "
    textInput.addTextChangedListener {
        val button = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        val isValidFilterExpression by lazy {
            try {
                (checkPrefix + it).toElementFilterExpression()
                true
            } catch(e: ParseException) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                false
            }
        }
        button?.isEnabled = textInput.text.toString().let {
            // check other stuff first, because creation filter expression is relatively slow
            (checkPrefix.isEmpty() || it.lowercase().matches(elementSelectionRegex))
                && it.count { c -> c == '('} == it.count { c -> c == ')'}
                && (it.contains('=') || it.contains('~'))
                && isValidFilterExpression
        }
    }

    dialog = dialog(context, messageId, prefs.getString(pref, defaultValue?.trimIndent() ?: "") ?: "", textInput)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            prefs.edit().putString(pref, textInput.text.toString()).apply()
            OsmQuestController.reloadQuestTypes()
        }
        .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
            prefs.edit().remove(pref).apply()
            OsmQuestController.reloadQuestTypes()
        }
        .create()
    dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(pref) } // disable reset button if setting is default
    return dialog
}

fun booleanQuestSettingsDialog(context: Context, prefs: SharedPreferences, pref: String, messageId: Int, answerYes: Int, answerNo: Int): AlertDialog =
    AlertDialog.Builder(context)
        .setMessage(messageId)
        .setNeutralButton(android.R.string.cancel, null)
        .setPositiveButton(answerYes) { _,_ ->
            prefs.edit().putBoolean(pref, true).apply()
            OsmQuestController.reloadQuestTypes()
        }
        .setNegativeButton(answerNo) { _,_ ->
            prefs.edit().putBoolean(pref, false).apply()
            OsmQuestController.reloadQuestTypes()
        }
        .create()

private fun dialog(context: Context, messageId: Int, initialValue: String, input: EditText): AlertDialog.Builder {
    input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    input.setPadding(20,10,20,10)
    input.setText(initialValue)
    input.maxLines = 15 // if lines are not limited, the edit text might get so big that buttons are off screen (thanks, google for allowing this)
    return AlertDialog.Builder(context)
        .setMessage(messageId)
        .setView(input)
        .setNegativeButton(android.R.string.cancel, null)
}

fun questPrefix(prefs: SharedPreferences) = if (prefs.getBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, false))
    prefs.getLong(Prefs.SELECTED_QUESTS_PRESET, 0).toString() + "_"
else
    ""

private val valueRegex = "[a-z0-9_?,/\\s]+".toRegex()
private val elementSelectionRegex = "[a-z0-9_=!~()|:,<>\\s+-]+".toRegex() // todo: relax a little bit?
