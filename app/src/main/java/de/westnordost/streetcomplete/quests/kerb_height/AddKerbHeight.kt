package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.kerb.couldBeAKerb
import de.westnordost.streetcomplete.osm.kerb.findAllKerbNodes
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddKerbHeight : OsmElementQuestType<KerbHeight> {

    private val eligibleKerbsFilter by lazy { """
        nodes with
          !kerb
          or kerb ~ yes|unknown
          or kerb !~ no|rolled and kerb older today -8 years
    """.toElementFilterExpression() }

    override val changesetComment = "Determine the heights of kerbs"
    override val wikiLink = "Key:kerb"
    override val icon = R.drawable.ic_quest_kerb_type
    override val achievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_kerb_height_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.findAllKerbNodes().filter { eligibleKerbsFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!eligibleKerbsFilter.matches(element) || element !is Node || !element.couldBeAKerb()) {
            false
        } else {
            null
        }

    override fun createForm() = AddKerbHeightForm()

    override fun applyAnswerTo(answer: KerbHeight, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("kerb", answer.osmValue)
        if (answer.osmValue == "no") {
            tags.remove("barrier")
        } else {
            tags["barrier"] = "kerb"
        }
    }
}
