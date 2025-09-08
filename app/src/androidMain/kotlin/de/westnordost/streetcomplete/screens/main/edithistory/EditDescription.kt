package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
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
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.added_tag_action_title
import de.westnordost.streetcomplete.resources.changed_tag_action_title
import de.westnordost.streetcomplete.resources.create_node_action_description
import de.westnordost.streetcomplete.resources.deleted_poi_action_description
import de.westnordost.streetcomplete.resources.hid_action_description
import de.westnordost.streetcomplete.resources.move_node_action_description
import de.westnordost.streetcomplete.resources.removed_tag_action_title
import de.westnordost.streetcomplete.resources.split_way_action_description
import de.westnordost.streetcomplete.ui.common.HtmlText
import de.westnordost.streetcomplete.util.html.replaceHtmlEntities
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
            Text(edit.text.orEmpty(), modifier)
        is OsmQuestHidden ->
            Text(stringResource(Res.string.hid_action_description), modifier)
        is OsmNoteQuestHidden ->
            Text(stringResource(Res.string.hid_action_description), modifier)
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
    return stringResource(title, "<tt>$tag</tt>")
}

private fun linkedKey(key: String): String =
    "<a href=\"https://wiki.openstreetmap.org/wiki/Key:$key\">$key</a>"

private val StringMapEntryChange.title: StringResource get() = when (this) {
    is StringMapEntryAdd -> Res.string.added_tag_action_title
    is StringMapEntryModify -> Res.string.changed_tag_action_title
    is StringMapEntryDelete -> Res.string.removed_tag_action_title
}
