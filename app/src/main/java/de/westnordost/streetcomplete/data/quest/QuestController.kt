package de.westnordost.streetcomplete.data.quest

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmElement
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.edits.*
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osm.edits.update_tags.*
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.quests.note_discussion.NoteAnswer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

/** Controls the workflow of quests: Solving them, hiding them instead, splitting the way instead,
 *  undoing, etc. */
@Singleton class QuestController @Inject constructor(
    private val osmQuestController: OsmQuestController,
    private val osmNoteQuestController: OsmNoteQuestController,
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val mapDataSource: MapDataWithEditsSource
) {

    /** Create a note for the given OSM Quest instead of answering it.
     * @return true if successful
     */
    suspend fun createNote(
        osmQuestId: Long,
        questTitle: String,
        text: String,
        imagePaths: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        val q = osmQuestController.get(osmQuestId) ?: return@withContext false

        val lowercaseTypeName = q.elementType.name.toLowerCase(Locale.US)
        val elementId = q.elementId
        val fullText =
            "Unable to answer \"$questTitle\"" +
            " for https://osm.org/$lowercaseTypeName/$elementId" +
            " via ${ApplicationConstants.USER_AGENT}:" +
            "\n\n$text"

        noteEditsController.add(0, NoteEditAction.CREATE, q.position, fullText, imagePaths)

        return@withContext true
    }

    /** Create a note at the given position.
     */
    suspend fun createNote(
        text: String,
        imagePaths: List<String>,
        position: LatLon
    ) = withContext(Dispatchers.IO) {
        val fullText = "$text\n\nvia ${ApplicationConstants.USER_AGENT}"
        noteEditsController.add(0, NoteEditAction.CREATE, position, fullText, imagePaths)
    }

    /** Split a way for the given OSM Quest.
     * @return true if successful
     */
    suspend fun splitWay(
        osmQuestId: Long,
        splits: List<SplitPolylineAtPosition>,
        source: String
    ): Boolean = withContext(Dispatchers.IO) {
        val q = osmQuestController.get(osmQuestId) ?: return@withContext false
        val w = mapDataSource.get(q.elementType, q.elementId) as? Way ?: return@withContext false
        elementEditsController.add(
            q.osmElementQuestType,
            q.elementType,
            q.elementId,
            source,
            q.position,
            SplitWayAction(ArrayList(splits), w.nodeIds.first(), w.nodeIds.last())
        )
        return@withContext true
    }

    /** Delete the element referred to by the given OSM quest id.
     * @return true if successful
     */
    suspend fun deletePoiElement(osmQuestId: Long, source: String): Boolean = withContext(Dispatchers.IO) {
        val q = osmQuestController.get(osmQuestId) ?: return@withContext false
        val e = mapDataSource.get(q.elementType, q.elementId) ?: return@withContext false

        Log.d(TAG, "Deleted ${q.elementType.name} #${q.elementId} in frame of quest ${q.type::class.simpleName!!}")

        elementEditsController.add(
            q.osmElementQuestType,
            q.elementType,
            q.elementId,
            source,
            q.position,
            DeletePoiNodeAction(e.version)
        )
        return@withContext true
    }

    /** Replaces the previous element which is assumed to be a shop/amenity of sort with another
     *  feature.
     *  @return true if successful
     */
    suspend fun replaceShopElement(
        osmQuestId: Long,
        tags: Map<String, String>,
        source: String
    ): Boolean = withContext(Dispatchers.IO) {
        val q = osmQuestController.get(osmQuestId) ?: return@withContext false
        val element = getOsmElement(q) ?: return@withContext false
        val changes = createReplaceShopChanges(element.tags.orEmpty(), tags)
        Log.d(TAG, "Replaced ${q.elementType.name} #${q.elementId} in frame of quest ${q.type::class.simpleName!!} with $changes")

        elementEditsController.add(
            q.osmElementQuestType,
            q.elementType,
            q.elementId,
            source,
            q.position,
            UpdateElementTagsAction(element.getSpatialParts(), changes, null)
        )

        return@withContext true
    }

    private fun createReplaceShopChanges(previousTags: Map<String, String>, newTags: Map<String, String>): StringMapChanges {
        val changesList = mutableListOf<StringMapEntryChange>()

        // first remove old tags
        for ((key, value) in previousTags) {
            val isOkToRemove = KEYS_THAT_SHOULD_NOT_BE_REMOVED_WHEN_SHOP_IS_REPLACED.none { it.matches(key) }
            if (isOkToRemove && !newTags.containsKey(key)) {
                changesList.add(StringMapEntryDelete(key, value))
            }
        }
        // then add new tags
        for ((key, value) in newTags) {
            val valueBefore = previousTags[key]
            if (valueBefore != null) changesList.add(StringMapEntryModify(key, valueBefore, value))
            else changesList.add(StringMapEntryAdd(key, value))
        }

        return StringMapChanges(changesList)
    }

    /** Apply the user's answer to the given quest.
     * @return true if successful
     */
    suspend fun solve(questId: Long, group: QuestGroup, answer: Any, source: String): Boolean {
        return when(group) {
            QuestGroup.OSM -> solveOsmQuest(questId, answer, source)
            QuestGroup.OSM_NOTE -> solveOsmNoteQuest(questId, answer as NoteAnswer)
        }
    }

    suspend fun getOsmElement(quest: OsmQuest): OsmElement? = withContext(Dispatchers.IO) {
        mapDataSource.get(quest.elementType, quest.elementId) as OsmElement?
    }

    private suspend fun solveOsmNoteQuest(questId: Long, answer: NoteAnswer): Boolean = withContext(Dispatchers.IO) {
        val q = osmNoteQuestController.get(questId) ?: return@withContext false

        require(answer.text.isNotEmpty()) { "NoteQuest has been answered with an empty comment!" }
        // for note quests: questId == noteId
        noteEditsController.add(questId, NoteEditAction.COMMENT, q.position, answer.text, answer.imagePaths)
        return@withContext true
    }

    private suspend fun solveOsmQuest(questId: Long, answer: Any, source: String): Boolean = withContext(Dispatchers.IO) {
        // race condition: another thread (i.e. quest download thread) may have removed the
        // element already (#282). So in this case, just ignore
        val q = osmQuestController.get(questId) ?: return@withContext false
        val element = getOsmElement(q) ?: return@withContext false

        val changes = createOsmQuestChanges(q, element, answer)
        require(!changes.isEmpty()) {
            "OsmQuest $questId (${q.type::class.simpleName!!}) has been answered by the user but the changeset is empty!"
        }

        Log.d(TAG, "Solved a ${q.type::class.simpleName!!} quest: $changes")

        elementEditsController.add(
            q.osmElementQuestType,
            q.elementType,
            q.elementId,
            source,
            q.position,
            UpdateElementTagsAction(element.getSpatialParts(), changes, q.osmElementQuestType)
        )

        return@withContext true
    }

    private fun createOsmQuestChanges(quest: OsmQuest, element: Element, answer: Any) : StringMapChanges {
        val changesBuilder = StringMapChangesBuilder(element.tags.orEmpty())
        quest.osmElementQuestType.applyAnswerToUnsafe(answer, changesBuilder)
        return changesBuilder.create()
    }

    /** Make the given quest invisible (per user interaction).  */
    suspend fun hide(questId: Long, group: QuestGroup) = withContext(Dispatchers.IO) {
        when (group) {
            QuestGroup.OSM -> {
                val quest = osmQuestController.get(questId)
                if (quest != null) osmQuestController.hide(quest)
            }
            QuestGroup.OSM_NOTE -> {
                osmNoteQuestController.hide(questId)
            }
        }
    }

    /** Unhide all previously hidden quests */
    suspend fun unhideAll(): Int = withContext(Dispatchers.IO) {
        osmQuestController.unhideAll() + osmNoteQuestController.unhideAll()
    }

    /** Retrieve the given quest from local database  */
    suspend fun get(group: QuestGroup, questId: Long): Quest? = withContext(Dispatchers.IO) {
        when (group) {
            QuestGroup.OSM -> osmQuestController.get(questId)
            QuestGroup.OSM_NOTE -> osmNoteQuestController.get(questId)
        }
    }

    companion object {
        private const val TAG = "QuestController"
    }
}

data class QuestAndGroup(val quest: Quest, val group: QuestGroup)

private val KEYS_THAT_SHOULD_NOT_BE_REMOVED_WHEN_SHOP_IS_REPLACED = listOf(
    "landuse", "historic",
    // building/simple 3d building mapping
    "building", "man_made", "building:.*", "roof:.*",
    // any address
    "addr:.*",
    // shop can at the same time be an outline in indoor mapping
    "level", "level:ref", "indoor", "room",
    // geometry
    "layer", "ele", "height", "area", "is_in",
    // notes and fixmes
    "FIXME", "fixme", "note"
).map { it.toRegex() }
