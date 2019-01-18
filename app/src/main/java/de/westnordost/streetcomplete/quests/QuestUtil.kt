package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Html
import android.text.Spanned

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType

fun Resources.getQuestTitle(questType: QuestType<*>, element: Element?): String {
    return getString(getQuestTitleResId(questType, element), getElementName(element))
}

fun Resources.getHtmlQuestTitle(questType: QuestType<*>, element: Element?): Spanned {
    val name = getElementName(element)
    val spanName = if (name != null) "<i>" + Html.escapeHtml(name) + "</i>" else null

    return Html.fromHtml(getString(getQuestTitleResId(questType, element), spanName))
}

private fun getElementName(element: Element?) = element?.tags?.get("name")

private fun getQuestTitleResId(questType: QuestType<*>, element: Element?) =
    (questType as? OsmElementQuestType<*>)?.getTitle(element?.tags ?: emptyMap()) ?: questType.title
