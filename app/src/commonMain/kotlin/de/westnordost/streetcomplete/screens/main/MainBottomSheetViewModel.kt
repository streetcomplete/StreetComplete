package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.LazyMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.ktx.truncateTo6Decimals
import de.westnordost.streetcomplete.util.math.enlargedBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withContext

@Stable
abstract class MainBottomSheetViewModel : ViewModel() {
    abstract fun bottomSheet(selection: MainSheetSelection): Flow<ShownBottomSheet?>
    abstract suspend fun getHighlightedMarkers(sheet: ShownBottomSheet): List<Marker>

    abstract fun hideQuest(questKey: QuestKey)

    abstract fun isSurvey(geometry: ElementGeometry): Boolean

    abstract fun submitEdit(
        elementEditType: ElementEditType,
        geometry: ElementGeometry,
        elementEditAction: ElementEditAction,
    )
    abstract fun commentNote(
        note: Note,
        text: String?,
        imagePaths: List<String> = emptyList(),
    )
    abstract fun createNote(
        position: LatLon,
        text: String,
        imagePaths: List<String> = emptyList(),
        trackpoints: List<Trackpoint>? = null
    )
}

@Stable
class MainBottomSheetViewModelImpl(
    private val mapDataSource: MapDataWithEditsSource,
    private val notesSource: NotesWithEditsSource,
    private val osmQuestSource: OsmQuestSource,
    private val osmNoteQuestSource: OsmNoteQuestSource,
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val hiddenQuestsController: QuestsHiddenController,
    private val surveyChecker: SurveyChecker,
    private val visibleQuestsSource: VisibleQuestsSource,
    private val overlayRegistry: OverlayRegistry,
    private val featureDictionary: Lazy<FeatureDictionary>,
) : MainBottomSheetViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun bottomSheet(selection: MainSheetSelection): Flow<ShownBottomSheet?> {
        if (selection is MainSheetSelection.CreateNote) {
            return flowOf(ShownBottomSheet.CreateOsmNote(selection.trackpoints))
        }
        if (selection is MainSheetSelection.Overlay && selection.elementKey == null) {
            return flowOf(overlayRegistry.getByName(selection.name)?.let { ShownBottomSheet.Overlay(it, null, null) })
        }
        // Shows the object as it was when selected: updates would swap the open form mid-edit.
        // Only its disappearance closes the sheet.
        return flow {
            var isShown = false
            changes(selection)
                .mapLatest { withContext(Dispatchers.IO) { load(selection) } }
                .transformWhile { loaded ->
                    if (!isShown || loaded == null) emit(loaded)
                    isShown = true
                    loaded != null
                }
                .collect { emit(it) }
        }
    }

    private fun load(selection: MainSheetSelection): ShownBottomSheet? = when (selection) {
        is MainSheetSelection.Quest -> getQuest(selection.key)
        is MainSheetSelection.Overlay -> {
            val overlay = overlayRegistry.getByName(selection.name)
            val key = selection.elementKey
            if (overlay == null) null
            else if (key == null) ShownBottomSheet.Overlay(overlay, null, null)
            else getElementInOverlay(overlay, key)
        }
        is MainSheetSelection.CreateNote -> ShownBottomSheet.CreateOsmNote(selection.trackpoints)
        // resolved from the edit history instead, see MainSheetState
        is MainSheetSelection.EditHistory -> null
    }

    /** Emits once, then whenever the [selection]'s object may have been removed. Listeners are
     *  registered before the first emission, so no removal is missed. */
    private fun changes(selection: MainSheetSelection): Flow<Unit> = callbackFlow {
        val elementKey = when (selection) {
            is MainSheetSelection.Overlay -> selection.elementKey
            is MainSheetSelection.Quest -> (selection.key as? OsmQuestKey)?.let {
                ElementKey(it.elementType, it.elementId)
            }
            else -> null
        }
        val questListener = object : VisibleQuestsSource.Listener {
            override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
                if (selection is MainSheetSelection.Quest && selection.key in removed) trySend(Unit)
            }
            override fun onInvalidated() { trySend(Unit) }
        }
        val elementListener = object : MapDataWithEditsSource.Listener {
            override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
                if (elementKey != null && elementKey in deleted) trySend(Unit)
            }
            override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) { trySend(Unit) }
            override fun onCleared() { trySend(Unit) }
        }
        visibleQuestsSource.addListener(questListener)
        mapDataSource.addListener(elementListener)
        trySend(Unit)
        awaitClose {
            visibleQuestsSource.removeListener(questListener)
            mapDataSource.removeListener(elementListener)
        }
    }.buffer(Channel.CONFLATED)

    private fun getElementInOverlay(overlay: Overlay, key: ElementKey): ShownBottomSheet? {
        val geometry = mapDataSource.getGeometry(key.type, key.id) ?: return null
        // A note at the position of the element blocks editing that element.
        val note = getNoteForElementAt(geometry.center)
        return if (note != null) {
            val quest = osmNoteQuestSource.get(note.id) ?: return null
            ShownBottomSheet.OsmNoteQuest(quest, note)
        } else {
            val element = mapDataSource.get(key.type, key.id) ?: return null
            ShownBottomSheet.Overlay(overlay, element, geometry)
        }
    }

    private fun getQuest(key: QuestKey): ShownBottomSheet? {
        if (visibleQuestsSource.get(key) == null) return null
        return when (key) {
            is OsmQuestKey -> {
                val quest = osmQuestSource.get(key) ?: return null
                val element = mapDataSource.get(key.elementType, key.elementId) ?: return null
                ShownBottomSheet.OsmQuest(quest, element)
            }
            is OsmNoteQuestKey -> {
                val quest = osmNoteQuestSource.get(key.noteId) ?: return null
                val note = notesSource.get(key.noteId) ?: return null
                ShownBottomSheet.OsmNoteQuest(quest, note)
            }
        }
    }

    override suspend fun getHighlightedMarkers(sheet: ShownBottomSheet): List<Marker> = withContext(Dispatchers.IO) {
        if (sheet !is ShownBottomSheet.OsmQuest) return@withContext emptyList()
        val quest = sheet.quest
        val element = sheet.element
        val bbox = quest.geometry.bounds.enlargedBy(quest.type.highlightedElementsRadius)
        val mapData = LazyMapDataWithGeometry(bbox, mapDataSource)
        val levels = parseLevelsOrNull(element.tags)
        quest.type.getHighlightedElements(element, mapData).mapNotNull { other ->
            // Highlight nearby elements only on the same level and layer.
            if (element == other) return@mapNotNull null
            if (!levels.levelsIntersect(parseLevelsOrNull(other.tags))) return@mapNotNull null
            if (element.tags["layer"] != other.tags["layer"]) return@mapNotNull null
            val geometry = mapData.getGeometry(other.type, other.id) ?: return@mapNotNull null
            Marker(geometry, getIcon(featureDictionary.value, other), getTitle(other.tags))
        }.toList()
    }

    override fun hideQuest(questKey: QuestKey) {
        launch(Dispatchers.IO) {
            hiddenQuestsController.hide(questKey)
        }
    }

    override fun isSurvey(geometry: ElementGeometry): Boolean =
        surveyChecker.checkIsSurvey(geometry)

    override fun submitEdit(
        elementEditType: ElementEditType,
        geometry: ElementGeometry,
        elementEditAction: ElementEditAction,
    ) {
        launch(Dispatchers.IO) {
            val isNearUserLocation = surveyChecker.checkIsSurvey(geometry)
            elementEditsController.add(elementEditType, geometry, "survey", elementEditAction, isNearUserLocation)
        }
    }

    override fun commentNote(
        note: Note,
        text: String?,
        imagePaths: List<String>,
    ) {
        launch(Dispatchers.IO) {
            noteEditsController.add(note.id, NoteEditAction.COMMENT, note.position, text, imagePaths)
        }
    }

    override fun createNote(
        position: LatLon,
        text: String,
        imagePaths: List<String>,
        trackpoints: List<Trackpoint>?
    ) {
        launch(Dispatchers.IO) {
            noteEditsController.add(0, NoteEditAction.CREATE, position, text, imagePaths, trackpoints)
        }
    }

    private fun getNoteForElementAt(position: LatLon): Note? =
        notesSource
            .getAll(BoundingBox(position, position).enlargedBy(0.2))
            .filter { note ->
                note.position.truncateTo6Decimals() == position.truncateTo6Decimals() &&
                hiddenQuestsController.get(OsmNoteQuestKey(note.id)) == null
            }.firstOrNull()
}

/** The data necessary to show an element from the map clicked on in the bottom sheet */
sealed interface ShownBottomSheet {
    data class OsmQuest(
        val quest: de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest,
        val element: Element,
    ) : ShownBottomSheet {
        override val position get() = quest.position
        override val geometry get() = quest.geometry
    }

    data class OsmNoteQuest(
        val quest: de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest,
        val note: Note
    ) : ShownBottomSheet {
        override val position get() = quest.position
        override val geometry get() = quest.geometry
    }

    data class Overlay(
        val overlay: de.westnordost.streetcomplete.data.overlays.Overlay,
        val element: Element?,
        override val geometry: ElementGeometry?,
    ) : ShownBottomSheet {
        override val position get() = geometry?.center
    }

    data class CreateOsmNote(
        val trackpoints: List<Trackpoint>?
    ) : ShownBottomSheet {
        override val position get() = null
        override val geometry get() = null
    }

    data class EditHistory(
        val edit: Edit,
        override val geometry: ElementGeometry,
    ) : ShownBottomSheet {
        override val position get() = edit.position
    }

    val position: LatLon?
    val geometry: ElementGeometry?
}
