package de.westnordost.streetcomplete.data.osm.upload.changesets

/** A row in the OpenChangeset table  */
data class OpenChangeset(val questType: String, val source: String, val changesetId: Long)
