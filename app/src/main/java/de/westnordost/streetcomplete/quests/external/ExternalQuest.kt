package de.westnordost.streetcomplete.quests.external

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuest
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestType

class ExternalQuest(private val externalList: ExternalList) : OtherSourceQuestType {

    override val changesetComment = "Edit user-defined list of elements"
    override val wikiLink = "Tags"
    override val icon = R.drawable.ic_quest_external
    override val defaultDisabledMessage = R.string.quest_external_message

    override fun getTitle(tags: Map<String, String>): Int = R.string.quest_external_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf("")

    override val source: String = "external"

    override fun download(bbox: BoundingBox) = getQuests(bbox)

    override fun upload() { externalList.deleteSolved() }

    override fun getQuests(bbox: BoundingBox): Collection<OtherSourceQuest> = externalList.get(bbox)

    override fun get(id: String): OtherSourceQuest? = externalList.getQuest(id)

    override fun onAddedEdit(edit: ElementEdit, id: String) = externalList.markSolved(id)

    override fun onDeletedEdit(edit: ElementEdit, id: String?) {
        if (edit.isSynced) return // if it's a real undo, can't undelete the line any more
        id?.let { externalList.markSolved(it, false) }
    }

    override fun onSyncedEdit(edit: ElementEdit, id: String?) {
        id?.let { externalList.markSolved(it) } // just mark as solved, and bunch-delete in the end
    }

    override fun deleteQuest(id: String): Boolean = externalList.delete(id)

    override fun deleteMetadataOlderThan(timestamp: Long) { }

    override val hasQuestSettings: Boolean = false

    override fun getQuestSettingsDialog(context: Context): AlertDialog? = null

    override fun createForm() = ExternalForm(externalList)
}
