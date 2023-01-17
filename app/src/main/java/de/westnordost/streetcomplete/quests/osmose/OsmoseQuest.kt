package de.westnordost.streetcomplete.quests.osmose

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class OsmoseQuest(private val osmoseDao: OsmoseDao) : ExternalSourceQuestType {

    override fun getTitle(tags: Map<String, String>) = R.string.quest_osmose_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf("")

    override fun download(bbox: BoundingBox) = osmoseDao.download(bbox)

    override fun upload() = osmoseDao.reportFalsePositives()

    override fun deleteMetadataOlderThan(timestamp: Long) = osmoseDao.deleteOlderThan(timestamp)

    override fun getQuests(bbox: BoundingBox) = osmoseDao.getAllQuests(bbox)

    override fun get(id: String): ExternalSourceQuest? = osmoseDao.getQuest(id)

    override fun deleteQuest(id: String): Boolean = osmoseDao.delete(id)

    override fun onAddedEdit(edit: ElementEdit, id: String) = osmoseDao.setDone(id)

    override fun onDeletedEdit(edit: ElementEdit, id: String?) {
        if (edit.isSynced) return // already reported as done
        if (id != null)
            osmoseDao.setNotAnswered(id)
    }

    override fun onSyncEditFailed(edit: ElementEdit, id: String?) {
        if (id != null) osmoseDao.delete(id)
    }

    override suspend fun onUpload(edit: ElementEdit, id: String?): Boolean {
        // check whether issue still exists before uploading
        if (id == null) return true // if we don't have an id, assume it's ok
        return osmoseDao.doesIssueStillExist(id)
    }

    override fun onSyncedEdit(edit: ElementEdit, id: String?) {
        if (id != null)
            osmoseDao.reportChange(id, false) // edits are never false positive
    }

    override val enabledInCountries: Countries
        get() = super.enabledInCountries

    override val changesetComment = "Fix osmose issues"
    override val wikiLink = "Osmose"
    override val icon = R.drawable.ic_quest_osmose
    override val defaultDisabledMessage = R.string.quest_osmose_message
    override val source = "osmose"

    override fun createForm() = OsmoseForm()

    override fun getQuestSettingsDialog(context: Context): AlertDialog {
        val levels = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, "")!!.split("%2C").mapNotNull { it.toIntOrNull() }
        val high = CheckBox(context).apply {
            setText(R.string.quest_settings_osmose_level_high)
            isChecked = levels.contains(1)
        }
        val medium = CheckBox(context).apply {
            setText(R.string.quest_settings_osmose_level_medium)
            isChecked = levels.contains(2)
        }
        val low = CheckBox(context).apply {
            setText(R.string.quest_settings_osmose_level_low)
            isChecked = levels.contains(3)
        }
        val hide = Button(context).apply {
            setText(R.string.quest_osmose_settings_items)
            setOnClickListener {
                singleTypeElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_OSMOSE_ITEMS, "", R.string.quest_osmose_settings, false)
                    .show()
            }
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { setText(R.string.quest_settings_osmose_level_title) })
            addView(high)
            addView(medium)
            addView(low)
            addView(hide)
            setPadding(30, 10, 30, 10)
        }

        return AlertDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.quest_osmose_title, "…"))
            .setView(ScrollView(context).apply { addView(layout) })
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val levelString = listOfNotNull(
                    if (high.isChecked) 1 else null,
                    if (medium.isChecked) 2 else null,
                    if (low.isChecked) 3 else null,
                ).takeIf { it.isNotEmpty() }?.joinToString("%2C") ?: ""
                prefs.edit().putString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, levelString).apply()
                downloadEnabled = levelString != ""
            }
            .create()
    }
}

const val PREF_OSMOSE_ITEMS = "qs_OsmoseQuest_blocked_items"
const val PREF_OSMOSE_LEVEL = "qs_OsmoseQuest_level"
