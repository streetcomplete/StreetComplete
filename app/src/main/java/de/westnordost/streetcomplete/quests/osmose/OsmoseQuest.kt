package de.westnordost.streetcomplete.quests.osmose

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
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

    override fun getTitleArgs(tags: Map<String, String>): Array<String> =
        arrayOf(tags.map { "${it.key}=${it.value}" }.sorted().toString())

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
    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(R.string.quest_osmose_settings_what)
            .setNeutralButton(R.string.quest_osmose_settings_items) { _,_ ->
                singleTypeElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_OSMOSE_ITEMS, "", R.string.quest_osmose_settings)
                    .show()
            }
            .setNegativeButton(R.string.quest_osmose_settings_disable) { _, _ ->
                prefs.edit().putBoolean(questPrefix(prefs) + PREF_OSMOSE_ENABLE_DOWNLOAD, false).apply()
            }
            .setPositiveButton(R.string.quest_osmose_settings_enable) { _, _ ->
                prefs.edit().putBoolean(questPrefix(prefs) + PREF_OSMOSE_ENABLE_DOWNLOAD, true).apply()
            }
            .create()

}

const val PREF_OSMOSE_ITEMS = "qs_OsmoseQuest_blocked_items"
const val PREF_OSMOSE_ENABLE_DOWNLOAD = "qs_OsmoseQuest_enable_download"
