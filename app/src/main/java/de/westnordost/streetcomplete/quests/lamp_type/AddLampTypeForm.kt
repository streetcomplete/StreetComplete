package de.westnordost.streetcomplete.quests.lamp_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddLampTypeForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("led", R.string.quest_lampType_led),
        TextItem("high_pressure_sodium", R.string.quest_lampType_highPressureSodium),
        TextItem("low_pressure_sodium", R.string.quest_lampType_lowPressureSodium),
        TextItem("gaslight", R.string.quest_lampType_gaslight),
        TextItem("fluorescent", R.string.quest_lampType_fluorescent),
        TextItem("incandescent", R.string.quest_lampType_incandescent),
        TextItem("metal-halide", R.string.quest_lampType_metalHalide),
        TextItem("mercury", R.string.quest_lampType_mercury),
        TextItem("halogen", R.string.quest_lampType_halogen),
    )
}
