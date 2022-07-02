package de.westnordost.streetcomplete.screens.settings

import android.content.res.Resources
import de.westnordost.streetcomplete.data.quest.QuestType

fun genericQuestTitle(resources: Resources, type: QuestType): String {
    // all parameters are replaced by generic three dots
    // it is assumed that quests will not have a ridiculously huge parameter count
    return resources.getString(type.title, *Array(10) { "…" })
}
