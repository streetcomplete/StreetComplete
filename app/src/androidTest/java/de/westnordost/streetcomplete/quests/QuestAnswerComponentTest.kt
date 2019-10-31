package de.westnordost.streetcomplete.quests

import org.junit.Test

import de.westnordost.streetcomplete.data.QuestGroup

import org.junit.Assert.assertEquals

class QuestAnswerComponentTest {

    @Test fun getSet() {
        val c1 = QuestAnswerComponent()
        c1.onCreate(QuestAnswerComponent.createArguments(11, QuestGroup.OSM))

        assertEquals(QuestGroup.OSM, c1.questGroup)
        assertEquals(11, c1.questId)

        val c2 = QuestAnswerComponent()
        c2.onCreate(c1.arguments)

        assertEquals(c2.questGroup, c1.questGroup)
        assertEquals(c2.questId, c1.questId)
    }

    @Test fun listener() {
        val c1 = QuestAnswerComponent()

        val expectQuestId = 3
        val expectGroup = QuestGroup.OSM
        val expectQuestTitle = "What?"
        val expectObject = "jo"

        c1.onAttach(object : OsmQuestAnswerListener {
            override fun onAnsweredQuest(questId: Long, group: QuestGroup, answer: Any) {
                assertEquals(expectQuestId.toLong(), questId)
                assertEquals(expectGroup, group)
                assertEquals(expectObject, answer)
            }

            override fun onComposeNote(questId: Long, group: QuestGroup, questTitle: String) {
                assertEquals(expectQuestId.toLong(), questId)
                assertEquals(expectGroup, group)
                assertEquals(expectQuestTitle, questTitle)
            }

            override fun onSplitWay(osmQuestId: Long) {
                assertEquals(expectQuestId.toLong(), osmQuestId)
            }

            override fun onSkippedQuest(questId: Long, group: QuestGroup) {
                assertEquals(expectQuestId.toLong(), questId)
                assertEquals(expectGroup, group)
            }
        })

        c1.onCreate(QuestAnswerComponent.createArguments(expectQuestId.toLong(), expectGroup))
        c1.onComposeNote(expectQuestTitle)
        c1.onSplitWay()
        c1.onAnsweredQuest(expectObject)
        c1.onSkippedQuest()
    }
}
