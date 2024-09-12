package de.westnordost.streetcomplete.quests.valves

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.valves.Valves.*
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun Valves.asItem() = Item(this, iconResId, titleResId)

private val Valves.titleResId: Int get() = when (this) {
    SCHRADER ->     R.string.quest_valves_schrader
    SCLAVERAND ->   R.string.quest_valves_sclaverand
    DUNLOP ->       R.string.quest_valves_dunlop
    REGINA ->       R.string.quest_valves_regina
}

private val Valves.iconResId: Int get() = when (this) {
    SCHRADER ->     R.drawable.valves_schrader
    SCLAVERAND ->   R.drawable.valves_presta
    DUNLOP ->       R.drawable.valves_dunlop
    REGINA ->       R.drawable.valves_regina
}
