package de.westnordost.streetcomplete.quests.level

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddLevelThing : OsmElementQuestType<String>, AndroidQuest {

    /* including any kind of public transport station because even really large bus stations feel
     * like small airport terminals, like Mo Chit 2 in Bangkok*/
    private val mallFilter by lazy { """
        ways, relations with
         shop = mall
         or aeroway = terminal
         or railway = station
         or amenity = bus_station
         or public_transport = station
    """.toElementFilterExpression() }

    private val thingsWithLevelFilter by lazy { """
        nodes, ways, relations with level
    """.toElementFilterExpression() }

    override val changesetComment = "Determine on which level things are in a building"
    override val wikiLink = "Key:level"
    override val icon = R.drawable.ic_quest_level_thing
    /* disabled because in a mall with multiple levels, if there are nodes with no level defined,
     * it really makes no sense to tag something as vacant if the level is not known. Instead, if
     * the user cannot find the thing on any level in the mall, delete the element completely. */
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

        // get all elements that have level tagged
        val thingsWithLevel = mapData.filter { thingsWithLevelFilter.matches(it) }
        if (thingsWithLevel.isEmpty()) return emptyList()

        // with this, find malls that contain elements that have different levels tagged
        val multiLevelMallGeometries = mallGeometries.filter { mallGeometry ->
            var level: String? = null
            for (element in thingsWithLevel) {
                val pos = mapData.getGeometry(element.type, element.id)?.center ?: continue
                if (!mallGeometry.bounds.contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                if (element.tags.containsKey("level")) {
                    if (level != null) {
                        if (level != element.tags["level"]) return@filter true
                    } else {
                        level = element.tags["level"]
                    }
                }
            }
            return@filter false
        }
        if (multiLevelMallGeometries.isEmpty()) return emptyList()

        // now, return all things that have no level tagged and are inside those multi-level malls
        val elementsWithoutLevel = mapData
            .filter { it.isThing() }
            .toMutableList()
        if (elementsWithoutLevel.isEmpty()) return emptyList()

        val result = mutableListOf<Element>()

        for (mallGeometry in multiLevelMallGeometries) {
            val it = elementsWithoutLevel.iterator()
            while (it.hasNext()) {
                val element = it.next()
                val pos = mapData.getGeometry(element.type, element.id)?.center ?: continue
                if (!mallGeometry.bounds.contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                result.add(element)
                it.remove() // thing can only be in one mall
            }
        }
        return result
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!element.isThing()) return false
        // for things with no level, we actually need to look at geometry in order to find if it is
        // contained within any multi-level mall
        return null
    }

    override fun createForm() = AddLevelThingForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["level"] = answer
    }
}
