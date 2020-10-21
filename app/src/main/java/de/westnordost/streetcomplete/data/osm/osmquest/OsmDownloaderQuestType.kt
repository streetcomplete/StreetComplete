package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry

/** Quest type based on OSM data which downloads the necessary data to create quests itself */
interface OsmDownloaderQuestType<T> : OsmElementQuestType<T> {

    /** Downloads map data for this quest type for the given [bbox] and puts the received data into
     *  the [handler]. Returns whether the download was successful */
    fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean
}