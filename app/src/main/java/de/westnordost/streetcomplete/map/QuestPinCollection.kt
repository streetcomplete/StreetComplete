package de.westnordost.streetcomplete.map

import android.content.res.Resources
import android.os.Build
import androidx.collection.LongSparseArray
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.data.Quest
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.streetcomplete.ktx.values
import de.westnordost.streetcomplete.map.tangram.toLngLat
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway
import java.util.*
import javax.inject.Inject

/** Modifiable collection of quest pin features for the quest pin layer in the map view */
class QuestPinCollection @Inject constructor(
    val questTypesProvider: OrderedVisibleQuestTypesProvider,
    val resources: Resources
) {
    private val questTypeOrders: MutableMap<QuestType<*>, Int> = mutableMapOf()

    // quest group -> ( quest Id -> [point, ...] )
    private val quests: EnumMap<QuestGroup, LongSparseArray<List<Point>>> = EnumMap(QuestGroup::class.java)

    init {
        initializeQuestTypeOrders()
    }

    fun add(quest: Quest, group: QuestGroup) {
        // hack away cycleway quests for old Android SDK versions (#713)
        if (quest.type is AddCycleway && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        val questIconName = resources.getResourceEntryName(quest.type.icon)
        val positions = quest.markerLocations
        val points = positions.map { position ->
            val properties = mapOf(
                "type" to "point",
                "kind" to questIconName,
                "order" to getQuestDrawOrder(quest).toString(),
                MARKER_QUEST_GROUP to group.name,
                MARKER_QUEST_ID to quest.id!!.toString()
            )
            Point(position.toLngLat(), properties)
        }
        if (quests[group] == null) quests[group] = LongSparseArray(256)
        quests[group]?.put(quest.id!!, points)
    }

    fun remove(questId: Long, group: QuestGroup) {
        quests[group]?.remove(questId)
    }

    fun clear() {
        for (value in quests.values) {
            value.clear()
        }
        initializeQuestTypeOrders()
    }

    fun getPoints(): List<Point> = quests.values.flatMap { questsById ->
        questsById.values.flatten()
    }

    private fun initializeQuestTypeOrders() {
        // this needs to be reinitialized when the quest order changes
        var order = 0
        for (questType in questTypesProvider.get()) {
            questTypeOrders[questType] = order++
        }
    }

    private fun getQuestDrawOrder(quest: Quest): Int {
        /* priority is decided by
           - primarily by quest type to allow quest prioritization
           - for quests of the same type - influenced by quest id,
             this is done to reduce chance that as user zoom in a quest disappears,
             especially in case where disappearing quest is one that user selected to solve
           main priority part - values fit into Integer, but with as large steps as possible */
        val questTypeOrder = questTypeOrders[quest.type] ?: 0
        val freeValuesForEachQuest = Int.MAX_VALUE / questTypeOrders.size
        /* quest ID is used to add values unique to each quest to make ordering consistent
           freeValuesForEachQuest is an int, so % freeValuesForEachQuest will fit into int */
        val hopefullyUniqueValueForQuest = (quest.id!! % freeValuesForEachQuest).toInt()
        return questTypeOrder * freeValuesForEachQuest + hopefullyUniqueValueForQuest
    }


    companion object {
        const val MARKER_QUEST_ID = "quest_id"
        const val MARKER_QUEST_GROUP = "quest_group"
    }
}
