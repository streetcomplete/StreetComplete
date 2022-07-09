package de.westnordost.streetcomplete.quests.contact

import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.quests.fullElementSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix

class AddContactWebsite(private val prefs: SharedPreferences) : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
         tourism = information and information = office
         or """.trimIndent() +
        prefs.getString(questPrefix(prefs) + PREF_WEBSITE_ELEMENTS, PLACES_FOR_CONTACT_QUESTS) +
        "\n) and !website and !contact:website and !contact:facebook and !contact:instagram and !brand and name"

    override val changesetComment = "Add website"
    override val wikiLink = "Key:website"
    override val icon = R.drawable.ic_quest_website

    override fun getTitle(tags: Map<String, String>) = R.string.quest_contact_website

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddContactWebsiteForm()

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        tags["website"] = answer
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context) =
        fullElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_WEBSITE_ELEMENTS, R.string.quest_settings_contact, PLACES_FOR_CONTACT_QUESTS)

}

private const val PREF_WEBSITE_ELEMENTS = "qs_AddContactWebsite_element_selection"
