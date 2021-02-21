package de.westnordost.streetcomplete.data.quest

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmElement
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.changes.*
import de.westnordost.streetcomplete.data.osm.changes.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestController
import de.westnordost.streetcomplete.data.osm.changes.update_tags.*
import de.westnordost.streetcomplete.data.osm.changes.split_way.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.changes.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNote
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteDao
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNote
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.quests.note_discussion.NoteAnswer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/** Controls the workflow of quests: Solving them, hiding them instead, splitting the way instead,
 *  undoing, etc. */
@Singleton class QuestController @Inject constructor(
    private val osmQuestController: OsmQuestController,
    private val osmNoteQuestController: OsmNoteQuestController,
    private val elementEditsController: ElementEditsController,
    private val createNoteDB: CreateNoteDao,
    private val commentNoteDB: CommentNoteDao,
    private val mapDataSource: MapDataWithEditsSource
): CoroutineScope by CoroutineScope(Dispatchers.Default) {

    /** Create a note for the given OSM Quest instead of answering it.
     * @return true if successful
     */
    fun createNote(osmQuestId: Long, questTitle: String, text: String, imagePaths: List<String>?): Boolean {
        val q = osmQuestController.get(osmQuestId) ?: return false
        val createNote = CreateNote(null, text, q.center, questTitle, ElementKey(q.elementType, q.elementId), imagePaths)
        createNoteDB.add(createNote)
        return true
    }

    /** Create a note at the given position.
     */
    fun createNote(text: String, imagePaths: List<String>?, position: LatLon) {
        val createNote = CreateNote(null, text, position, null, null, imagePaths)
        createNoteDB.add(createNote)
    }

    /** Split a way for the given OSM Quest.
     * @return true if successful
     */
    fun splitWay(osmQuestId: Long, splits: List<SplitPolylineAtPosition>, source: String): Boolean {
        val q = osmQuestController.get(osmQuestId) ?: return false
        val w = mapDataSource.get(q.elementType, q.elementId) as? Way ?: return false
        elementEditsController.add(
            q.osmElementQuestType,
            q.elementType,
            q.elementId,
            source,
            q.center,
            SplitWayAction(ArrayList(splits), w.nodeIds.first(), w.nodeIds.last())
        )
        return true
    }

    /** Delete the element referred to by the given OSM quest id.
     * @return true if successful
     */
    fun deletePoiElement(osmQuestId: Long, source: String): Boolean {
        val q = osmQuestController.get(osmQuestId) ?: return false
        val e = mapDataSource.get(q.elementType, q.elementId) ?: return false

        Log.d(TAG, "Deleted ${q.elementType.name} #${q.elementId} in frame of quest ${q.type.javaClass.simpleName}")

        elementEditsController.add(
            q.osmElementQuestType,
            q.elementType,
            q.elementId,
            source,
            q.center,
            DeletePoiNodeAction(e.version)
        )
        return true
    }

    /** Replaces the previous element which is assumed to be a shop/amenity of sort with another
     *  feature.
     *  @return true if successful
     */
    fun replaceShopElement(osmQuestId: Long, tags: Map<String, String>, source: String): Boolean {
        val q = osmQuestController.get(osmQuestId) ?: return false
        val element = getOsmElement(q) ?: return false
        val changes = createReplaceShopChanges(element.tags.orEmpty(), tags)
        Log.d(TAG, "Replaced ${q.elementType.name} #${q.elementId} in frame of quest ${q.type.javaClass.simpleName} with $changes")

        elementEditsController.add(
            q.osmElementQuestType,
            q.elementType,
            q.elementId,
            source,
            q.center,
            UpdateElementTagsAction(element.getSpatialParts(), changes, null)
        )

        return true
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
    fun solve(questId: Long, group: QuestGroup, answer: Any, source: String): Boolean {
        return when(group) {
            QuestGroup.OSM -> solveOsmQuest(questId, answer, source)
            QuestGroup.OSM_NOTE -> solveOsmNoteQuest(questId, answer as NoteAnswer)
        }
    }

    fun getOsmElement(quest: OsmQuest): OsmElement? =
        mapDataSource.get(quest.elementType, quest.elementId) as OsmElement?

    private fun solveOsmNoteQuest(questId: Long, answer: NoteAnswer): Boolean {
        val q = osmNoteQuestController.get(questId) ?: return false

        require(answer.text.isNotEmpty()) { "NoteQuest has been answered with an empty comment!" }
        val commentNote = CommentNote(questId, q.center, answer.text, answer.imagePaths)
        commentNoteDB.add(commentNote)
        return true
    }

    private fun solveOsmQuest(questId: Long, answer: Any, source: String): Boolean {
        // race condition: another thread (i.e. quest download thread) may have removed the
        // element already (#282). So in this case, just ignore
        val q = osmQuestController.get(questId) ?: return false
        val element = getOsmElement(q) ?: return false

        val changes = createOsmQuestChanges(q, element, answer)
        require(!changes.isEmpty()) {
            "OsmQuest $questId (${q.type.javaClass.simpleName}) has been answered by the user but the changeset is empty!"
        }

        Log.d(TAG, "Solved a ${q.type.javaClass.simpleName} quest: $changes")

        elementEditsController.add(
            q.osmElementQuestType,
            q.elementType,
            q.elementId,
            source,
            q.center,
            UpdateElementTagsAction(element.getSpatialParts(), changes, q.osmElementQuestType)
        )

        return true
    }

    private fun createOsmQuestChanges(quest: OsmQuest, element: Element, answer: Any) : StringMapChanges {
        val changesBuilder = StringMapChangesBuilder(element.tags.orEmpty())
        quest.osmElementQuestType.applyAnswerToUnsafe(answer, changesBuilder)
        return changesBuilder.create()
    }

    /** Make the given quest invisible (per user interaction).  */
    fun hide(questId: Long, group: QuestGroup) {
        when (group) {
            QuestGroup.OSM -> {
                val quest = osmQuestController.get(questId) ?: return
                osmQuestController.hide(quest)
            }
            QuestGroup.OSM_NOTE -> {
                osmNoteQuestController.hide(questId)
            }
        }
    }

    /** Unhide all previously hidden quests */
    fun unhideAll(): Int {
        return osmQuestController.unhideAll() + osmNoteQuestController.unhideAll()
    }

    /** Retrieve the given quest from local database  */
    fun get(questId: Long, group: QuestGroup): Quest? = when (group) {
        QuestGroup.OSM -> osmQuestController.get(questId)
        QuestGroup.OSM_NOTE -> osmNoteQuestController.get(questId)
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
