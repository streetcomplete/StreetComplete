package de.westnordost.streetcomplete.screens.main.bottom_sheet.quest

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.Node
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
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.ConfirmDeleteDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerPixel
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.LocalQuestType
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.util.ktx.geometryType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun <T> OsmQuestFormContainer(
    onDismiss: () -> Unit,
    onEdit: (action: ElementEditAction) -> Unit,
    onLeaveNote: () -> Unit,
    onHideQuest: () -> Unit,
    onSplitWay: () -> Unit,
    onMoveNode: () -> Unit,
    questType: OsmElementQuestType<T>,
    element: Element,
    geometry: ElementGeometry,
    mapRotation: Float,
    mapTilt: Float,
    mapMetersPerPixel: Double,
    onSetMapMarkers: (Iterable<Marker>) -> Unit,
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

    fun onAction(action: QuestAction<T>) {
        when (action) {
            Action.Dismiss -> onDismiss()
            Action.LeaveNote -> onLeaveNote()
            Action.HideQuest -> onHideQuest()
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

    CompositionLocalProvider(
        LocalQuestType provides questType,
        LocalElement provides element,
        LocalMapRotation provides mapRotation,
        LocalMapTilt provides mapTilt,
        LocalMapMetersPerPixel provides mapMetersPerPixel,
        LocalMapMarkersCallback provides onSetMapMarkers
    ) {
        questType.Form(
            on = ::onAction,
            element = element,
            geometry = geometry,
            countryInfo = countryInfo
        )
    }

    if (confirmSplitWay) {
        ConfirmationDialog(
            onDismissRequest = { confirmSplitWay = false },
            onConfirmed = onSplitWay,
            text = { Text(stringResource(Res.string.quest_split_way_description)) }
        )
    }
    if (confirmMoveNode) {
        ConfirmationDialog(
            onDismissRequest = { confirmMoveNode = false },
            onConfirmed = onMoveNode,
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
                        onLeaveNote()
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
            onLeaveNote = onLeaveNote
        )
    }
}
