package de.westnordost.streetcomplete.quests.railway_electrification

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

class AddRailwayElectrificationSystem : OsmElementQuestType<RailwayElectrificationSystem> {

    private val tracksFilter by lazy { """
        ways with
            railway ~ light_rail|narrow_gauge|preserved|rail|subway|tram
            and !electrified
    """.toElementFilterExpression() }

    override val commitMessage = "Add electrification system for railway track."
    override val wikiLink = "Key:electrified"
    //TODO: Create and use proper quest icon.
    override val icon = R.drawable.ic_quest_railway

    override val isSplitWayEnabled: Boolean = true

    override val questTypeAchievements: List<QuestTypeAchievement> = listOf()

    override fun getTitle(tags: Map<String, String>): Int =
        R.string.quest_railway_electrification_system_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.ways.filter{ isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean = tracksFilter.matches(element)

    override fun createForm(): AbstractQuestAnswerFragment<RailwayElectrificationSystem> =
        AddRailwayElectrificationSystemForm()

    override fun applyAnswerTo(
        answer: RailwayElectrificationSystem,
        changes: StringMapChangesBuilder
    ) {
        if (!answer.osmValue.isNullOrEmpty())
            changes.add("electrified", answer.osmValue)
    }
}
