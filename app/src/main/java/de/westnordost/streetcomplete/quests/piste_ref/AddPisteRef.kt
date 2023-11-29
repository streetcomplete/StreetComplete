package de.westnordost.streetcomplete.quests.piste_ref

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.fullElementSelectionDialog
import de.westnordost.streetcomplete.quests.getPrefixedFullElementSelectionPref
import de.westnordost.streetcomplete.util.isWinter

class AddPisteRef : OsmElementQuestType<PisteRefAnswer> {

    private val elementFilter = """
        ways, relations with
          piste:type = downhill
          and !ref
          and !piste:ref
    """
    private val filter by lazy { elementFilter.toElementFilterExpression() }

    override val changesetComment = "Survey piste ref"
    override val wikiLink = "Key:piste:ref"
    override val icon = R.drawable.ic_quest_piste_ref
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return if (isWinter(mapData.nodes.firstOrNull()?.position)) mapData.filter(filter).asIterable()
            else emptyList()
    }

    override fun isApplicableTo(element: Element) = if (filter.matches(element)) null else false

    override fun getTitle(tags: Map<String, String>) = R.string.quest_piste_ref_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("ways, relations with piste:type = downhill")

    override fun createForm() = AddPisteRefForm()

    override fun applyAnswerTo(answer: PisteRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is PisteRef ->          tags["piste:ref"] = answer.ref
            is PisteConnection ->   tags["piste:type"] = "connection"
        }
    }

    override val hasQuestSettings: Boolean = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        fullElementSelectionDialog(context, prefs, this.getPrefixedFullElementSelectionPref(prefs), R.string.quest_settings_element_selection, elementFilter)
}
