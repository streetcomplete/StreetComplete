package de.westnordost.streetcomplete.view.controller

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.view.presetIconIndex

/** Just displays a OSM feature */
class FeatureViewController(
    private val featureDictionary: FeatureDictionary,
    private val textView: TextView,
    private val iconView: ImageView
) {
    private val locales = getLocalesForFeatureDictionary(textView.resources.configuration)

    var countryOrSubdivisionCode: String? = null

    var searchText: String? = null
        set(value) {
            if (field == value) return
            field = value
            update()
        }

    var feature: Feature? = null
        set(value) {
            if (field == value) return
            field = value
            update()
        }

    init {
        update()
    }

    private fun update() {
        val feature = feature
        val context = iconView.context
        if (feature == null) {
            textView.text = null
            textView.setHint(R.string.quest_select_hint)
            val questionDrawable = iconView.context.getDrawable(R.drawable.ic_question_24dp)
            questionDrawable?.alpha = 64
            iconView.setImageDrawable(questionDrawable)
        } else if (feature.isSuggestion) {
            val parentFeature = getParentFeature(feature)
            val text = SpannableStringBuilder()
            text.italic { appendName(textView.context, feature, searchText) }
            parentFeature?.name?.let { text.append("\n$it") }
            textView.text = text
            iconView.setImageDrawable(parentFeature?.getIconDrawable(context))
        } else {
            textView.text = SpannableStringBuilder().appendName(textView.context, feature, searchText)
            iconView.setImageDrawable(feature.getIconDrawable(context))
        }
    }

    private fun getParentFeature(feature: Feature): Feature? =
        featureDictionary
            .byId(feature.id.substringBeforeLast('/'))
            .inCountry(countryOrSubdivisionCode)
            .forLocale(*locales)
            .get()
}

private fun SpannableStringBuilder.appendName(context: Context, feature: Feature, searchText: String?): SpannableStringBuilder {
    if (searchText == null) {
        bold { append(feature.name) }
        return this
    }

    val matchedName = feature.findMatchedName(searchText)

    if (matchedName == null) {
        append(feature.name)
        return this
    }

    bold {
        val matchColor = ContextCompat.getColor(context, R.color.matched_search_text)
        val searchTextContainsSpaces = searchText.contains(' ')
        if (!searchTextContainsSpaces) {
            for (word in matchedName.split(' ')) {
                if (word.startsWith(searchText, ignoreCase = true)) {
                    color(matchColor) { append(word.substring(0, searchText.length)) }
                    append(word.substring(searchText.length))
                } else {
                    append(word)
                }
                append(' ')
            }
        } else {
            if (matchedName.startsWith(searchText, ignoreCase = true)) {
                color(matchColor) { append(matchedName.substring(0, searchText.length)) }
                append(matchedName.substring(searchText.length))
            } else {
                append(matchedName)
            }
        }
    }
    return this
}

private fun Feature.findMatchedName(searchText: String): String? {
    val searchTextContainsSpaces = searchText.contains(' ')
    for (name in names) {
        if (!searchTextContainsSpaces) {
            for (word in name.split(' ')) {
                if (word.startsWith(searchText, ignoreCase = true)) {
                    return name
                }
            }
        } else {
            if (name.startsWith(searchText, ignoreCase = true)) {
                return name
            }
        }
    }
    return null
}

private fun Feature.getIconDrawable(context: Context): Drawable? {
    if (icon == null) return null
    val id = presetIconIndex[icon] ?: return null
    return context.getDrawable(id)
}
