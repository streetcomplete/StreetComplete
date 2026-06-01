package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Action.*
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheet
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetState
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubble
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleArrowDirection
import de.westnordost.streetcomplete.ui.common.speech_bubble.SpeechBubbleNoArrow
import de.westnordost.streetcomplete.ui.ktx.isLandscape
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.theme.defaultTextLinkStyles
import de.westnordost.streetcomplete.ui.theme.titleSmall
import de.westnordost.streetcomplete.ui.util.annotateLinks
import de.westnordost.streetcomplete.util.ktx.isDeletable
import de.westnordost.streetcomplete.util.ktx.isSplittable
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** A generic quest form, with a [title], [subtitle], [hintText] and [hintImages] in the
 *  header speech bubble, then an optional [note] by another mapper shown below as another speech
 *  bubble, then finally the speech bubble containing the center-aligned [content] padded with a
 *  [contentPadding] (if there is any content) and an OK button to confirm the input.
 *
 *  **This composable requires the `LocalQuestType` composition local to be set!**
 *
 *  At the very start of the text button row, there's a text button labeled "Uh…" that, when tapped,
 *  opens a dropdown menu containing [otherAnswers] (defined from start to bottom). */
@Composable
fun QuestForm(
    isComplete: Boolean,
    onClickOk: () -> Unit,
    on: (Action) -> Unit,
    modifier: Modifier = Modifier,
    featureDictionary: FeatureDictionary = koinInject(),
    hasChanges: Boolean = isComplete,
    title: String = stringResource(LocalQuestType.current!!.title),
    subtitle: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    hintText: String? = LocalQuestType.current!!.hint?.let { stringResource(it) },
    hintImages: List<DrawableResource> = LocalQuestType.current!!.hintImages,
    note: String? = LocalElement.current?.tags?.get("note"),
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    QuestForm(
        title = title,
        subtitle = subtitle,
        hintText = hintText,
        hintImages = hintImages,
        note = note,
        isComplete = isComplete,
        hasChanges = hasChanges,
        onClickOk = onClickOk,
        on = on,
        answers = emptyList(),
        otherAnswers = otherAnswers,
        contentPadding = contentPadding,
        modifier = modifier,
        content = content,
    )
}

/** A generic quest form, with a [title], [subtitle], [hintText] and [hintImages] in the
 *  header speech bubble, then an optional [note] by another mapper shown below as another speech
 *  bubble, then finally the speech bubble containing the center-aligned [content] padded with a
 *  [contentPadding] (if there is any content) and below a row of text buttons showing
 *  different [answers] (defined from start to end).
 *
 *  **This composable requires the `LocalQuestType` composition local to be set!**
 *
 *  At the very start of the text button row, there's a text button labeled "Uh…" that, when tapped,
 *  opens a dropdown menu containing [otherAnswers] (defined from start to bottom). */
@Composable
fun QuestForm(
    answers: List<AnswerItem>,
    on: (Action) -> Unit,
    modifier: Modifier = Modifier,
    featureDictionary: FeatureDictionary = koinInject(),
    title: String = stringResource(LocalQuestType.current!!.title),
    subtitle: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    hintText: String? = LocalQuestType.current!!.hint?.let { stringResource(it) },
    hintImages: List<DrawableResource> = LocalQuestType.current!!.hintImages,
    note: String? = LocalElement.current?.tags?.get("note"),
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    QuestForm(
        title = title,
        subtitle = subtitle,
        hintText = hintText,
        hintImages = hintImages,
        note = note,
        isComplete = true,
        hasChanges = false,
        onClickOk = null,
        on = on,
        answers = answers,
        otherAnswers = otherAnswers,
        contentPadding = contentPadding,
        modifier = modifier,
        content = content,
    )
}

@Composable
private fun QuestForm(
    title: String,
    subtitle: AnnotatedString?,
    hintText: String?,
    hintImages: List<DrawableResource>,
    note: String?,
    isComplete: Boolean,
    hasChanges: Boolean,
    onClickOk: (() -> Unit)?,
    on: (Action) -> Unit,
    answers: List<AnswerItem>,
    otherAnswers: @Composable () -> List<AnswerItem>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    mapDataWithEditsSource: MapDataWithEditsSource = koinInject(),
    content: @Composable (BoxScope.() -> Unit)?,
) {
    val windowInfo = LocalWindowInfo.current

    val initialState =
        if (LocalWindowInfo.current.isLandscape) BottomSheetState.Expanded
        else BottomSheetState.Collapsed

    val elevation = 4.dp

    val element = LocalElement.current!!

    var confirmCantSay by remember { mutableStateOf(false) }

    @Composable
    fun createDefaultOtherAnswers(): List<AnswerItem> {
        val result = ArrayList<AnswerItem>()
        if (
            element is Node // add moveNodeAnswer only if it's a free floating node
            && mapDataWithEditsSource.getWaysForNode(element.id).isEmpty()
        ) {
            result.add(AnswerItem(stringResource(Res.string.move_node)) { on(MoveNode) })
        }
        if (element.isPlaceOrDisusedPlace()) {
            result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_does_not_exist)) { on(ReplacePoi) })
        }
        if (element.isDeletable()) {
            result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_does_not_exist)) { on(DeletePoi) })
        }
        if (element.isSplittable()) {
            result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_differs_along_the_way)) { on(SplitWay) })
        }
        result.add(AnswerItem(stringResource(Res.string.quest_generic_answer_notApplicable)) { confirmCantSay = true })
        return result
    }

    Box(modifier = modifier.sizeIn(maxWidth = Dimensions.getMaxQuestFormWidth(windowInfo))) {
        BottomSheet(
            initialState = initialState,
            peekHeight = Dimensions.QuestFormPeekHeight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeechBubble(
                    elevation = elevation,
                    arrowDirection = SpeechBubbleArrowDirection.Top,
                    arrowPlacementBias = 0.1f,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    QuestHeader(
                        title = title,
                        subtitle = subtitle,
                        hintText = hintText,
                        hintImages = hintImages,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (note != null) {
                    NoteBubble(
                        text = note,
                        elevation = elevation,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    )
                }

                QuestAnswerBubble(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = elevation,
                    answers = answers,
                    otherAnswers = { otherAnswers() + createDefaultOtherAnswers() },
                    contentPadding = contentPadding,
                    content = content,
                )
            }
        }
        if (onClickOk != null) {
            FloatingOkButton(
                visible = isComplete,
                onClick = onClickOk,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .safeDrawingPadding()
                    .padding(8.dp)
            )
        }
    }

    if (confirmCantSay) {
        CantSayDialog(
            onDismissRequest = { confirmCantSay = false },
            onLeaveNote = { on(Action.LeaveNote) },
            onHideQuest = { on(Action.HideQuest) },
        )
    }
}

/** Speech bubble (without arrow) that contains a note another user left for this object */
@Composable
private fun NoteBubble(
    text: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
) {
    SpeechBubbleNoArrow(
        modifier = modifier,
        elevation = elevation
    ) {
        Column {
            Text(
                text = stringResource(Res.string.note_for_object),
                style = MaterialTheme.typography.titleSmall
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                SelectionContainer {
                    Text(
                        text = text.annotateLinks(MaterialTheme.typography.defaultTextLinkStyles()),
                        style = MaterialTheme.typography.body2,
                    )
                }
            }
        }
    }
}


/*
@Composable
fun QuestForm(
    viewModel: OsmQuestViewModel,
    answers: QuestAnswer,
    modifier: Modifier = Modifier,
    otherAnswers: List<Answer> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable (BoxScope.() -> Unit)? = null
) {

    var confirmSplitWay by remember { mutableStateOf(false) }
    var confirmMoveNode by remember { mutableStateOf(false) }
    var confirmDeletePoi by remember { mutableStateOf(false) }
    var confirmReplacePlace by remember { mutableStateOf(false) }

    QuestForm(
        title = stringResource(
            viewModel.questType.getTitle(viewModel.element.tags) ?: viewModel.questType.title
        ),
        answers = answers,
        subtitle = nameAndLocationLabel(viewModel.element, viewModel.featureDictionary),
        hintText = viewModel.questType.hint?.let { stringResource(it) },
        hintImages = viewModel.questType.hintImages,
        note = viewModel.element.tags["note"],
        otherAnswers = otherAnswers,
        contentPadding = contentPadding,
        content = content
    )


    if (confirmSplitWay) {
        ConfirmationDialog(
            onDismissRequest = { confirmSplitWay = false },
            onConfirmed = { TODO },
            text = { Text(stringResource(Res.string.quest_split_way_description)) }
        )
    }
    if (confirmMoveNode) {
        ConfirmationDialog(
            onDismissRequest = { confirmMoveNode = false },
            onConfirmed = { TODO },
            text = { Text(stringResource(Res.string.quest_move_node_message)) }
        )
    }
    if (confirmReplacePlace) {
        ShopGoneDialog(
            onDismissRequest = { confirmReplacePlace = false },
            onSelectAnswer = { answer ->
                when (answer) {
                    is ShopType -> onShopReplacementSelected(answer.feature)
                    ShopTypeAnswer.IsShopVacant -> onShopDisusedSelected()
                    ShopTypeAnswer.LeaveNote -> composeNote()
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
            onConfirmDelete = { TODO },
            onLeaveNote = { TODO }
        )
    }
}
*/
