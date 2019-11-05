package de.westnordost.streetcomplete.quests

import android.os.Bundle
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.data.QuestGroup

class QuestAnswerComponent {

    private lateinit var callbackListener: OsmQuestAnswerListener

    var questId: Long = 0L
        private set
    lateinit var questGroup: QuestGroup
        private set

    val arguments get() = createArguments(questId, questGroup)

    fun onCreate(args: Bundle?) {

        if (args == null || args.getLong(ARG_QUEST_ID, -1) == -1L || args.getString(ARG_QUEST_GROUP, null) == null) {
            throw IllegalStateException(
                "Use QuestAnswerComponent.createArguments and pass the created bundle as an argument."
            )
        }

        questId = args.getLong(ARG_QUEST_ID)
        questGroup = QuestGroup.valueOf(args.getString(ARG_QUEST_GROUP)!!)
    }

    fun onAttach(listener: OsmQuestAnswerListener) {
        callbackListener = listener
    }

    fun onAnsweredQuest(answer: Any) {
        callbackListener.onAnsweredQuest(questId, questGroup, answer)
    }

    fun onComposeNote(questTitle: String) {
        callbackListener.onComposeNote(questId, questGroup, questTitle)
    }

    fun onSplitWay() {
        if (questGroup != QuestGroup.OSM) throw IllegalStateException()
        callbackListener.onSplitWay(questId)
    }

    fun onSkippedQuest() {
        callbackListener.onSkippedQuest(questId, questGroup)
    }

    companion object {
        private const val ARG_QUEST_ID = "questId"
        private const val ARG_QUEST_GROUP = "questGroup"

        fun createArguments(questId: Long, group: QuestGroup) = bundleOf(
            ARG_QUEST_ID to questId,
            ARG_QUEST_GROUP to group.name
        )
    }
}
