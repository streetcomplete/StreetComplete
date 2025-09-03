package de.westnordost.streetcomplete.quests.level

import de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

abstract class AAddLevel : OsmElementQuestType<String>, AndroidQuest {

    abstract val mallFilter: ElementFilterExpression
    abstract val isThing: Boolean
    abstract val filter: ElementFilterExpression

    private val thingsWithLevelFilter by lazy { """
        nodes, ways, relations with level
    """.toElementFilterExpression() }

    override val wikiLink = "Key:level"
    /* disabled because in a mall with multiple levels, if there are nodes with no level defined,
     * it really makes no sense to tag something as vacant if the level is not known. Instead, if
     * the user cannot find the place on any level in the mall, delete the element completely. */
    override val isReplacePlaceEnabled = false
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // get geometry of all malls in the area
        val mallGeometries = mapData
            .filter { mallFilter.matches(it) }
            .mapNotNull { mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry }
        if (mallGeometries.isEmpty()) return emptyList()

        // get all shops that have level tagged
        val thingsWithLevel = mapData.filter { thingsWithLevelFilter.matches(it) }
        if (thingsWithLevel.isEmpty()) return emptyList()

        // with this, find malls that contain shops that have different levels tagged
        val multiLevelMallGeometries = mallGeometries.filter { mallGeometry ->
            var level: String? = null
            for (shop in thingsWithLevel) {
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.bounds.contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                if (shop.tags.containsKey("level")) {
                    if (level != null) {
                        if (level != shop.tags["level"]) return@filter true
                    } else {
                        level = shop.tags["level"]
                    }
                }
            }
            return@filter false
        }
        if (multiLevelMallGeometries.isEmpty()) return emptyList()

        // now, return all shops that have no level tagged and are inside those multi-level malls
        val shopsWithoutLevel = mapData
            .filter {
                filter.matches(it) &&
                if (isThing) it.isThing()
                else it.isPlace()
            }
            .toMutableList()
        if (shopsWithoutLevel.isEmpty()) return emptyList()

        val result = mutableListOf<Element>()

        for (mallGeometry in multiLevelMallGeometries) {
            val it = shopsWithoutLevel.iterator()
            while (it.hasNext()) {
                val shop = it.next()
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.bounds.contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                result.add(shop)
                it.remove() // shop can only be in one mall
            }
        }
        return result
    }

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["level"] = answer
    }
}
