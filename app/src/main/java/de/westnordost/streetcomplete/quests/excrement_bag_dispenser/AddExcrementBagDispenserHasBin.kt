package de.westnordost.streetcomplete.quests.excrement_bag_dispenser

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddExcrementBagDispenserHasBin() : OsmElementQuestType<Boolean> {

    private val nodesFilter by lazy { """
        nodes with
          vending = excrement_bags
          and !bin and amenity != waste_basket
          and access !~ private|no
    """.toElementFilterExpression() }

    private val nearbyBinsFilter by lazy { """
        nodes with
          (bin != no or amenity != waste_basket)
          and access !~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether excrement bag dispensers provide bins"
    override val wikiLink = "Key:bin"
    override val icon = R.drawable.ic_quest_bin_amenity
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_excrement_bag_dispenser_bin_title
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        //TODO: Alles was schon Mülleimer hat, kann weg gefiltert werden. LatLonRaster, da alle Nachbarn rein und dann wegfiltern. Welche Entfernung macht Sinn?
        val nodes = mapData.nodes.filter {
            nodesFilter.matches(it)
        }
        return nodes
    }

    override fun isApplicableTo(element: Element) = if (!nodesFilter.matches(element)) false else null

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> =
        getMapData().filter("nodes with amenity = waste_basket or vending = excrement_bags")

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["bin"] = answer.toYesNo()
    }
}
