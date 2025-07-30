package de.westnordost.streetcomplete.data.presets

object EditTypePresetsTable {
    const val NAME = "quest_presets"

    object Columns {
        const val EDIT_TYPE_PRESET_ID = "quest_preset_id"
        const val EDIT_TYPE_PRESET_NAME = "quest_preset_name"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.EDIT_TYPE_PRESET_ID} INTEGER PRIMARY KEY,
            ${Columns.EDIT_TYPE_PRESET_NAME} TEXT NOT NULL
        );
    """
}
