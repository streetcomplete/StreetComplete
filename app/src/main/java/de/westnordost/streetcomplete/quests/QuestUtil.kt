package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Html
import android.text.Spanned
import androidx.core.text.parseAsHtml
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType

fun Resources.getQuestTitle(questType: QuestType, element: Element?): String {
    val arguments = getTemplateArguments(questType, element)
    return getString(getQuestTitleResId(questType, element), *arguments)
}

fun Resources.getHtmlQuestTitle(questType: QuestType, element: Element?): Spanned {
    val arguments = getTemplateArguments(questType, element)
    val spannedArguments = arguments.map { "<i>" + Html.escapeHtml(it) + "</i>" }.toTypedArray()
    return getString(getQuestTitleResId(questType, element), *spannedArguments).parseAsHtml()
}

private fun getTemplateArguments(questType: QuestType, element: Element?): Array<String> {
    val tags = element?.tags ?: emptyMap()
    return ((questType as? OsmElementQuestType<*>)?.getTitleArgs(tags)) ?: emptyArray()
}

private fun getQuestTitleResId(questType: QuestType, element: Element?) =
    (questType as? OsmElementQuestType<*>)?.getTitle(element?.tags ?: emptyMap()) ?: questType.title
