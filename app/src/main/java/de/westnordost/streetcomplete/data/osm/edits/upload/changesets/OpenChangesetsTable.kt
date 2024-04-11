package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

object OpenChangesetsTable {
    const val NAME = "open_changesets"

    object Columns {
        const val QUEST_TYPE = "quest_type"
        const val SOURCE = "source"
        const val CHANGESET_ID = "changeset_id"
        const val LAST_POSITION_LATITUDE = "latitude"
        const val LAST_POSITION_LONGITUDE = "longitude"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_TYPE} varchar(255),
            ${Columns.SOURCE} varchar(255),
            ${Columns.CHANGESET_ID} int NOT NULL,
            ${Columns.LAST_POSITION_LATITUDE} double NOT NULL,
            ${Columns.LAST_POSITION_LONGITUDE} double NOT NULL,
            CONSTRAINT primary_key PRIMARY KEY (${Columns.QUEST_TYPE}, ${Columns.SOURCE})
        );
    """
}
