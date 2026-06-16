package de.westnordost.streetcomplete.quests.police_type

import de.westnordost.streetcomplete.quests.police_type.PoliceType.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val PoliceType.title: String get() = when (this) {
    CARABINIERI ->        "Carabinieri"
    POLIZIA_DI_STATO ->   "Polizia di Stato"
    GUARDIA_DI_FINANZA -> "Guardia di Finanza"
    POLIZIA_MUNICIPALE -> "Polizia Municipale"
    POLIZIA_LOCALE ->     "Polizia Locale"
    GUARDIA_COSTIERA ->   "Guardia Costiera"
}

val PoliceType.icon: DrawableResource get() = when (this) {
    CARABINIERI ->        Res.drawable.italian_police_type_carabinieri
    POLIZIA_DI_STATO ->   Res.drawable.italian_police_type_polizia
    GUARDIA_DI_FINANZA -> Res.drawable.italian_police_type_finanza
    POLIZIA_MUNICIPALE -> Res.drawable.italian_police_type_municipale
    POLIZIA_LOCALE ->     Res.drawable.italian_police_type_locale
    GUARDIA_COSTIERA ->   Res.drawable.italian_police_type_costiera
}
