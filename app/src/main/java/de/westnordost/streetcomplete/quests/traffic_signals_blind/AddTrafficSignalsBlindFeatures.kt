package de.westnordost.streetcomplete.quests.traffic_signals_blind

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.getQuestPrintStatement
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.data.meta.deleteCheckDatesForKey
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddTrafficSignalsBlindFeatures(
    private val overpassApi: OverpassMapDataAndGeometryApi,
    private val r: ResurveyIntervalsStore
) : OsmElementQuestType<TrafficSignalsBlindFeaturesAnswer> {

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassApi.query(getOverpassQuery(bbox), handler)
    }

    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}
        node[highway = crossing][crossing = traffic_signals] -> .all;
        
        node.all["traffic_signals:sound"]["traffic_signals:vibration"]["traffic_signals:arrow"] -> .with_tags;
        (.all; - .with_tags;) -> .without_tags;
        
        node.with_tags${olderThan(4).toOverpassQLString()} -> .old;
        
        (.without_tags; .old;);
        ${getQuestPrintStatement()}
    """.trimIndent()


    override fun isApplicableTo(element: Element): Boolean {
        if (element !is Node) return false
        val t = element.tags ?: return false
        return t["highway"] == "crossing" &&
               t["crossing"] == "traffic_signals" && (
            !t.containsKey("traffic_signals:sound") ||
            !t.containsKey("traffic_signals:vibration") ||
            !t.containsKey("traffic_signals:arrow") ||
            olderThan(4).matches(element)
        )
    }

    private fun olderThan(years: Int) =
        TagOlderThan("traffic_signals", RelativeDate(-(r * 365 * years).toFloat()))

    override val commitMessage = "Add features for blind people to traffic lights"
    override val wikiLink = "Tag:highway=traffic_signals"
    override val icon = R.drawable.ic_quest_blind_traffic_lights

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_blind_title

    override fun createForm() = AddTrafficSignalsBlindFeaturesForm()

    override fun applyAnswerTo(answer: TrafficSignalsBlindFeaturesAnswer, changes: StringMapChangesBuilder) {
        changes.addOrModify("traffic_signals:sound", answer.sound.toYesNo())
        changes.addOrModify("traffic_signals:vibration", answer.vibration.toYesNo())
        changes.addOrModify("traffic_signals:arrow", answer.arrow.toYesNo())

        // only set the check date if nothing was changed
        val isNotActuallyChangingAnything = changes.getChanges().all { change ->
            change is StringMapEntryModify && change.value == change.valueBefore
        }
        if (isNotActuallyChangingAnything) {
            changes.updateCheckDateForKey("traffic_signals")
        } else {
            changes.deleteCheckDatesForKey("traffic_signals")
        }
    }
}
