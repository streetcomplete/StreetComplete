package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.PolylinePainter
import de.westnordost.streetcomplete.screens.main.bottom_sheet.note.rememberTrackpointsPainter
import de.westnordost.streetcomplete.ui.common.HtmlText
import de.westnordost.streetcomplete.ui.ktx.fadingHorizontalScrollEdges
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.theme.surfaceContainer
import de.westnordost.streetcomplete.util.html.replaceHtmlEntities
import de.westnordost.streetcomplete.util.image.fileBitmapPainter
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Shows what an edit changed. */
@Composable
fun EditDescription(
    edit: Edit,
    modifier: Modifier = Modifier,
) {
    when (edit) {
        is ElementEdit -> {
            when (edit.action) {
                is UpdateElementTagsAction ->
                    TagUpdatesList(edit.action.changes.changes, modifier)
                is DeletePoiNodeAction ->
                    Text(stringResource(Res.string.deleted_poi_action_description), modifier)
                is SplitWayAction ->
                    Text(stringResource(Res.string.split_way_action_description), modifier)
                is CreateNodeAction ->
                    Column(modifier) {
                        Text(stringResource(Res.string.create_node_action_description))
                        TagList(edit.action.tags)
                    }
                is CreateNodeFromVertexAction ->
                    TagUpdatesList(edit.action.changes.changes, modifier)
                is MoveNodeAction ->
                    Text(stringResource(Res.string.move_node_action_description), modifier)
            }
        }
        is NoteEdit ->
            Column(modifier) {
                Text(edit.text.orEmpty())
                NoteImagesAndTrackRow(edit.track, edit.imagePaths)
            }
        is OsmQuestHidden ->
            Text(stringResource(Res.string.hid_action_description), modifier)
        is OsmNoteQuestHidden ->
            Text(stringResource(Res.string.hid_action_description), modifier)
    }
}

/** Shows a row of thumbnails recorded track + images */
@Composable
private fun NoteImagesAndTrackRow(
    trackpoints: List<Trackpoint>?,
    imagePaths: List<String>,
    modifier: Modifier = Modifier,
    fileSystem: FileSystem = koinInject()
) {
    val state = rememberLazyListState()

    val trackpointsPainter = trackpoints?.let { rememberTrackpointsPainter(it) }
    val painters = buildList {
        if (trackpointsPainter != null) add(trackpointsPainter)
        addAll(imagePaths.mapNotNull { fileBitmapPainter(fileSystem, Path(it)) })
    }
    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fadingHorizontalScrollEdges(state.scrollIndicatorState, 16.dp)
    ) {
        items(painters) { painter ->
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colors.surfaceContainer, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

/** Shows a list of OSM tags in a bullet list */
@Composable
private fun TagList(
    tags: Map<String, String>,
    modifier: Modifier = Modifier
) {
    HtmlText(
        html = tags.toHtml(),
        modifier = modifier,
    )
}

/** Shows a list of changes to OSM tags in a bullet list */
@Composable
private fun TagUpdatesList(
    changes: Collection<StringMapEntryChange>,
    modifier: Modifier = Modifier
) {
    HtmlText(
        html = changes.toHtml(),
        modifier = modifier,
    )
}

@Composable
@ReadOnlyComposable
private fun Map<String, String>.toHtml(): String {
    val result = StringBuilder()
    result.append("<ul>")
    for ((key, value) in this) {
        result.append("<li><tt>")
        result.append(linkedKey(key.replaceHtmlEntities()))
        result.append(" = ")
        result.append(value.replaceHtmlEntities())
        result.append("</tt></li>")
    }
    result.append("</ul>")
    return result.toString()
}

@Composable
private fun Collection<StringMapEntryChange>.toHtml(): String {
    val result = StringBuilder()
    result.append("<ul>")
    for (change in this) {
        result.append("<li>")
        result.append(change.toHtml())
        result.append("</li>")
    }
    result.append("</ul>")
    return result.toString()
}

@Composable
private fun StringMapEntryChange.toHtml(): String {
    val k = key.replaceHtmlEntities()

    val tag = when (this) {
        is StringMapEntryAdd -> linkedKey(k) + " = " + value.replaceHtmlEntities()
        is StringMapEntryDelete -> "<s>" + k + " = " + valueBefore.replaceHtmlEntities() + "</s>"
        is StringMapEntryModify -> {
            if (value == valueBefore) linkedKey(k) + " = " + value.replaceHtmlEntities()
            else linkedKey(k) + " = <s>" + valueBefore.replaceHtmlEntities() + "</s></tt> <tt>" + value.replaceHtmlEntities()
        }
    }
    return stringResource(title, "<tt>$tag</tt>")
}

private fun linkedKey(key: String): String =
    "<a href=\"https://wiki.openstreetmap.org/wiki/Key:$key\">$key</a>"

private val StringMapEntryChange.title: StringResource get() = when (this) {
    is StringMapEntryAdd -> Res.string.added_tag_action_title
    is StringMapEntryModify -> {
        if (value == valueBefore) Res.string.unchanged_tag_action_title
        else Res.string.changed_tag_action_title
    }
    is StringMapEntryDelete -> Res.string.removed_tag_action_title
}
