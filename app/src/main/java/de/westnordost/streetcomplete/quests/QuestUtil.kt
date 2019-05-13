package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Html
import android.text.Spanned

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import java.util.*
import java.util.concurrent.FutureTask

fun Resources.getQuestTitle(questType: QuestType<*>, element: Element?, featureDictionaryFuture: FutureTask<FeatureDictionary>?): String {
    val name = getElementName(element, configuration.locale, featureDictionaryFuture)
    return getString(getQuestTitleResId(questType, element), name)
}

fun Resources.getHtmlQuestTitle(questType: QuestType<*>, element: Element?, featureDictionaryFuture: FutureTask<FeatureDictionary>?): Spanned {
    val name = getElementName(element, configuration.locale, featureDictionaryFuture)
    val spanName = if (name != null) "<i>" + Html.escapeHtml(name) + "</i>" else null
    return Html.fromHtml(getString(getQuestTitleResId(questType, element), spanName))
}

private fun getElementName(element: Element?, locale: Locale, featureDictionaryFuture: FutureTask<FeatureDictionary>?) =

    element?.tags?.let { tags ->
        tags["name"] ?:
        tags["brand"] ?:
        featureDictionaryFuture?.get()?.let { it.byTags(tags).forLocale(locale).find()?.firstOrNull()?.name }
    }

private fun getQuestTitleResId(questType: QuestType<*>, element: Element?) =
    (questType as? OsmElementQuestType<*>)?.getTitle(element?.tags ?: emptyMap()) ?: questType.title
