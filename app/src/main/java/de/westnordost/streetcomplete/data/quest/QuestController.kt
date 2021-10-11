package de.westnordost.streetcomplete.data.quest

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.meta.KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED
import de.westnordost.streetcomplete.data.osm.edits.*
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.*
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.quests.note_discussion.NoteAnswer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        questKey: QuestKey,
        questTitle: String,
        text: String,
        imagePaths: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        val q = get(questKey) ?: return@withContext false

        var fullText = "Unable to answer \"$questTitle\""
        if (q is OsmQuest && q.elementId > 0) {
            val lowercaseTypeName = q.elementType.name.lowercase()
            val elementId = q.elementId
            fullText += " for https://osm.org/$lowercaseTypeName/$elementId"
        }
        fullText += " via ${ApplicationConstants.USER_AGENT}:\n\n$text"

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
        q: OsmQuest,
        splits: List<SplitPolylineAtPosition>,
        source: String
    ): Boolean = withContext(Dispatchers.IO) {
        val w = getOsmElement(q) as? Way ?: return@withContext false
        val geom = q.geometry as? ElementPolylinesGeometry ?: return@withContext false

        elementEditsController.add(
            q.osmElementQuestType,
            w,
            geom,
            source,
            SplitWayAction(ArrayList(splits))
        )
        return@withContext true
    }

    /** Delete the element referred to by the given OSM quest id.
     * @return true if successful
     */
    suspend fun deletePoiElement(
        q: OsmQuest,
        source: String
    ): Boolean = withContext(Dispatchers.IO) {
        val e = getOsmElement(q) ?: return@withContext false

        Log.d(TAG, "Deleted ${q.elementType.name} #${q.elementId} in frame of quest ${q.type::class.simpleName!!}")

        elementEditsController.add(
            q.osmElementQuestType,
            e,
            q.geometry,
            source,
            DeletePoiNodeAction
        )
        return@withContext true
    }

    /** Replaces the previous element which is assumed to be a shop/amenity of sort with another
     *  feature.
     *  @return true if successful
     */
    suspend fun replaceShopElement(
        q: OsmQuest,
        tags: Map<String, String>,
        source: String
    ): Boolean = withContext(Dispatchers.IO) {
        val e = getOsmElement(q) ?: return@withContext false

        val changes = createReplaceShopChanges(e.tags, tags)
        Log.d(TAG, "Replaced ${q.elementType.name} #${q.elementId} in frame of quest ${q.type::class.simpleName!!} with $changes")

        elementEditsController.add(
            q.osmElementQuestType,
            e,
            q.geometry,
            source,
            UpdateElementTagsAction(changes)
        )

        return@withContext true
    }

    private fun createReplaceShopChanges(previousTags: Map<String, String>, newTags: Map<String, String>): StringMapChanges {
        val changesList = mutableListOf<StringMapEntryChange>()

        // first remove old tags
        for ((key, value) in previousTags) {
            val isOkToRemove = KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED.any { it.matches(key) }
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
    suspend fun solve(quest: Quest, answer: Any, source: String): Boolean {
        return when(quest) {
            is OsmNoteQuest -> solveOsmNoteQuest(quest, answer as NoteAnswer)
            is OsmQuest -> solveOsmQuest(quest, answer, source)
            else -> throw NotImplementedError()
        }
    }

    suspend fun getOsmElement(quest: OsmQuest): Element? = withContext(Dispatchers.IO) {
        mapDataSource.get(quest.elementType, quest.elementId)
    }

    private suspend fun solveOsmNoteQuest(
        q: OsmNoteQuest,
        answer: NoteAnswer
    ): Boolean = withContext(Dispatchers.IO) {
        require(answer.text.isNotEmpty()) { "NoteQuest has been answered with an empty comment!" }
        // for note quests: questId == noteId
        noteEditsController.add(q.id, NoteEditAction.COMMENT, q.position, answer.text, answer.imagePaths)
        return@withContext true
    }

    private suspend fun solveOsmQuest(
        q: OsmQuest,
        answer: Any, source: String
    ): Boolean = withContext(Dispatchers.IO) {
        val e = getOsmElement(q) ?: return@withContext false

        /** When OSM data is being updated (e.g. during download), first that data is persisted to
         *  the database and after that, the quests are updated on the new data.
         *
         *  Depending on the volume of the data, this may take some seconds. So in this time, OSM
         *  data and the quests are out of sync: If in this time, a quest is solved, the quest may
         *  not be applicable to the element anymore. So we need to check that before trying to
         *  apply the changes.
         *
         *  Why not synchronize the updating of OSM data and generated quests so that they never can
         *  go out of sync? It was like this (since v32) initially, but it made using the app
         *  (opening quests, solving quests) unusable and seemingly unresponsive while the app was
         *  downloading/updating data. See issue #2876 */
        if (q.osmElementQuestType.isApplicableTo(e) == false) return@withContext false

        val changes = createOsmQuestChanges(q, e, answer)
        require(!changes.isEmpty()) {
            "OsmQuest ${q.key} has been answered by the user but there are no changes!"
        }

        Log.d(TAG, "Solved a ${q.type::class.simpleName!!} quest: $changes")

        elementEditsController.add(
            q.osmElementQuestType,
            e,
            q.geometry,
            source,
            UpdateElementTagsAction(changes)
        )

        return@withContext true
    }

    private fun createOsmQuestChanges(quest: OsmQuest, element: Element, answer: Any) : StringMapChanges {
        val changesBuilder = StringMapChangesBuilder(element.tags)
        quest.osmElementQuestType.applyAnswerToUnsafe(answer, changesBuilder)
        return changesBuilder.create()
    }

    /** Make the given quest invisible (per user interaction).  */
    suspend fun hide(questKey: QuestKey) = withContext(Dispatchers.IO) {
        when (questKey) {
            is OsmNoteQuestKey -> osmNoteQuestController.hide(questKey.noteId)
            is OsmQuestKey -> osmQuestController.hide(questKey)
        }
    }

    /** Unhide all previously hidden quests */
    suspend fun unhideAll(): Int = withContext(Dispatchers.IO) {
        osmQuestController.unhideAll() + osmNoteQuestController.unhideAll()
    }

    /** Retrieve the given quest from local database  */
    suspend fun get(questKey: QuestKey): Quest? = withContext(Dispatchers.IO) {
        when (questKey) {
            is OsmNoteQuestKey -> osmNoteQuestController.get(questKey.noteId)
            is OsmQuestKey -> osmQuestController.get(questKey)
        }
    }

    companion object {
        private const val TAG = "QuestController"
    }
}
