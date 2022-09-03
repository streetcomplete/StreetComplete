package de.westnordost.streetcomplete.quests.osmose

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.widget.SwitchCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class OsmoseQuest(private val db: OsmoseDao, private val prefs: SharedPreferences) : OsmElementQuestType<OsmoseAnswer> {

    override fun getTitle(tags: Map<String, String>) = R.string.quest_osmose_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf("")

    override val changesetComment = "Fix osmose issues"
    override val wikiLink = "Osmose"
    override val icon = R.drawable.ic_quest_osmose
    override val defaultDisabledMessage = R.string.quest_osmose_message

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val elements = mutableListOf<Element>()
        val map = db.getAll()
        mapData.forEach {
            if (map.contains(ElementKey(it.type, it.id)))
                elements.add(it)
        }
        return elements
    }

    override fun isApplicableTo(element: Element): Boolean =
        db.get(ElementKey(element.type, element.id)) != null

    override fun createForm() = OsmoseForm(db)

    override fun applyAnswerTo(answer: OsmoseAnswer, tags: Tags, timestampEdited: Long) {
        if (answer is AdjustTagAnswer) {
            tags[answer.tag] = answer.newValue
            db.setDone(answer.uuid)
        }
    }

    override val hasQuestSettings = true

    // actual ignoring of stuff happens when downloading
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
                singleTypeElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_OSMOSE_ITEMS, "", R.string.quest_osmose_settings)
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
