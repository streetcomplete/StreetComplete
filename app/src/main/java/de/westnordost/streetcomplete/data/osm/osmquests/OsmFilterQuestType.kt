package de.westnordost.streetcomplete.data.osm.osmquests

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.fullElementSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix

/** Quest type where each quest refers to one OSM element where the element selection is based on
 *  a simple [element filter expression][de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression].
 */
abstract class OsmFilterQuestType<T> : OsmElementQuestType<T> {

    val filter by lazy {
        prefs.getString(getPref(), elementFilter)!!.toElementFilterExpression()
    }

    protected abstract val elementFilter: String

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter(prefs.getString(getPref(), elementFilter)!!).asIterable()

    override fun isApplicableTo(element: Element) = filter.matches(element)

    override val hasQuestSettings: Boolean = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog? =
        fullElementSelectionDialog(context, prefs, getPref(), R.string.quest_settings_element_selection, elementFilter)

    private fun getPref() = questPrefix(prefs) + "qs_${name}_full_element_selection"
}
