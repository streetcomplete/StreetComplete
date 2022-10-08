package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Html
import android.text.Spanned
import androidx.core.text.parseAsHtml
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestType
import de.westnordost.streetcomplete.data.quest.QuestType

fun Resources.getQuestTitle(questType: QuestType, tags: Map<String, String>): String {
    val arguments = getTemplateArguments(questType, tags)
    return getString(getQuestTitleResId(questType, tags), *arguments)
}

fun Resources.getHtmlQuestTitle(questType: QuestType, tags: Map<String, String>): Spanned {
    val arguments = getTemplateArguments(questType, tags)
    val spannedArguments = arguments.map { "<i>" + Html.escapeHtml(it) + "</i>" }.toTypedArray()
    return getString(getQuestTitleResId(questType, tags), *spannedArguments).parseAsHtml()
}

private fun getTemplateArguments(questType: QuestType, tags: Map<String, String>): Array<String> =
    when (questType) {
        is OsmElementQuestType<*> -> questType.getTitleArgs(tags)
        is OtherSourceQuestType -> questType.getTitleArgs(tags)
        else -> emptyArray()
    }

private fun getQuestTitleResId(questType: QuestType, tags: Map<String, String>) =
    when (questType) {
        is OsmElementQuestType<*> -> questType.getTitle(tags)
        is OtherSourceQuestType -> questType.getTitle(tags)
        else -> questType.title
    }
