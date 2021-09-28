package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BLIND
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.kerb_height.couldBeAKerb
import de.westnordost.streetcomplete.quests.kerb_height.findAllKerbNodes

class AddTactilePavingKerb : OsmElementQuestType<Boolean> {

    private val eligibleKerbsFilter by lazy { """
        nodes with
          !tactile_paving
          or tactile_paving = unknown
          or tactile_paving = no and tactile_paving older today -4 years
          or tactile_paving = yes and tactile_paving older today -8 years
    """.toElementFilterExpression() }

    override val commitMessage = "Add tactile paving on kerbs"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_kerb_tactile_paving
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON

    override val questTypeAchievements = listOf(BLIND)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactile_paving_kerb_title

    override fun createForm() = TactilePavingForm()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.findAllKerbNodes().filter { eligibleKerbsFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!eligibleKerbsFilter.matches(element) || element !is Node || !element.couldBeAKerb()) false
        else null

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("tactile_paving", answer.toYesNo())
        changes.addOrModify("barrier", "kerb")
    }
}
