package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.LIFESAVER

class AddFireHydrantDiameter : OsmFilterQuestType<FireHydrantDiameterAnswer>() {

    override val elementFilter = """
        nodes with
         emergency = fire_hydrant
         and fire_hydrant:type and
         (fire_hydrant:type = pillar or fire_hydrant:type = underground)
         and !fire_hydrant:diameter
         and fire_hydrant:diameter:signed != no
    """
    override val changesetComment = "Add fire hydrant diameter"
    override val wikiLink = "Tag:emergency=fire_hydrant"
    override val icon = R.drawable.ic_quest_fire_hydrant_diameter
    override val isDeleteElementEnabled = true
    override val questTypeAchievements = listOf(LIFESAVER)

    override fun isEnabled(countryInfo: CountryInfo) = countryInfo.hasFireHydrantDiameterSign

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fireHydrant_diameter_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with emergency = fire_hydrant")

    override fun createForm() = AddFireHydrantDiameterForm()

    override fun applyAnswerTo(answer: FireHydrantDiameterAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is FireHydrantDiameter ->       tags["fire_hydrant:diameter"] = answer.toOsmValue()
            is NoFireHydrantDiameterSign -> tags["fire_hydrant:diameter:signed"] = "no"
        }
    }
}
