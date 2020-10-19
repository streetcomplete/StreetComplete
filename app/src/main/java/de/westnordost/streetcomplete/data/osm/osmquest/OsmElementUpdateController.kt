package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.map.getRelationComplete
import de.westnordost.osmapi.map.getWayComplete
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import javax.inject.Inject

/** When an element has been updated or deleted (from the API), this class takes care of updating
 *  the element and the data that is dependent on the element - the quests */
class OsmElementUpdateController @Inject constructor(
    private val mapDataApi: MapDataApi,
    private val elementGeometryCreator: ElementGeometryCreator,
    private val elementDB: MergedElementDao,
    private val questGiver: OsmQuestGiver,
){

    /** The [element] has been updated. Persist that, determine its geometry and update the quests
     *  based on that element. If [recreateQuestTypes] is not null, always (re)create the given
     *  quest types on the element without checking for its eligibility */
    fun update(element: Element, recreateQuestTypes: List<OsmElementQuestType<*>>?) {
        val newGeometry = createGeometry(element)
        if (newGeometry != null) {
            elementDB.put(element)

            if (recreateQuestTypes == null) {
                questGiver.updateQuests(element, newGeometry)
            } else {
                questGiver.recreateQuests(element, newGeometry, recreateQuestTypes)
            }
        } else {
            // new element has invalid geometry
            delete(element.type, element.id)
        }
    }

    fun delete(elementType: Element.Type, elementId: Long) {
        elementDB.delete(elementType, elementId)
        // geometry is deleted by the  osmQuestController
        questGiver.deleteQuests(elementType, elementId)
    }

    fun get(elementType: Element.Type, elementId: Long): Element? {
        return elementDB.get(elementType, elementId)
    }

    fun cleanUp() {
        elementDB.deleteUnreferenced()
    }

    private fun createGeometry(element: Element): ElementGeometry? {
        when(element) {
            is Node -> {
                return elementGeometryCreator.create(element)
            }
            is Way -> {
                val mapData: MapData
                try {
                    mapData = mapDataApi.getWayComplete(element.id)
                } catch (e: OsmNotFoundException) {
                    return null
                }
                return elementGeometryCreator.create(element, mapData)
            }
            is Relation -> {
                val mapData: MapData
                try {
                    mapData = mapDataApi.getRelationComplete(element.id)
                } catch (e: OsmNotFoundException) {
                    return null
                }
                return elementGeometryCreator.create(element, mapData)
            }
            else -> return null
        }
    }
}