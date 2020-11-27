package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Html
import android.text.Spanned
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.core.text.parseAsHtml
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.ktx.toTypedArray
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
    val typeName = lazy { findTypeName(tags, featureDictionaryFuture, localeList) }
    return ((questType as? OsmElementQuestType<*>)?.getTitleArgs(tags, typeName)) ?: emptyArray()
}

private fun findTypeName(
    tags: Map<String, String>,
    featureDictionaryFuture: FutureTask<FeatureDictionary>?,
    localeList: LocaleListCompat
): String? {
    val dict = featureDictionaryFuture?.get() ?: return null
    return dict
        .byTags(tags)
        .isSuggestion(false)
        .forLocale(*localeList.toTypedArray(), null)
        .find()
        .firstOrNull()
        ?.name
}

private fun getQuestTitleResId(questType: QuestType<*>, element: Element?) =
    (questType as? OsmElementQuestType<*>)?.getTitle(element?.tags ?: emptyMap()) ?: questType.title
