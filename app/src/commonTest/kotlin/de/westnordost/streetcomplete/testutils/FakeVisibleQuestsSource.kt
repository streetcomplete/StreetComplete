package de.westnordost.streetcomplete.testutils

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.util.math.contains
import dev.mokkery.mock

/** Serves the given [quests] like the real source would: by their center position.
 *
 *  A subclass rather than a mock because a mocked VisibleQuestsSource instantiates its constructor
 *  parameters, and QuestTypeRegistry does not accept an empty list. */
class FakeVisibleQuestsSource : VisibleQuestsSource(
    QuestTypeRegistry(listOf(0 to QUEST_TYPE)), mock(), mock(), mock(), mock(), mock(), mock()
) {
    var quests: List<Quest> = emptyList()
    var listener: Listener? = null

    override fun addListener(listener: Listener) { this.listener = listener }
    override fun removeListener(listener: Listener) { this.listener = null }
    override fun getAll(bbox: BoundingBox): List<Quest> = quests.filter { it.position in bbox }
    override fun get(questKey: QuestKey): Quest? = quests.find { it.key == questKey }
}
