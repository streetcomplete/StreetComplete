package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
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
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.ktx.truncateTo6Decimals
import de.westnordost.streetcomplete.util.math.enlargedBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class MainBottomSheetViewModel : ViewModel() {
    abstract val shownBottomSheet: StateFlow<ShownBottomSheet?>

    abstract fun showCreateElementInOverlay(overlay: Overlay)

    abstract fun showElementInOverlay(overlay: Overlay, elementKey: ElementKey)

    abstract fun showQuest(questKey: QuestKey)

    abstract fun showCreateNote(isGpxAttached: Boolean)

    abstract fun closeBottomSheet()

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
        track: List<Trackpoint> = emptyList()
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
) : MainBottomSheetViewModel() {
    override val shownBottomSheet = MutableStateFlow<ShownBottomSheet?>(null)

    override fun closeBottomSheet() {
        shownBottomSheet.value = null
    }

    override fun showCreateElementInOverlay(overlay: Overlay) {
        shownBottomSheet.value = ShownBottomSheet.Overlay(overlay, null, null)
    }

    override fun showElementInOverlay(overlay: Overlay, elementKey: ElementKey) {
        launch(Dispatchers.IO) {
            showElementInOverlayOrNote(overlay, elementKey)
        }
    }

    private suspend fun showElementInOverlayOrNote(overlay: Overlay, elementKey: ElementKey) {
        val geometry = mapDataSource.getGeometry(elementKey.type, elementKey.id) ?: return

        // a note at the position of the element blocks editing of that element
        val note = getNoteForElementAt(geometry.center)
        if (note != null) {
            val quest = osmNoteQuestSource.get(note.id) ?: return
            shownBottomSheet.value = ShownBottomSheet.OsmNoteQuest(quest, note)
        } else {
            val element = mapDataSource.get(elementKey.type, elementKey.id) ?: return
            shownBottomSheet.value = ShownBottomSheet.Overlay(overlay, element, geometry)
        }
    }

    override fun showQuest(questKey: QuestKey) {
        launch(Dispatchers.IO) {
            when (questKey) {
                is OsmNoteQuestKey -> showOsmNoteQuest(questKey)
                is OsmQuestKey -> showOsmQuest(questKey)
            }
        }
    }

    override fun showCreateNote(isGpxAttached: Boolean) {
        shownBottomSheet.value = ShownBottomSheet.CreateOsmNote(isGpxAttached)
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
        track: List<Trackpoint>
    ) {
        launch(Dispatchers.IO) {
            noteEditsController.add(0, NoteEditAction.CREATE, position, text, imagePaths, track)
        }
    }

    private fun showOsmQuest(questKey: OsmQuestKey) {
        val element = mapDataSource.get(questKey.elementType, questKey.elementId) ?: return
        val geometry = mapDataSource.getGeometry(questKey.elementType, questKey.elementId) ?: return
        val quest = osmQuestSource.get(questKey) ?: return
        shownBottomSheet.value = ShownBottomSheet.OsmQuest(quest, element, geometry)
    }

    private fun showOsmNoteQuest(questKey: OsmNoteQuestKey) {
        val note = notesSource.get(questKey.noteId) ?: return
        val quest = osmNoteQuestSource.get(questKey.noteId) ?: return
        shownBottomSheet.value = ShownBottomSheet.OsmNoteQuest(quest, note)
    }

    private fun getNoteForElementAt(position: LatLon): Note? {
        return notesSource
            .getAll(BoundingBox(position, position).enlargedBy(0.2))
            .filter { note ->
                note.position.truncateTo6Decimals() == position.truncateTo6Decimals() &&
                hiddenQuestsController.get(OsmNoteQuestKey(note.id)) == null
            }.firstOrNull()
    }
}

/** The data necessary to show an element from the map clicked on in the bottom sheet */
sealed interface ShownBottomSheet {
    data class OsmQuest(
        val quest: de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest,
        val element: Element,
        val geometry: ElementGeometry
    ) : ShownBottomSheet

    data class OsmNoteQuest(
        val quest: Quest,
        val note: Note
    ) : ShownBottomSheet

    data class Overlay(
        val overlay: de.westnordost.streetcomplete.data.overlays.Overlay,
        val element: Element?,
        val geometry: ElementGeometry?,
    ) : ShownBottomSheet

    data class CreateOsmNote(
        val isGpxAttached: Boolean
    ) : ShownBottomSheet
}
