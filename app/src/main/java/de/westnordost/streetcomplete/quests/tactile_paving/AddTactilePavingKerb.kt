package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.kerb.couldBeAKerb
import de.westnordost.streetcomplete.osm.kerb.findAllKerbNodes
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddTactilePavingKerb : OsmElementQuestType<Boolean> {

    private val eligibleKerbsFilter by lazy { """
        nodes with
          !tactile_paving
          or tactile_paving = unknown
          or tactile_paving = no and tactile_paving older today -4 years
          or tactile_paving = yes and tactile_paving older today -8 years
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether kerbs have tactile paving"
    override val wikiLink = "Key:tactile_paving"
    override val icon = R.drawable.ic_quest_kerb_tactile_paving
    override val enabledInCountries = COUNTRIES_WHERE_TACTILE_PAVING_IS_COMMON
    override val achievements = listOf(BLIND)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tactile_paving_kerb_title

    override fun createForm() = TactilePavingForm()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.findAllKerbNodes().filter { eligibleKerbsFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!eligibleKerbsFilter.matches(element) || element !is Node || !element.couldBeAKerb()) false
        else null

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("tactile_paving", answer.toYesNo())
        if (tags["kerb"] != "no") {
            tags["barrier"] = "kerb"
        }
    }
}
