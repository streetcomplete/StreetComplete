package de.westnordost.streetcomplete.view.localized_name

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.core.text.parseAsHtml
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.Abbreviations
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

fun confirmNoName(context: Context, onConfirmed: () -> Unit) {
    AlertDialog.Builder(context)
        .setTitle(R.string.quest_name_answer_noName_confirmation_title)
        .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> onConfirmed() }
        .setNegativeButton(R.string.quest_generic_confirmation_no, null)
        .show()
}

/** Get possible abbreviations in the given localized names */
fun getPossibleAbbreviations(
    localizedNames: List<LocalizedName>,
    defaultLanguage: String?,
    abbreviationsByLanguage: Map<String, Abbreviations?>,
): List<String> {
    val possibleAbbreviations = ArrayList<String>()
    for (localizedName in localizedNames) {
        val languageTag = localizedName.languageTag
            .takeIf { it.isNotEmpty() } ?: defaultLanguage.orEmpty()
        val name = localizedName.name

        val abbr = abbreviationsByLanguage[languageTag]
        val containsLocalizedAbbreviations = abbr?.containsAbbreviations(name) == true

        if (name.contains(".") || containsLocalizedAbbreviations) {
            possibleAbbreviations.add(name)
        }
    }
    return possibleAbbreviations
}

fun confirmPossibleAbbreviationsIfAny(context: Context, names: ArrayDeque<String>, onConfirmedAll: () -> Unit) {
    if (names.isEmpty()) {
        onConfirmedAll()
    } else {
        /* recursively call self on confirm until the list of not-abbreviations to confirm is
           through */
        val name = names.removeFirst()
        confirmPossibleAbbreviation(context, name) {
            confirmPossibleAbbreviationsIfAny(context, names, onConfirmedAll)
        }
    }
}

fun confirmPossibleAbbreviation(context: Context, name: String, onConfirmed: () -> Unit) {
    val title = context.getString(
        R.string.quest_streetName_nameWithAbbreviations_confirmation_title_name,
        "<i>" + Html.escapeHtml(name) + "</i>"
    ).parseAsHtml()

    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
        .setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive) { _, _ -> onConfirmed() }
        .setNegativeButton(R.string.quest_generic_confirmation_no, null)
        .show()
}

fun showKeyboardInfo(context: Context) {
    AlertDialog.Builder(context)
        .setTitle(R.string.quest_streetName_cantType_title)
        .setMessage(R.string.quest_streetName_cantType_description)
        .setPositiveButton(R.string.quest_streetName_cantType_open_settings) { _, _ ->
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        .setNeutralButton(R.string.quest_streetName_cantType_open_store) { _, _ ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_MARKET)
            context.startActivity(intent)
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}
