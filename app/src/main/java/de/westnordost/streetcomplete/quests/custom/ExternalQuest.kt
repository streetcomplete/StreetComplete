package de.westnordost.streetcomplete.quests.custom

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuest
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestType

// custom quest was named "external quest" initially, but this is rather unclear as every
// "OtherSourceQuestType" is based on external sources, i.e. not just map data
// this class and the source string retain the old name because they are also used in database
class ExternalQuest(private val customQuestList: CustomQuestList) : OtherSourceQuestType {

    override val changesetComment = "Edit user-defined list of elements"
    override val wikiLink = "Tags"
    override val icon = R.drawable.ic_quest_custom
    override val defaultDisabledMessage = R.string.quest_custom_quest_message

    override fun getTitle(tags: Map<String, String>): Int = R.string.quest_custom_quest_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf("")

    override val source: String = "external"

    override fun download(bbox: BoundingBox) = getQuests(bbox)

    override var downloadEnabled = true // it's not actually a download, so no need to ever disable

    override fun upload() { customQuestList.deleteSolved() }

    override fun getQuests(bbox: BoundingBox): Collection<OtherSourceQuest> = customQuestList.get(bbox)

    override fun get(id: String): OtherSourceQuest? = customQuestList.getQuest(id)

    override fun onAddedEdit(edit: ElementEdit, id: String) = customQuestList.markSolved(id)

    override fun onDeletedEdit(edit: ElementEdit, id: String?) {
        if (edit.isSynced) return // if it's a real undo, can't undelete the line any more
        id?.let { customQuestList.markSolved(it, false) }
    }

    override fun onSyncedEdit(edit: ElementEdit, id: String?) {
        id?.let { customQuestList.markSolved(it) } // just mark as solved, and bunch-delete in the end
    }

    override fun onSyncEditFailed(edit: ElementEdit, id: String?) {
        id?.let { customQuestList.markSolved(it, false) }
    }

    override suspend fun onUpload(edit: ElementEdit, id: String?): Boolean = true

    override fun deleteQuest(id: String): Boolean = customQuestList.delete(id)

    override fun deleteMetadataOlderThan(timestamp: Long) { }

    override val hasQuestSettings: Boolean = false

    override fun getQuestSettingsDialog(context: Context): AlertDialog? = null

    override fun createForm() = CustomQuestForm(customQuestList)
}
