package de.westnordost.streetcomplete.quests.show_poi

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class ShowFixme : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          (fixme or FIXME)
          and fixme !~ "${prefs.getString(questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT)}"
          and FIXME !~ "${prefs.getString(questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT)}"
    """
    override val changesetComment = "Remove/adjust fixme"
    override val wikiLink = "Key:fixme"
    override val icon = R.drawable.ic_quest_poi_fixme
    override val dotColor = "red"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_fixme
    override val dotLabelSources = listOf("fixme", "FIXME")

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fixme_title

    override fun createForm() = ShowFixmeAnswerForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (!answer) {
            tags.remove("fixme")
            tags.remove("FIXME")
        }
    }

    override val hasQuestSettings = true

    // actual ignoring of stuff happens when downloading
    override fun getQuestSettingsDialog(context: Context) =
        singleTypeElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_FIXME_IGNORE, FIXME_IGNORE_DEFAULT, R.string.quest_settings_fixme_title)
}

private const val PREF_FIXME_IGNORE = "qs_ShowFixme_ignore_values"
private const val FIXME_IGNORE_DEFAULT = "yes|continue|continue?"
