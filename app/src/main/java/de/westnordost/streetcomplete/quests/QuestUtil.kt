package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Html
import android.text.Spanned
import androidx.core.os.ConfigurationCompat
import androidx.core.text.HtmlCompat
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.*
import java.util.concurrent.FutureTask

fun Resources.getQuestTitle(questType: QuestType<*>, element: Element?, featureDictionaryFuture: FutureTask<FeatureDictionary>?): String {
    val locale = ConfigurationCompat.getLocales(configuration)[0]
    val arguments = getTemplateArguments(questType, element, locale, featureDictionaryFuture)
    return getString(getQuestTitleResId(questType, element), *arguments)
}

fun Resources.getHtmlQuestTitle(questType: QuestType<*>, element: Element?, featureDictionaryFuture: FutureTask<FeatureDictionary>?): Spanned {
    val locale = ConfigurationCompat.getLocales(configuration)[0]
    val arguments = getTemplateArguments(questType, element, locale, featureDictionaryFuture)
    val spannedArguments = arguments.map {"<i>" + Html.escapeHtml(it) + "</i>"}.toTypedArray()
    return HtmlCompat.fromHtml(getString(getQuestTitleResId(questType, element), *spannedArguments),
        HtmlCompat.FROM_HTML_MODE_LEGACY)
}

private fun getTemplateArguments(
    questType: QuestType<*>,
    element: Element?,
    locale: Locale,
    featureDictionaryFuture: FutureTask<FeatureDictionary>?
): Array<String> {
    val tags = element?.tags ?: emptyMap()
    val typeName = lazy { featureDictionaryFuture?.get()?.let { dict ->
        dict.byTags(tags).forLocale(locale).find()?.firstOrNull()?.name
    }}
    return ((questType as? OsmElementQuestType<*>)?.getTitleArgs(tags, typeName)) ?: emptyArray()
}


private fun getQuestTitleResId(questType: QuestType<*>, element: Element?) =
    (questType as? OsmElementQuestType<*>)?.getTitle(element?.tags ?: emptyMap()) ?: questType.title
