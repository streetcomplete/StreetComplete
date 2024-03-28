package de.westnordost.streetcomplete.quests

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.text.HtmlCompat
import androidx.core.widget.doAfterTextChanged
import com.github.difflib.text.DiffRow.Tag
import com.github.difflib.text.DiffRowGenerator
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.ParseException
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import java.util.regex.PatternSyntaxException

// restarts are typically necessary on changes of element selection because the filter is created by lazy
// quests settings should follow the pattern: qs_<quest_name>_<something>, e.g. "qs_AddLevel_more_levels"
// when to call reloadQuestTypes: if whatever is changed is not read from settings every time, or if dynamic quest creation is enabled

/** for setting values of a single key, comma separated */
fun singleTypeElementSelectionDialog(
    context: Context,
    prefs: SharedPreferences,
    pref: String,
    defaultValue: String,
    messageId: Int,
    onChanged: () -> Unit = { OsmQuestController.reloadQuestTypes() }
): AlertDialog {
    val textInput = EditText(context)
    val dialog = dialog(context, messageId, prefs.getString(pref, defaultValue)!!.replace("|",", "), textInput)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            val prefText = textInput.text.toString().split(",").joinToString("|") { it.trim() }
            if (prefs.getString(pref, defaultValue) == prefText) return@setPositiveButton
            prefs.edit().putString(pref, prefText).apply()
            onChanged()
        }
        .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
            prefs.edit().remove(pref).apply()
            onChanged()
        }
        .create()
    textInput.doAfterTextChanged {
        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button?.isEnabled = textInput.text.toString().let {
            it.lowercase().matches(valueRegex)
                && !it.trim().endsWith(',')
                && !it.contains(",,")
                && it.isNotEmpty() }
    }
    dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(pref) }
    return dialog
}

/** for setting values of a single number */
fun numberSelectionDialog(context: Context, prefs: SharedPreferences, pref: String, defaultValue: Int, messageId: Int): AlertDialog {
    val numberInput = EditText(context)
    numberInput.inputType = InputType.TYPE_CLASS_NUMBER
    numberInput.setText(prefs.getInt(pref, defaultValue).toString())
    val dialog = AlertDialog.Builder(context)
        .setMessage(messageId)
        .setViewWithDefaultPadding(numberInput)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok) { _,_ ->
            numberInput.text.toString().toIntOrNull()?.let {
                if (it == prefs.getInt(pref, it)) return@setPositiveButton
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
    numberInput.doAfterTextChanged {
        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button?.isEnabled = numberInput.text.toString().let { it.toIntOrNull() != null }
    }
    dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(pref) }
    return dialog
}

/** For setting full element selection.
 *  This will check validity of input and only allow saving selection can be parsed.
 */
fun fullElementSelectionDialog(context: Context, prefs: SharedPreferences, pref: String, messageId: Int, defaultValue: String): AlertDialog {
    val textInput = EditText(context)
    val checkPrefix = if (pref.endsWith("_full_element_selection")) "" else "nodes with "

    val message = HtmlCompat.fromHtml(context.getString(messageId), HtmlCompat.FROM_HTML_MODE_LEGACY)

    val dialog = dialog(context, messageId, prefs.getString(pref, defaultValue.trimIndent())!!, textInput)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            if (textInput.text.toString() == prefs.getString(pref, defaultValue.trimIndent())) return@setPositiveButton
            prefs.edit().putString(pref, textInput.text.toString()).apply()
            OsmQuestController.reloadQuestTypes()
        }
        .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
            prefs.edit().remove(pref).apply()
            OsmQuestController.reloadQuestTypes()
        }
        .setMessage(message)
        .setView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(textInput)
            if (prefs.contains(pref))
                addView(getDiffButton(context, defaultValue) { textInput.text.toString() })
        })
        .create()
    textInput.doAfterTextChanged {
        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val isValidFilterExpression by lazy {
            try {
                (checkPrefix + it).toElementFilterExpression()
                true
            } catch(e: ParseException) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                false
            } catch(e: PatternSyntaxException) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                false
            }
        }
        button?.isEnabled = textInput.text.toString().let {
            // check other stuff first, because creation filter expression is relatively slow
            (checkPrefix.isEmpty() || it.lowercase().matches(elementSelectionRegex))
                && it.count { c -> c == '('} == it.count { c -> c == ')'}
                && (it.contains('=') || it.contains('~') || it.contains('!'))
                && isValidFilterExpression
        }
    }
    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(pref) // disable reset button if setting is default
        dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance() // make the link actually open a browser
    }
    return dialog
}

private fun getDiffButton(context: Context, defaultText: String, getCurrentText: () -> String) =
    Button(context).apply {
        setText(R.string.quest_settings_highlight_changes_button)
        setOnClickListener {
            val drg = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .ignoreWhiteSpaces(true)
                .oldTag { f -> if (f) "<b><i><del>" else "</del></i></b>" }
                .newTag { f -> if (f) "<b><u><ins>" else "</ins></u></b>" }
                .build()
            val thatSpace = " "
            val newDefault = defaultText.replace("|", "$thatSpace|$thatSpace") // replace with (nearly) invisible space, so word differences are used
            val newCurrent = getCurrentText().replace("|", "$thatSpace|$thatSpace")
            val diffRows = drg.generateDiffRows(newDefault.split("\n"), newCurrent.split("\n"))
            val diffText = diffRows.mapNotNull {
                if (it.tag == Tag.EQUAL) return@mapNotNull null
                it.oldLine
            }.joinToString("<br>")
            AlertDialog.Builder(context)
                .setMessage(HtmlCompat.fromHtml(diffText, HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setNegativeButton(R.string.close, null)
                .show()
        }
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
    val padding = context.resources.dpToPx(8).toInt()
    input.setPadding(2 * padding, padding, 2 * padding, padding) // should be less than default padding to allow more text per line
    input.setText(initialValue)
    input.maxLines = 15 // if lines are not limited, the edit text might get so big that buttons are off screen (thanks, google for allowing this)
    return AlertDialog.Builder(context)
        .setMessage(messageId)
        .setView(input)
        .setNegativeButton(android.R.string.cancel, null)
}

fun getLabelOrElementSelectionDialog(context: Context, questType: OsmFilterQuestType<*>, prefs: SharedPreferences): AlertDialog {
    val description = TextView(context).apply {
        setText(R.string.quest_settings_dot_labels_message)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    }
    val prefWithPrefix = getPrefixedLabelSourcePref(questType, prefs)
    val labels = EditText(context).apply {
        setText(prefs.getString(prefWithPrefix, questType.dotLabelSources.joinToString(", ")))
    }
    var d: AlertDialog? = null
    d = AlertDialog.Builder(context)
        .setViewWithDefaultPadding(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(description)
            addView(labels)
            addView(Button(context).apply {
                setText(R.string.element_selection_button)
                setOnClickListener {
                    fullElementSelectionDialog(context, prefs, questType.getPrefixedFullElementSelectionPref(prefs), R.string.quest_settings_element_selection, questType.elementFilter).show()
                    d?.dismiss()
                }
            })
        })
        .setPositiveButton(android.R.string.ok) { _, _ ->
            labels.text.toString().split(",")
            prefs.edit { putString(prefWithPrefix, labels.text.toString()) }
            OsmQuestController.reloadQuestTypes()
        }
        .setNegativeButton(android.R.string.cancel, null)
        .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
            prefs.edit().remove(prefWithPrefix).apply()
            OsmQuestController.reloadQuestTypes()
        }.create()
    d.setOnShowListener { d.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(prefWithPrefix) } // disable reset button if setting is default
    return d
}

fun getLabelSources(defaultValue: String, questType: OsmFilterQuestType<*>, prefs: SharedPreferences) =
    prefs.getString(getPrefixedLabelSourcePref(questType, prefs),
        defaultValue
    )!!.split(",").map { it.trim() }

private fun getPrefixedLabelSourcePref(questType: OsmElementQuestType<*>, prefs: SharedPreferences) = "${questPrefix(prefs)}qs_${questType.name}_label_sources"

fun questPrefix(prefs: SharedPreferences) = if (prefs.getBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, false))
    prefs.getLong(Prefs.SELECTED_QUESTS_PRESET, 0).toString() + "_"
else
    ""

fun questPrefix(prefs: ObservableSettings) = if (prefs.getBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, false))
    prefs.getLong(Prefs.SELECTED_QUESTS_PRESET, 0).toString() + "_"
else
    ""

fun OsmElementQuestType<*>.getPrefixedFullElementSelectionPref(prefs: SharedPreferences) = "${questPrefix(prefs)}qs_${name}_full_element_selection"

private val valueRegex = "[a-z\\d_?,/\\s]+".toRegex()

// relax a little bit? but e.g. A-Z is very uncommon and might lead to mistakes
private val elementSelectionRegex = "[a-z\\d_=!?\"~*\\[\\]()|:.,<>\\s+-]+".toRegex()
