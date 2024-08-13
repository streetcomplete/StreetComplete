package de.westnordost.streetcomplete.screens.main.edithistory

import android.text.Html
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeFromVertexAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.html.replaceHtmlEntities
import de.westnordost.streetcomplete.util.ktx.openUri
import java.text.DateFormat

/** Confirmation dialog for undoing an edit */
@Composable
fun UndoDialog(
    edit: Edit,
    element: Element?,
    featureDictionaryLazy: Lazy<FeatureDictionary>,
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
) {
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = onConfirmed,
        title = { Text(stringResource(R.string.undo_confirm_title2)) },
        text = {
            EditDetails(
                edit = edit,
                element = element,
                featureDictionaryLazy = featureDictionaryLazy
            )
        },
        confirmButtonText = stringResource(R.string.undo_confirm_positive),
        cancelButtonText = stringResource(R.string.undo_confirm_negative),
    )
}

/** Shows details about an edit - time, icon, title, location and what was changed */
@Composable
private fun EditDetails(
    edit: Edit,
    element: Element?,
    featureDictionaryLazy: Lazy<FeatureDictionary>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = DateFormat.getTimeInstance(DateFormat.SHORT).format(edit.createdTimestamp),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.alpha(ContentAlpha.medium)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            EditImage(edit)
            Column {
                Text(
                    text = edit.getTitle(element?.tags),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.alpha(ContentAlpha.high)
                )
                if (edit is ElementEdit && element != null) {
                    val nameAndLocation = remember(element, context.resources) {
                        getNameAndLocationLabel(element, context.resources, featureDictionaryLazy.value)
                    }
                    if (nameAndLocation != null) {
                        Text(
                            text = nameAndLocation.toString(),
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.alpha(ContentAlpha.medium)
                        )
                    }
                }
            }
        }
        Divider()
        EditDescription(
            edit = edit,
            onClickLink = { context.openUri(it) }
        )
    }
}

@Composable
private fun EditDescription(
    edit: Edit,
    onClickLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (edit) {
        is ElementEdit -> {
            when (edit.action) {
                is UpdateElementTagsAction ->
                    TagUpdatesList(
                        changes = edit.action.changes.changes,
                        onClickLink = onClickLink,
                        modifier = modifier
                    )

                is DeletePoiNodeAction ->
                    Text(stringResource(R.string.deleted_poi_action_description), modifier)

                is SplitWayAction ->
                    Text(stringResource(R.string.split_way_action_description), modifier)

                is CreateNodeAction ->
                    TODO()

                is CreateNodeFromVertexAction ->
                    TagUpdatesList(
                        changes = edit.action.changes.changes,
                        onClickLink = onClickLink,
                        modifier = modifier
                    )

                is MoveNodeAction ->
                    Text(stringResource(R.string.move_node_action_description), modifier)
            }
        }

        is NoteEdit ->
            Text(edit.text.orEmpty(), modifier)

        is OsmQuestHidden ->
            Text(stringResource(R.string.hid_action_description), modifier)

        is OsmNoteQuestHidden ->
            Text(stringResource(R.string.hid_action_description), modifier)
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun TagUpdatesList(
    changes: Collection<StringMapEntryChange>,
    modifier: Modifier = Modifier,
    onClickLink: (String) -> Unit
) {
    val linkColor = MaterialTheme.colors.secondary
    val text = remember(changes, linkColor) {
        changes.toAnnotatedString(linkColor)
    }

    SelectionContainer {
        ClickableText(
            text = text,
            modifier = modifier,
        ) { offset ->
            val link = text.getUrlAnnotations(offset, offset).firstOrNull()?.item?.url
            if (link != null) { onClickLink(link) }
        }
    }
}

private fun Collection<StringMapEntryChange>.toAnnotatedString(linkColor: Color): AnnotatedString {
    val builder = AnnotatedString.Builder()
    for (change in this) {
        builder.pushStyle(ParagraphStyle(textIndent = TextIndent(restLine = 8.sp)))
        builder.append(change.toAnnotatedString(linkColor))
        builder.pop()
    }
    return builder.toAnnotatedString()
}

@OptIn(ExperimentalTextApi::class)
private fun StringMapEntryChange.toAnnotatedString(linkColor: Color): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(when (this) {
        is StringMapEntryAdd -> '+'
        is StringMapEntryModify -> '*'
        is StringMapEntryDelete -> '-'
    }) // TODO use strings after all?
    builder.append(' ')
    builder.pushStyle(SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = Color(0x33bbbbbb)
    ))

    when (this) {
        // link only when adding or modifying
        is StringMapEntryAdd,
        is StringMapEntryModify -> {
            builder.pushStyle(SpanStyle(
                textDecoration = TextDecoration.Underline,
                color = linkColor,
            ))
            val escapedKey = key.replaceHtmlEntities()
            builder.pushUrlAnnotation(UrlAnnotation(
                "<a href=\"https://wiki.openstreetmap.org/wiki/Key:$escapedKey\">"
            ))
        }
        // strike-through for deleting
        is StringMapEntryDelete -> {
            builder.pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
        }
    }
    builder.append(key)
    when (this) {
        is StringMapEntryAdd,
        is StringMapEntryModify -> {
            builder.pop()
            builder.pop()
        }
        is StringMapEntryDelete -> {
            builder.pop()
        }
    }
    builder.append(" = ")
    builder.append(when (this) {
        is StringMapEntryAdd -> value
        is StringMapEntryModify -> value
        is StringMapEntryDelete -> valueBefore
    })
    builder.pop()
    return builder.toAnnotatedString()
}

