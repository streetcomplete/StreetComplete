package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.QuestGroup

interface OsmQuestAnswerListener {
    /** Called when the user answered the quest with the given id. What is in the bundle, is up to
     * the dialog with which the quest was answered  */
    fun onAnsweredQuest(questId: Long, group: QuestGroup, answer: Any)

    /** Called when the user chose to leave a note instead  */
    fun onComposeNote(questId: Long, group: QuestGroup, questTitle: String)

    /** Called when the user chose to split the way  */
    fun onSplitWay(osmQuestId: Long)

    /** Called when the user chose to skip the quest  */
    fun onSkippedQuest(questId: Long, group: QuestGroup)
}
