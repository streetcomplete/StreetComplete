package de.westnordost.streetcomplete.quests.level

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddLevelThing : OsmElementQuestType<String>, AndroidQuest {

    /* only nodes because ways/relations are not likely to be floating around freely in a mall
     * outline */
    private val filter by lazy { "nodes with !level".toElementFilterExpression() }

    /* including any kind of public transport station because even really large bus stations feel
     * like small airport terminals, like Mo Chit 2 in Bangkok*/
    private val mallFilter by lazy { """
        ways, relations with
         shop ~ department_store|mall
         or aeroway = terminal
         or railway = station
         or amenity = bus_station
         or public_transport = station
    """.toElementFilterExpression() }

    override val changesetComment = "Determine on which level things are in a building"
    override val wikiLink = "Key:level"
    override val icon = R.drawable.quest_level_thing
    override val isReplacePlaceEnabled = false
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_level_thing_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // get geometry of all malls in the area
        val mallGeometries = mapData
            .filter { mallFilter.matches(it) }
            .mapNotNull { mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry }
        if (mallGeometries.isEmpty()) return emptyList()

        val multiLevelMallGeometries = getMultiLevelMallGeometries(mallGeometries, mapData)
        if (multiLevelMallGeometries.isEmpty()) return emptyList()

        // now, return all things that have no level tagged and are inside those multi-level malls
        val thingsWithoutLevel = mapData
            .filter(filter)
            .filter { it.isThing() }
            .toMutableList()
        if (thingsWithoutLevel.isEmpty()) return emptyList()

        val result = mutableListOf<Element>()

        for (mallGeometry in multiLevelMallGeometries) {
            val it = thingsWithoutLevel.iterator()
            while (it.hasNext()) {
                val thing = it.next()
                val pos = mapData.getGeometry(thing.type, thing.id)?.center ?: continue
                if (!mallGeometry.bounds.contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                result.add(thing)
                it.remove() // thing can only be in one mall
            }
        }
        return result
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element) || !element.isThing()) return false
        // for things with no level, we actually need to look at geometry in order to find if it is
        // contained within any multi-level mall
        return null
    }

    override fun createForm() = AddLevelThingForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["level"] = answer
    }
}
