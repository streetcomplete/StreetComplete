package de.westnordost.streetcomplete.quests

import android.content.res.Resources
import android.text.Spanned
import androidx.core.text.parseAsHtml
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType

fun Resources.getQuestTitle(questType: QuestType<*>, element: Element?): String {
    return getString(getQuestTitleResId(questType, element))
}

fun Resources.getHtmlQuestTitle(questType: QuestType<*>, element: Element?): Spanned {
    return getString(getQuestTitleResId(questType, element)).parseAsHtml()
}

private fun getQuestTitleResId(questType: QuestType<*>, element: Element?) =
    (questType as? OsmElementQuestType<*>)?.getTitle(element?.tags ?: emptyMap()) ?: questType.title
