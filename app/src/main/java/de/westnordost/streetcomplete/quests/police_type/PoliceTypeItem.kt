package de.westnordost.streetcomplete.quests.police_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.quests.police_type.PoliceType.*

fun PoliceType.asItem() = Item(this, iconResId, titleResId)

private val PoliceType.titleResId: Int get() = when (this) {
    CARABINIERI ->        R.string.quest_policeType_type_it_carabinieri
    POLIZIA_DI_STATO ->   R.string.quest_policeType_type_it_polizia_di_stato
    GUARDIA_DI_FINANZA -> R.string.quest_policeType_type_it_guardia_di_finanza
    POLIZIA_MUNICIPALE -> R.string.quest_policeType_type_it_polizia_municipale
    POLIZIA_LOCALE ->     R.string.quest_policeType_type_it_polizia_locale
    GUARDIA_COSTIERA ->   R.string.quest_policeType_type_it_guardia_costiera
}

private val PoliceType.iconResId: Int get() = when (this) {
    CARABINIERI ->        R.drawable.ic_italian_police_type_carabinieri
    POLIZIA_DI_STATO ->   R.drawable.ic_italian_police_type_polizia
    GUARDIA_DI_FINANZA -> R.drawable.ic_italian_police_type_finanza
    POLIZIA_MUNICIPALE -> R.drawable.ic_italian_police_type_municipale
    POLIZIA_LOCALE ->     R.drawable.ic_italian_police_type_locale
    GUARDIA_COSTIERA ->   R.drawable.ic_italian_police_type_costiera
}
