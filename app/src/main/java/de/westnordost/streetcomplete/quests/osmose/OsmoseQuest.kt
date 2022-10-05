package de.westnordost.streetcomplete.quests.osmose

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.widget.SwitchCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuest
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestType
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class OsmoseQuest(private val osmoseDao: OsmoseDao, private val prefs: SharedPreferences) : OtherSourceQuestType {

    override fun getTitle(tags: Map<String, String>) = R.string.quest_osmose_title

    override fun download(bbox: BoundingBox) = osmoseDao.download(bbox)

    override fun upload() = osmoseDao.reportChanges()

    override fun deleteMetadataOlderThan(timestamp: Long) = osmoseDao.deleteOlderThan(timestamp)

    override fun getQuests(bbox: BoundingBox) = osmoseDao.getAllQuests(bbox)

    override fun get(id: String): OtherSourceQuest? = osmoseDao.getQuest(id)

    override fun deleteQuest(id: String): Boolean = osmoseDao.delete(id)

    override fun onDeletedEdit(edit: ElementEdit, id: String?) {
        if (id == null)
            osmoseDao.setFromDoneToNotAnsweredNear(edit.position)
        else
            osmoseDao.setNotAnswered(id)
    }

    override fun onSyncedEdit(edit: ElementEdit, id: String?) {
        // todo: either report change here instead of in upload, or ignore it...
    }

    override val enabledInCountries: Countries
        get() = super.enabledInCountries

    override val changesetComment = "Fix osmose issues"
    override val wikiLink = "Osmose"
    override val icon = R.drawable.ic_quest_osmose
    override val defaultDisabledMessage = R.string.quest_osmose_message
    override val source = "osmose"

    override fun createForm() = OsmoseForm()

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog {
        val enable = SwitchCompat(context).apply {
            setText(R.string.quest_osmose_settings_enable)
            isChecked = prefs.getBoolean(questPrefix(prefs) + PREF_OSMOSE_ENABLE_DOWNLOAD, false)
            setOnCheckedChangeListener { _, b ->
                prefs.edit().putBoolean(questPrefix(prefs) + PREF_OSMOSE_ENABLE_DOWNLOAD, b).apply()
            }
        }
        enable.setPadding(30,10,30,10)

        return AlertDialog.Builder(context)
            .setTitle(R.string.quest_osmose_settings_what)
            .setView(enable)
            .setNegativeButton(R.string.quest_osmose_settings_items) { _,_ ->
                singleTypeElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_OSMOSE_ITEMS, "", R.string.quest_osmose_settings, false)
                    .show()
            }
            .setNeutralButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.quest_settings_osmose_level_title) { _, _ ->
                showLevelDialog(context)
            }
            .create()
    }

    private fun showLevelDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.quest_settings_osmose_level_title)
            .setItems(R.array.pref_quest_settings_osmose_levels) { _, i ->
                val levelString = when (i) {
                    1 -> "1%2C2"
                    2 -> "1%2C2%2C3"
                    else -> "1"
                }
                prefs.edit().putString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, levelString).apply()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

const val PREF_OSMOSE_ITEMS = "qs_OsmoseQuest_blocked_items"
const val PREF_OSMOSE_ENABLE_DOWNLOAD = "qs_OsmoseQuest_enable_download"
const val PREF_OSMOSE_LEVEL = "qs_OsmoseQuest_level"
