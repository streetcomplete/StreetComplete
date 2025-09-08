package de.westnordost.streetcomplete.data.logs

object LogsTable {
    const val NAME = "logs"

    object Columns {
        const val LEVEL = "level"
        const val TAG = "tag"
        const val MESSAGE = "message"
        const val ERROR = "error"
        const val TIMESTAMP = "timestamp"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.LEVEL} text NOT NULL,
            ${Columns.TAG} text NOT NULL,
            ${Columns.MESSAGE} text NOT NULL,
            ${Columns.ERROR} text,
            ${Columns.TIMESTAMP} int NOT NULL
        );
    """

    const val INDEX_CREATE = """
        CREATE INDEX logs_timestamp_index ON $NAME (${Columns.TIMESTAMP});
    """
}
