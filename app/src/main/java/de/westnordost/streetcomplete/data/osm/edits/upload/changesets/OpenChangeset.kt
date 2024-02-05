package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/** A row in the OpenChangeset table  */
data class OpenChangeset(
    val questType: String,
    val source: String,
    val changesetId: Long,
    val lastPosition: LatLon,
)
