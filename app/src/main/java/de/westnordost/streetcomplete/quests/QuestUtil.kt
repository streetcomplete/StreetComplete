package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Html
import android.text.Spanned
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.core.text.parseAsHtml
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.ktx.toList
import java.util.Locale
import java.util.concurrent.FutureTask

fun Resources.getQuestTitle(questType: QuestType<*>, element: Element?, featureDictionaryFuture: FutureTask<FeatureDictionary>?): String {
    val localeList = ConfigurationCompat.getLocales(configuration)
    val arguments = getTemplateArguments(questType, element, localeList, featureDictionaryFuture)
    return getString(getQuestTitleResId(questType, element), *arguments)
}

fun Resources.getHtmlQuestTitle(questType: QuestType<*>, element: Element?, featureDictionaryFuture: FutureTask<FeatureDictionary>?): Spanned {
    val localeList = ConfigurationCompat.getLocales(configuration)
    val arguments = getTemplateArguments(questType, element, localeList, featureDictionaryFuture)
    val spannedArguments = arguments.map {"<i>" + Html.escapeHtml(it) + "</i>"}.toTypedArray()
    return getString(getQuestTitleResId(questType, element), *spannedArguments).parseAsHtml()
}

private fun getTemplateArguments(
    questType: QuestType<*>,
    element: Element?,
    localeList: LocaleListCompat,
    featureDictionaryFuture: FutureTask<FeatureDictionary>?
): Array<String> {
    val tags = element?.tags ?: emptyMap()
    val typeName = lazy { findTypeName(element, featureDictionaryFuture, localeList) }
    return ((questType as? OsmElementQuestType<*>)?.getTitleArgs(tags, typeName)) ?: emptyArray()
}

private fun findTypeName(
    element: Element?,
    featureDictionaryFuture: FutureTask<FeatureDictionary>?,
    localeList: LocaleListCompat
): String? {
    val dict = featureDictionaryFuture?.get() ?: return null
    val locales = localeList.toList().toMutableList()
    /* add fallback to English if (some) English is not part of the locale list already as the
       fallback for text is also always English in this app (strings.xml) independent of, or rather
       additionally to what is in the user's LocaleList. */
    if (locales.none { it.language == Locale.ENGLISH.language }) {
        locales.add(Locale.ENGLISH)
    }
    return dict
        .byTags(element?.tags ?: emptyMap())
    // not for geometry because at this point we cannot tell apart points and vertices
    //    .forGeometry(element?.geometryType)
        .isSuggestion(false)
        .forLocale(*locales.toTypedArray())
        .find()
        .firstOrNull()
        ?.name
}

private fun getQuestTitleResId(questType: QuestType<*>, element: Element?) =
    (questType as? OsmElementQuestType<*>)?.getTitle(element?.tags ?: emptyMap()) ?: questType.title

fun getNameOrBrandOrOperatorOrRef(tags: Map<String, String>): String? {
    val name = tags["name"]
    val brand = tags["brand"]
    val ref = tags["ref"]
    val operator = tags["operator"]

    return when {
        name != null -> name
        brand != null -> brand
        // special special: If both operator and ref are available, show them both
        operator != null && ref != null -> "$operator $ref"
        operator != null -> operator
        ref != null -> ref
        else -> null
    }
}
