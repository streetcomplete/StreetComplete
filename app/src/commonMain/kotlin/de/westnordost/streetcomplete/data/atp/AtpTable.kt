package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.WayTables
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.NAME_NODES

object AtpTable {
    const val NAME = "atp_matches"

    object Columns {
        const val ID = "atp_entry_id"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val OSM_ELEMENT_MATCH_ID = "osm_element_match_id"
        const val OSM_ELEMENT_MATCH_TYPE = "osm_element_match_type"
        const val ATP_TAGS = "atp_tags"
        const val OSM_TAGS = "osm_tags"
        const val LAST_SYNC = "last_sync"
        const val REPORT_TYPE = "report_type"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.OSM_ELEMENT_MATCH_ID} int,
            ${Columns.OSM_ELEMENT_MATCH_TYPE} varchar(15),
            ${Columns.ATP_TAGS} text NOT NULL,
            ${Columns.OSM_TAGS} text NOT NULL,
            ${Columns.LAST_SYNC} int NOT NULL,
            ${Columns.REPORT_TYPE} text NOT NULL
        );
    """

    const val INDEX_CREATE = """
        CREATE INDEX atp_id_index ON $NAME (${Columns.ID});
    """

    const val SPATIAL_INDEX_CREATE = """
        CREATE INDEX atp_spatial_index ON $NAME (
            ${Columns.LATITUDE},
            ${Columns.LONGITUDE}
        );
    """
}
