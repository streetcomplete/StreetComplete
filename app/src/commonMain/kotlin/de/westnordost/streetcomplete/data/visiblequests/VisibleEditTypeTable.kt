package de.westnordost.streetcomplete.data.visiblequests

object VisibleEditTypeTable {
    const val NAME = "quest_visibility"

    object Columns {
        const val EDIT_TYPE_PRESET_ID = "quest_preset_id"
        const val EDIT_TYPE = "quest_type"
        const val VISIBILITY = "visibility"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.EDIT_TYPE_PRESET_ID} INTEGER NOT NULL,
            ${Columns.EDIT_TYPE} TEXT,
            ${Columns.VISIBILITY} INTEGER NOT NULL,
            CONSTRAINT primary_key PRIMARY KEY (${Columns.EDIT_TYPE_PRESET_ID}, ${Columns.EDIT_TYPE})
        );
    """
}
