package de.westnordost.streetcomplete.quests.level

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.util.math.contains

class AddLevel : AAddLevel() {

    override val isThing: Boolean = false

    /* including any kind of public transport station because even really large bus stations feel
     * like small airport terminals, like Mo Chit 2 in Bangkok*/
    override val mallFilter by lazy { """
        ways, relations with
         shop = mall
         or aeroway = terminal
         or railway = station
         or amenity = bus_station
         or public_transport = station
    """.toElementFilterExpression() }

    /* only nodes because ways/relations are not likely to be floating around freely in a mall
     * outline */
    override val filter by lazy { """
        nodes with
          !level
          and (name or brand or noname = yes or name:signed = no)
    """.toElementFilterExpression() }

    override val changesetComment = "Determine on which level shops are in a building"
    override val icon = R.drawable.ic_quest_level

    override fun getTitle(tags: Map<String, String>) = R.string.quest_level_title2

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element) || !element.isPlace()) return false
        // for shops with no level, we actually need to look at geometry in order to find if it is
        // contained within any multi-level mall
        return null
    }

    override fun createForm() = AddLevelForm()
}
