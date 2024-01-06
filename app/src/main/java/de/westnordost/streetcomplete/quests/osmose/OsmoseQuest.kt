package de.westnordost.streetcomplete.quests.osmose

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.ktx.dpToPx

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
            setOnClickListener {showIgnoredItemsDialog(context) }
        }
        val appLanguage = SwitchCompat(context).apply {
            setText(R.string.quest_osmose_use_app_language)
            isChecked = prefs.getBoolean(PREF_OSMOSE_APP_LANGUAGE, false)
        }
        val appLanguageInfo = TextView(context).apply {
            setText(R.string.quest_osmose_use_app_language_information)
            val padding = context.dpToPx(8).toInt()
            setPadding(padding, 0, padding, 0)
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { setText(R.string.quest_settings_osmose_level_title) })
            addView(high)
            addView(medium)
            addView(low)
            addView(hide)
            addView(appLanguage)
            addView(appLanguageInfo)
        }

        return AlertDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.quest_osmose_title, "…"))
            .setViewWithDefaultPadding(ScrollView(context).apply { addView(layout) })
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val levelString = listOfNotNull(
                    if (high.isChecked) 1 else null,
                    if (medium.isChecked) 2 else null,
                    if (low.isChecked) 3 else null,
                ).takeIf { it.isNotEmpty() }?.joinToString("%2C") ?: ""
                if (levelString != prefs.getString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, OSMOSE_DEFAULT_IGNORED_ITEMS)) {
                    prefs.edit { putString(questPrefix(prefs) + PREF_OSMOSE_LEVEL, levelString) }
                    downloadEnabled = levelString != ""
                    osmoseDao.reloadIgnoredItems()
                    OsmQuestController.reloadQuestTypes() // actually this is doing a bit more than necessary, but whatever
                }
                prefs.edit { putBoolean(PREF_OSMOSE_APP_LANGUAGE, appLanguage.isChecked) }
            }
            .create()
    }

    // dialog broken if list is long and button text is long
    //  but that actually looks like an Android issue,,,
    //  anyway, with current short button text all buttons are in one line, and there should be no problem
    private fun showIgnoredItemsDialog(context: Context) {
        val pref = questPrefix(prefs) + PREF_OSMOSE_ITEMS
        val items = prefs.getString(pref, OSMOSE_DEFAULT_IGNORED_ITEMS)!!.split("§§").filter { it.isNotEmpty() }.toTypedArray()
        val itemsForRemoval = mutableSetOf<String>()
        var d: AlertDialog? = null
        d = AlertDialog.Builder(context)
            .setMultiChoiceItems(items, null) { _, i, x ->
                if (x) itemsForRemoval.add(items[i])
                else itemsForRemoval.remove(items[i])
                d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = itemsForRemoval.isNotEmpty()
            }
            .setPositiveButton(R.string.quest_osmose_remove_checked) { _, _ ->
                prefs.edit { putString(pref, items.filterNot { it in itemsForRemoval }.joinToString("§§")) }
                osmoseDao.reloadIgnoredItems()
                OsmQuestController.reloadQuestTypes()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
                prefs.edit { remove(pref) }
                osmoseDao.reloadIgnoredItems()
                OsmQuestController.reloadQuestTypes()
            }.create()
        d.show()
        d.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = itemsForRemoval.isNotEmpty()
        d.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(pref)
    }
}

const val PREF_OSMOSE_ITEMS = "qs_OsmoseQuest_blocked_items"
const val PREF_OSMOSE_LEVEL = "qs_OsmoseQuest_level"
const val PREF_OSMOSE_APP_LANGUAGE = "qs_OsmoseQuest_app_language" // do not use the quest settings prefix here, as it doesn't make sense for language

// items that have associated SC quests/overlays are disabled by default
// same for issues related to ignored relation types
// §§ is used as separator
const val OSMOSE_DEFAULT_IGNORED_ITEMS =
    "3230/32301" + "§§" + // "Probably only for bottles, not any type of glass"
    "4061/40610" + "§§" + // "object needs review" (fixme poi "quest")
    "7130/71301" + "§§" + // "Missing maxheight tag"
    "2060/1" + "§§" + // "addr:housenumber or addr:housename without addr:street, addr:district, addr:neighbourhood, addr:quarter, addr:suburb, addr:place or addr:hamlet must be in a associatedStreet relation"
    "3250" + "§§" + // "Invalid Opening Hours" (will be not be asked immediately, but frequently re-surveyed, at least of the option is on)
    "shop=yes is unspecific. Please replace ''yes'' by a specific value." + "§§" +
//    alternative for all languages: 9002/9002007 and contains "shop=yes" or "shop = yes" (thanks, translator)
    "barrier=yes is unspecific. Please replace ''yes'' by a specific value." + "§§" +
    "traffic_calming=yes is unspecific. Please replace ''yes'' by a specific value" + "§§" +
    "amenity=recycling without recycling:*" + "§§" +
//    alternative for all languages: 9001/9001001 and contains "recycling:*"
    "amenity=recycling without recycling_type=container or recycling_type=centre" + "§§" +
//    alternative for all languages: 9001/9001001 and contains all 3 tags
    "emergency=fire_hydrant without fire_hydrant:type" + "§§" +
//    alternative for all languages: 9001/9001001 and contains "emergency=fire_hydrant" and "fire_hydrant:type"
    "Combined foot- and cycleway without segregated." + "§§" +
//    alternative for all languages: 9001/9001001 and contains "segregated"
    "leisure=pitch without sport" + "§§" +
//    alternative for all languages and types: 9001/9001001 and contains "leisure=pitch" and "sport"
    "The tag `parking:lane:both` is deprecated in favour of `parking:both`" + "§§" +
    "The tag `parking:lane:left` is deprecated in favour of `parking:left`" + "§§" +
    "The tag `parking:lane:right` is deprecated in favour of `parking:right`" + "§§" +
//    alternative for all languages and types: 4010 and contains "parking:lane:*" and "parking:<same>"
    "The tag `parking:orientation` is deprecated in favour of `orientation`" + "§§" +
    "Same value of cycleway:left and cycleway:right" + "§§" + // there is no quest, but SC may cause this and does not understand the "fix"
//    alternative for all languages: 9001 and contains "cycleway:left" and "cycleway:right"
// "tracktype=grade4 together with surface=asphalt" -> how to do it properly? current system won't work, or needs blacklisting all combinations
//    alternative for all languages and types: 9001/9001001 and contains "tracktype=" and "surface="
    "female=yes together with male=yes" + "§§" + // this is not necessarily the same as unisex
    // relation-related stuff below
    "1260" + "§§" + // Osmosis_Relation_Public_Transport
    "2140" + "§§" + // missing tags on public transport relations / stops
    "1140" + "§§" + // missing tag or role
    "1200" + "§§" + //  1-member relation
    "9007" + "§§" // various relation related issues, usually missing tags

