package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.ui.common.HtmlText
import de.westnordost.streetcomplete.util.html.replaceHtmlEntities

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
                    Text(stringResource(R.string.deleted_poi_action_description), modifier)
                is SplitWayAction ->
                    Text(stringResource(R.string.split_way_action_description), modifier)
                is CreateNodeAction ->
                    Column(modifier) {
                        Text(stringResource(R.string.create_node_action_description))
                        TagList(edit.action.tags)
                    }
                is CreateNodeFromVertexAction ->
                    TagUpdatesList(edit.action.changes.changes, modifier)
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
@ReadOnlyComposable
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
@ReadOnlyComposable
private fun StringMapEntryChange.toHtml(): String {
    val k = key.replaceHtmlEntities()
    val v = when (this) {
        is StringMapEntryAdd -> value
        is StringMapEntryModify -> value
        is StringMapEntryDelete -> valueBefore
    }.replaceHtmlEntities()

    val tag = when (this) {
        is StringMapEntryAdd -> linkedKey(k) + " = $v"
        is StringMapEntryDelete -> "<s>$k = $v</s>"
        is StringMapEntryModify -> linkedKey(k) + " = $v"
    }
    return stringResource(titleResId, "<tt>$tag</tt>")
}

private fun linkedKey(key: String): String =
    "<a href=\"https://wiki.openstreetmap.org/wiki/Key:$key\">$key</a>"

private val StringMapEntryChange.titleResId: Int get() = when (this) {
    is StringMapEntryAdd -> R.string.added_tag_action_title
    is StringMapEntryModify -> R.string.changed_tag_action_title
    is StringMapEntryDelete -> R.string.removed_tag_action_title
}
