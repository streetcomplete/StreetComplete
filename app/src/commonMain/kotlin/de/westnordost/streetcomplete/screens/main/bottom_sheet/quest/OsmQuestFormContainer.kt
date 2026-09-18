package de.westnordost.streetcomplete.screens.main.bottom_sheet.quest

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.osm.places.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.places.getPlaceAsDisused
import de.westnordost.streetcomplete.quests.shop_type.ShopGoneDialog
import de.westnordost.streetcomplete.quests.shop_type.ShopType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeAnswer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.BottomSheetFormState
import de.westnordost.streetcomplete.screens.main.bottom_sheet.BottomSheetSubForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.move_node.MoveNodeForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.LeaveNoteInsteadForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.split_way.SplitWayForm
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.CantSayDialog
import de.westnordost.streetcomplete.ui.common.quest.ConfirmDeleteDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.LocalLastMapClick
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerDp
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.LocalQuestType
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.util.ReplaceBottomSheetTransitionSpec
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.util.ktx.geometryType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Container in which all quest forms are housed.
 *
 *  Takes care of showing the forms for the "other answers" (leave note, split way, move node)
 *  and associated confirmation dialogs and animates between the overlay form and those.
 *
 *  @param onSetMapMarkers is called when the form shown wishes to show markers on the map. E.g. the
 *         split way form and level form shows markers
 *
 *  @param formState which form is shown, shared with what it places on the map
 */
@Composable
fun <T> OsmQuestFormContainer(
    onDismiss: () -> Unit,
    onEdit: (action: ElementEditAction) -> Unit,
    onLeaveNote: (noteText: String, noteImagePaths: List<String>) -> Unit,
    onHideQuest: () -> Unit,
    questType: OsmElementQuestType<T>,
    element: Element,
    geometry: ElementGeometry,
    mapPosition: LatLon?,
    mapRotation: Float,
    mapTilt: Float,
    mapMetersPerDp: Double,
    onSetMapMarkers: (Iterable<Marker>?) -> Unit,
    formState: BottomSheetFormState,
    lastMapClick: MapClick?,
    modifier: Modifier = Modifier,
    countryBoundaries: CountryBoundaries = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
    countryInfos: CountryInfos = koinInject(),
) {
    val center = geometry.center
    val countryInfo = remember(center) { countryInfos.get(countryBoundaries, center) }

    var confirmSplitWay by remember { mutableStateOf(false) }
    var confirmMoveNode by remember { mutableStateOf(false) }
    var confirmDeletePoi by remember { mutableStateOf(false) }
    var confirmReplacePlace by remember { mutableStateOf(false) }
    var confirmCantSay by remember { mutableStateOf(false) }

    fun showForm(form: BottomSheetSubForm) {
        formState.subForm = form
        onSetMapMarkers(null)
    }

    fun onAction(action: QuestAction<T>) {
        when (action) {
            Action.Dismiss -> onDismiss()
            Action.LeaveNote -> showForm(BottomSheetSubForm.LeaveNote)
            Action.HideQuest -> onHideQuest()
            Action.CantSay -> confirmCantSay = true
            Action.SplitWay -> confirmSplitWay = true
            Action.MoveNode -> confirmMoveNode = true
            Action.DeletePoi -> confirmDeletePoi = true
            Action.ReplacePoi -> confirmReplacePlace = true
            is Answer<T> -> {
                val changesBuilder = StringMapChangesBuilder(element.tags)
                questType.applyAnswerTo(action.value, changesBuilder, geometry, element.timestampEdited)
                val changes = changesBuilder.create()
                onEdit(UpdateElementTagsAction(element, changes))
            }
        }
    }

    AnimatedContent(
        targetState = formState.subForm,
        transitionSpec = ReplaceBottomSheetTransitionSpec,
        modifier = modifier,
    ) { currentState ->
        CompositionLocalProvider(
            LocalQuestType provides questType,
            LocalElement provides element,
            LocalMapRotation provides mapRotation,
            LocalMapTilt provides mapTilt,
            LocalMapMetersPerDp provides mapMetersPerDp,
            LocalLastMapClick provides lastMapClick,
            // AnimatedContent keeps the outgoing form alive until its transition ends.
            LocalMapMarkersCallback provides { if (currentState == formState.subForm) onSetMapMarkers(it) },
        ) {
            when (currentState) {
                BottomSheetSubForm.Main -> {
                    questType.Form(
                        on = ::onAction,
                        element = element,
                        geometry = geometry,
                        countryInfo = countryInfo
                    )
                }
                BottomSheetSubForm.LeaveNote -> {
                    LeaveNoteInsteadForm(
                        onLeaveNote = { text, noteImagePaths ->
                            onLeaveNote(text, noteImagePaths)
                        },
                        onDismiss = onDismiss,
                        editType = questType,
                        element = element,
                    )
                }
                BottomSheetSubForm.SplitWay -> {
                    SplitWayForm(
                        onConfirmed = { onEdit(SplitWayAction(element, it)) },
                        onDismiss = onDismiss,
                        mapPosition = mapPosition,
                        way = element as Way,
                        wayGeometry = geometry as ElementPolylinesGeometry,
                        snipAnimation = formState.snipAnimation,
                    )
                }
                BottomSheetSubForm.MoveNode -> {
                    MoveNodeForm(
                        onConfirmed = { onEdit(MoveNodeAction(element, it)) },
                        onDismiss = onDismiss,
                        mapPosition = mapPosition,
                        node = element as Node,
                    )
                }
            }
        }
    }

    if (confirmSplitWay) {
        ConfirmationDialog(
            onDismissRequest = { confirmSplitWay = false },
            onConfirmed = { showForm(BottomSheetSubForm.SplitWay) },
            text = { Text(stringResource(Res.string.quest_split_way_description)) }
        )
    }
    if (confirmMoveNode) {
        ConfirmationDialog(
            onDismissRequest = { confirmMoveNode = false },
            onConfirmed = { showForm(BottomSheetSubForm.MoveNode) },
            text = { Text(stringResource(Res.string.quest_move_node_message)) }
        )
    }
    if (confirmReplacePlace) {
        ShopGoneDialog(
            onDismissRequest = { confirmReplacePlace = false },
            onSelectAnswer = { answer ->
                when (answer) {
                    is ShopType -> {
                        val builder = StringMapChangesBuilder(element.tags)
                        answer.feature.applyReplacePlaceTo(builder)
                        onEdit(UpdateElementTagsAction(element, builder.create()))
                    }
                    ShopTypeAnswer.IsShopVacant -> {
                        val vacantShop = featureDictionary.getPlaceAsDisused(element, country = countryInfo.countryOrSubdivisionCode)
                        val builder = StringMapChangesBuilder(element.tags)
                        vacantShop.applyReplacePlaceTo(builder)
                        onEdit(UpdateElementTagsAction(element, builder.create()))
                    }
                    ShopTypeAnswer.LeaveNote -> {
                        showForm(BottomSheetSubForm.LeaveNote)
                    }
                }
            },
            featureDictionary = featureDictionary,
            geometryType = element.geometryType,
            countryCode = countryInfo.countryOrSubdivisionCode,
        )
    }
    if (confirmDeletePoi) {
        ConfirmDeleteDialog(
            onDismissRequest = { confirmDeletePoi = false },
            onConfirmDelete = {
                onEdit(DeletePoiNodeAction(element as Node))
            },
            onLeaveNote = {
                showForm(BottomSheetSubForm.LeaveNote)
            }
        )
    }
    if (confirmCantSay) {
        CantSayDialog(
            onDismissRequest = { confirmCantSay = false },
            onLeaveNote = { showForm(BottomSheetSubForm.LeaveNote) },
            onHideQuest = { onHideQuest() },
        )
    }
}

