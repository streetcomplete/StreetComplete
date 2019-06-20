package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil

class AddMaxWeight(private val overpassServer: OverpassMapDataDao) : OsmElementQuestType<MaxWeightAnswer> {

    private val wayFilter by lazy { FiltersParser().parse("""
        ways with highway ~ trunk|primary|secondary|tertiary|unclassified|residential|living_street|service
         and !maxweight
         and bridge ~ yes|viaduct|movable|covered|tresle|cantilever
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and (access !~ private|no or (foot and foot !~ private|no))
         and area != yes
    """)}

    override val commitMessage = "Add maximum allowed weight"
    override val icon = R.drawable.ic_quest_max_weight

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_maxweight_title
    }

    override fun isApplicableTo(element: Element) = wayFilter.matches(element)

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler)
    }

    private fun getOverpassQuery(bbox: BoundingBox): String? {
        return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
                "(" +
                wayFilter.toOverpassQLString(null) +
                ");" +
                OverpassQLUtil.getQuestPrintStatement()
    }

    override fun createForm() = AddMaxWeightForm()

    override fun applyAnswerTo(answer: MaxWeightAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is MaxWeight -> {
                changes.add("maxweight", answer.value.toString())
            }
            is NoMaxWeightSign -> {
                changes.add("maxweight:signed", "no")
            }
        }
    }
}
