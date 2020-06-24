package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.quest.QuestGroup

interface IsShowingQuestDetails {
    val questId: Long
    val questGroup: QuestGroup
}
