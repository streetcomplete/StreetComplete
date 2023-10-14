package de.westnordost.streetcomplete.data.logs

object LogsTable {
    const val NAME = "logs"

    object Columns {
        const val ID = "id"
        const val LEVEL = "level"
        const val TAG = "tag"
        const val MESSAGE = "message"
        const val ERROR = "error"
        const val TIMESTAMP = "timestamp"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.LEVEL} int NOT NULL,
            ${Columns.TAG} varchar(255) NOT NULL,
            ${Columns.MESSAGE} text NOT NULL,
            ${Columns.ERROR} text,
            ${Columns.TIMESTAMP} int NOT NULL
        );
    """
}
