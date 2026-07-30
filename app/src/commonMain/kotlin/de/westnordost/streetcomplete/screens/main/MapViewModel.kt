package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class MapViewModel : ViewModel() {
    abstract val shownBottomSheet: StateFlow<ShownBottomSheet?>

    abstract fun showCreateElementInOverlay(overlay: Overlay)

    abstract fun showElementInOverlay(overlay: Overlay, elementKey: ElementKey)

    abstract fun showQuest(questKey: QuestKey)

    abstract fun showCreateNote(isGpxAttached: Boolean)

    abstract fun hideQuest(questKey: QuestKey)

    abstract fun isSurvey(geometry: ElementGeometry): Boolean

    abstract fun submitEdit(
        elementEditType: ElementEditType,
        element: Element,
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
class MapViewModelImpl(
    private val mapDataSource: MapDataWithEditsSource,
    private val notesSource: NotesWithEditsSource,
    private val questController: OsmQuestController,
    private val questTypeRegistry: QuestTypeRegistry,
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val hiddenQuestsController: QuestsHiddenController,
    private val surveyChecker: SurveyChecker,
) : MapViewModel() {
    override val shownBottomSheet = MutableStateFlow<ShownBottomSheet?>(null)

    override fun showCreateElementInOverlay(overlay: Overlay) {
        shownBottomSheet.value = ShownBottomSheet.Overlay(overlay, null, null)
    }

    override fun showElementInOverlay(
        overlay: Overlay,
        elementKey: ElementKey,
    ) {
        launch(Dispatchers.IO) {
            val element = mapDataSource.get(elementKey.type, elementKey.id)
            val geometry = mapDataSource.getGeometry(elementKey.type, elementKey.id)
            if (element != null && geometry != null) {
                shownBottomSheet.value = ShownBottomSheet.Overlay(overlay, element, geometry)
            }
        }
    }

    override fun showQuest(questKey: QuestKey) {
        when (questKey) {
            is OsmNoteQuestKey -> {
                launch(Dispatchers.IO) {
                    val note = notesSource.get(questKey.noteId)
                    if (note != null) {
                        shownBottomSheet.value = ShownBottomSheet.OsmNoteQuest(note)
                    }
                }
            }
            is OsmQuestKey -> {
                launch(Dispatchers.IO) {
                    val element = mapDataSource.get(questKey.elementType, questKey.elementId)
                    val geometry = mapDataSource.getGeometry(questKey.elementType, questKey.elementId)
                    val questType = questTypeRegistry.getByName(questKey.questTypeName) as? OsmElementQuestType<*>
                    if (element != null && geometry != null && questType != null) {
                        shownBottomSheet.value = ShownBottomSheet.OsmQuest(questType, element, geometry)
                    }
                }
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
        element: Element,
        geometry: ElementGeometry,
        elementEditAction: ElementEditAction,
    ) {
        val isNearUserLocation = surveyChecker.checkIsSurvey(geometry)
        launch(Dispatchers.IO) {
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
}

/** The data necessary to show an element from the map clicked on in the bottom sheet */
sealed interface ShownBottomSheet {
    data class OsmQuest(
        val questType: OsmElementQuestType<*>,
        val element: Element,
        val geometry: ElementGeometry
    ) : ShownBottomSheet

    data class OsmNoteQuest(
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
