package de.westnordost.streetcomplete.settings

import android.view.View
import de.westnordost.streetcomplete.data.QuestType

fun genericQuestTitle(resourceProvider: View, type: QuestType<*>): String {
    val questTitleTemplate = resourceProvider.resources.getString(type.title)
    val parameterCount = questTitleTemplate.split("%s").size - 1
    return resourceProvider.resources.getString(type.title, *Array(parameterCount){"…"})
}
