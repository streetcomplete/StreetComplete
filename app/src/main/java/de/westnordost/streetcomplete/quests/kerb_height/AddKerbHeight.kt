package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate

class AddKerbHeight : OsmElementQuestType<String> {

    private val eligibleKerbsFilter by lazy { """
        nodes with
          !kerb
          or kerb ~ yes|unknown
          or kerb !~ no|rolled and kerb older today -8 years
    """.toElementFilterExpression() }

    override val commitMessage = "Add kerb height info"
    override val wikiLink = "Key:kerb"
    override val icon = R.drawable.ic_quest_kerb_type

    override fun getTitle(tags: Map<String, String>) = R.string.quest_kerb_height_title

    override fun createForm() = AddKerbHeightForm()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData.findAllKerbNodes().filter { eligibleKerbsFilter.matches(it) }
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("kerb", answer)
    }
}
