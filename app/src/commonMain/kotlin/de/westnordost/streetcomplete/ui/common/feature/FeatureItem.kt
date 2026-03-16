package de.westnordost.streetcomplete.ui.common.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.allDrawableResources
import de.westnordost.streetcomplete.resources.preset_maki_marker_stroked
import de.westnordost.streetcomplete.ui.common.feature.buildAnnotatedName
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import org.jetbrains.compose.resources.painterResource

/** Displays an OSM [feature] in the region specified by the given [countryCode].
 *
 *  If the [searchText] matches any characters of the feature's name, the matched section will be
 *  highlighted accordingly.
 *  */
@Composable
fun FeatureItem(
    feature: Feature,
    featureDictionary: FeatureDictionary,
    countryCode: String?,
    modifier: Modifier = Modifier,
    searchText: String? = null,
    iconSize: Dp = 30.dp
) {
    val color = LocalContentColor.current
    val languages = remember { getLanguagesForFeatureDictionary() }
    val parentFeature = if (feature.isSuggestion) {
        featureDictionary.getParentOfById(
            id = feature.id,
            languages = languages,
            country = countryCode
        )
    } else null

    // brand features usually don't have an own icon, so, we fall back to parent feature, e.g. for
    // Aldi, use icon of shop/supermarket. Finally, if there is no icon at all, use a
    // placeholder
    val iconResourceName = feature.iconResourceName ?: parentFeature?.iconResourceName
    val icon = Res.allDrawableResources[iconResourceName] ?: Res.drawable.preset_maki_marker_stroked

    val annotatedName = remember(searchText, color) {
        feature.buildAnnotatedName(searchText = searchText, color = color)
    }
    // brand features first have their name, then in second line the name of the parend feature
    val annotatedText = if (feature.isSuggestion && parentFeature != null) {
        buildAnnotatedString {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(annotatedName) }
            append("\n")
            append(parentFeature.name)
        }
    } else {
        annotatedName
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = iconResourceName,
            modifier = Modifier.size(iconSize)
        )
        Text(
            text = annotatedText,
            style = LocalTextStyle.current.copy(hyphens = Hyphens.Auto)
        )
    }
}

private val Feature.iconResourceName: String? get() =
    icon?.let { "preset_" + it.replace('-', '_') }

/** The feature's name but with the search text highlighted. Also, when the feature has several
 *  names (aliases), the name that matches the search text is displayed instead of the "primary"
 *  name. */
private fun Feature.buildAnnotatedName(
    searchText: String?,
    color: Color
): AnnotatedString = buildAnnotatedString {
    // no search: just show name
    if (searchText.isNullOrEmpty()) {
        append(name)
        return@buildAnnotatedString
    }
    val matchedName = findMatchedName(searchText)
    // search but no match with name (likely, match with term): show name not in bold
    if (matchedName == null) {
        append(name)
        return@buildAnnotatedString
    }

    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))

    val searchTextContainsSpaces = searchText.contains(' ')
    val matchColor = color.copy(alpha = 0.38f)
    if (!searchTextContainsSpaces) {
        for (word in matchedName.split(' ')) {
            if (word.startsWith(searchText, ignoreCase = true)) {
                withStyle(SpanStyle(color = matchColor)) {
                    append(word.substring(0, searchText.length))
                }
                append(word.substring(searchText.length))
            } else {
                append(word)
            }
            append(' ')
        }
    } else {
        if (matchedName.startsWith(searchText, ignoreCase = true)) {
            withStyle(SpanStyle(color = matchColor)) {
                append(matchedName.substring(0, searchText.length))
            }
            append(matchedName.substring(searchText.length))
        } else {
            append(matchedName)
        }
    }
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

/** Returns the feature associated with the parent of the given [id] or null if it does not exist.
 *  Otherwise, same parameters as getById */
private fun FeatureDictionary.getParentOfById(
    id: String,
    languages: List<String?>? = null,
    country: String? = null
): Feature? =
    getById(
        id = id.substringBeforeLast('/'),
        languages = languages,
        country = country
    )
