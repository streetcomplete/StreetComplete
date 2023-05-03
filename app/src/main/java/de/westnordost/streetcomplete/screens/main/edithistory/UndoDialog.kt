package de.westnordost.streetcomplete.screens.main.edithistory

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.edithistory.overlayIcon
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeFromVertexAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.COMMENT
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.CREATE
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.databinding.DialogUndoBinding
import de.westnordost.streetcomplete.quests.getHtmlQuestTitle
import de.westnordost.streetcomplete.util.getNameAndLocationLabel
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.view.CharSequenceText
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.setHtml
import de.westnordost.streetcomplete.view.setText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.MissingFormatArgumentException
import java.util.concurrent.FutureTask

class UndoDialog(
    context: Context,
    private val edit: Edit,
) : AlertDialog(context), KoinComponent {

    private val mapDataSource: MapDataWithEditsSource by inject()
    private val editHistoryController: EditHistoryController by inject()
    private val featureDictionaryFuture: FutureTask<FeatureDictionary> by inject(named("FeatureDictionaryFuture"))

    private val featureDictionary: FeatureDictionary get() = featureDictionaryFuture.get()

    private val binding = DialogUndoBinding.inflate(LayoutInflater.from(context))

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        binding.icon.setImageResource(edit.icon)
        val overlayResId = edit.overlayIcon
        if (overlayResId != 0) binding.overlayIcon.setImageResource(overlayResId)
        binding.createdTimeText.text =
            DateUtils.getRelativeTimeSpanString(edit.createdTimestamp, nowAsEpochMilliseconds(), DateUtils.MINUTE_IN_MILLIS)
        binding.descriptionContainer.addView(edit.descriptionView)

        setTitle(R.string.undo_confirm_title2)
        setView(binding.root)
        setButton(BUTTON_POSITIVE, context.getText(R.string.undo_confirm_positive), null) { _, _ ->
            scope.launch(Dispatchers.IO) { editHistoryController.undo(edit) }
        }
        setButton(BUTTON_NEGATIVE, context.getText(R.string.undo_confirm_negative), null, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scope.launch {
            binding.titleText.text = edit.getTitle()
            if (edit is ElementEdit) {
                binding.titleHintText.text = edit.getPrimaryElement()?.let {
                    getNameAndLocationLabel(it, context.resources, featureDictionary)
                }
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        scope.cancel()
    }

    private suspend fun ElementEdit.getPrimaryElement(): Element? {
        val key = action.elementKeys.firstOrNull() ?: return null
        return withContext(Dispatchers.IO) { mapDataSource.get(key.type, key.id) }
    }

    private suspend fun Edit.getTitle(): CharSequence = when (this) {
        is ElementEdit -> {
            if (type is QuestType) {
                getQuestTitle(type, getPrimaryElement()?.tags.orEmpty())
            }
            else context.resources.getText(type.title)
        }
        is NoteEdit -> {
            context.resources.getText(when (action) {
                CREATE -> R.string.created_note_action_title
                COMMENT -> R.string.commented_note_action_title
            })
        }
        is OsmQuestHidden -> {
            val element = withContext(Dispatchers.IO) { mapDataSource.get(elementType, elementId) }
            getQuestTitle(questType, element?.tags.orEmpty())
        }
        is OsmNoteQuestHidden -> {
            context.resources.getText(R.string.quest_noteDiscussion_title)
        }
        else -> throw IllegalArgumentException()
    }

    private val Edit.descriptionView: View get() = when (this) {
        is ElementEdit -> {
            when (action) {
                is UpdateElementTagsAction ->
                    createListOfTagUpdates(action.changes.changes)
                is DeletePoiNodeAction ->
                    createTextView(ResText(R.string.deleted_poi_action_description))
                is SplitWayAction ->
                    createTextView(ResText(R.string.split_way_action_description))
                is CreateNodeAction ->
                    createCreateNodeDescriptionView(action.tags)
                is CreateNodeFromVertexAction ->
                    createListOfTagUpdates(action.changes.changes)
                is MoveNodeAction ->
                    createTextView(ResText(R.string.move_node_action_description))
                else -> throw IllegalArgumentException()
            }
        }
        is NoteEdit -> createTextView(text?.let { CharSequenceText(it) })
        is OsmQuestHidden -> createTextView(ResText(R.string.hid_action_description))
        is OsmNoteQuestHidden -> createTextView(ResText(R.string.hid_action_description))
        else -> throw IllegalArgumentException()
    }

    private fun getQuestTitle(questType: QuestType, tags: Map<String, String>): CharSequence =
        try {
            context.resources.getHtmlQuestTitle(questType, tags)
        } catch (e: MissingFormatArgumentException) {
            /* The exception happens when the number of format strings in the quest title
             * differs from what can be "filled" by getHtmlQuestTitle. When does this happen?
             * It happens the element is null or otherwise is not at all what is expected by
             * that quest type.
             * So, this is the fallback for that case */
            context.resources.getString(questType.title, *Array(10) { "…" })
        }

    private fun createTextView(text: Text?): TextView {
        val txt = TextView(context)
        txt.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        txt.setText(text)
        txt.setTextIsSelectable(true)
        return txt
    }

    private fun createListOfTagUpdates(changes: Collection<StringMapEntryChange>): TextView {
        val txt = TextView(context)
        txt.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        txt.setHtml(changes.joinToString(separator = "", prefix = "<ul>", postfix = "</ul>") { change ->
           "<li>" +
           context.resources.getString(
               change.titleResId,
               "<tt>" + change.toLinkedTagString() + "</tt>"
           ) +
           "</li>"
        })
        return txt
    }

    private fun createCreateNodeDescriptionView(tags: Map<String, String>): TextView {
        val txt = TextView(context)
        txt.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        txt.setHtml(
            context.resources.getString(R.string.create_node_action_description) +
            tags.entries.joinToString(separator = "", prefix = "<ul>", postfix = "</ul>") { (key, value) ->
                "<li><tt>" + linkedTagString(key, value) + "</tt></li>"
            }
        )
        return txt
    }
}

private fun StringMapEntryChange.toLinkedTagString(): String =
    linkedTagString(key, when (this) {
        is StringMapEntryAdd -> value
        is StringMapEntryModify -> value
        is StringMapEntryDelete -> valueBefore
    })

private fun linkedTagString(key: String, value: String): String {
    val escapedKey = Html.escapeHtml(key)
    val escapedValue = Html.escapeHtml(value)
    val keyLink = "<a href=\"https://wiki.openstreetmap.org/wiki/Key:$escapedKey\">$escapedKey</a>"
    return "$keyLink = $escapedValue"
}

private val StringMapEntryChange.titleResId: Int get() = when (this) {
    is StringMapEntryAdd -> R.string.added_tag_action_title
    is StringMapEntryModify -> R.string.changed_tag_action_title
    is StringMapEntryDelete -> R.string.removed_tag_action_title
}
