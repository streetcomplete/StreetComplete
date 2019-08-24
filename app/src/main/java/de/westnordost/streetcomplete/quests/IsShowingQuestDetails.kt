package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.QuestGroup

interface IsShowingQuestDetails {
    val questId: Long
    val questGroup: QuestGroup
}
