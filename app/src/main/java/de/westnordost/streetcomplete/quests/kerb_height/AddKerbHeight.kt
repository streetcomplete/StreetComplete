package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR

class AddKerbHeight : OsmElementQuestType<KerbHeight> {

    private val eligibleKerbsFilter by lazy { """
        nodes with
          !kerb
          or kerb ~ yes|unknown
          or kerb !~ no|rolled and kerb older today -8 years
    """.toElementFilterExpression() }

    override val commitMessage = "Add kerb height info"
    override val wikiLink = "Key:kerb"
    override val icon = R.drawable.ic_quest_kerb_type

    override val questTypeAchievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_kerb_height_title

    override fun createForm() = AddKerbHeightForm()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.findAllKerbNodes().filter { eligibleKerbsFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!eligibleKerbsFilter.matches(element) || element !is Node || !element.couldBeAKerb()) false
        else null

    override fun applyAnswerTo(answer: KerbHeight, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("kerb", answer.osmValue)
        changes.addOrModify("barrier", "kerb")
    }
}
