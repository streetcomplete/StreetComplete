package de.westnordost.streetcomplete.quests.level

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isShopExpressionFragment
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddLevel : OsmElementQuestType<String> {

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

    /* only nodes because ways/relations are not likely to be floating around freely in a mall
    *  outline */
    private val filter by lazy { """
        nodes with
         (${isShopExpressionFragment()})
         and !level
         and (name or brand or noname = yes or name:signed = no)
    """.toElementFilterExpression() }

    override val changesetComment = "Determine on which level shops are in a building"
    override val wikiLink = "Key:level"
    override val icon = R.drawable.ic_quest_level
    /* disabled because in a mall with multiple levels, if there are nodes with no level defined,
    *  it really makes no sense to tag something as vacant if the level is not known. Instead, if
    *  the user cannot find the place on any level in the mall, delete the element completely. */
    override val isReplaceShopEnabled = false
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_level_title2

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
                if (!mallGeometry.getBounds().contains(pos)) continue
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
            .filter { filter.matches(it) }
            .toMutableList()
        if (shopsWithoutLevel.isEmpty()) return emptyList()

        val result = mutableListOf<Element>()

        for (mallGeometry in multiLevelMallGeometries) {
            val it = shopsWithoutLevel.iterator()
            while (it.hasNext()) {
                val shop = it.next()
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.getBounds().contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                result.add(shop)
                it.remove() // shop can only be in one mall
            }
        }
        return result
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        // for shops with no level, we actually need to look at geometry in order to find if it is
        // contained within any multi-level mall
        return null
    }

    override fun createForm() = AddLevelForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["level"] = answer
    }
}
