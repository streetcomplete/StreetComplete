package de.westnordost.streetcomplete.quests.police_type

import de.westnordost.streetcomplete.quests.police_type.PoliceType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.italian_police_type_carabinieri
import de.westnordost.streetcomplete.resources.italian_police_type_costiera
import de.westnordost.streetcomplete.resources.italian_police_type_finanza
import de.westnordost.streetcomplete.resources.italian_police_type_locale
import de.westnordost.streetcomplete.resources.italian_police_type_municipale
import de.westnordost.streetcomplete.resources.italian_police_type_polizia
import de.westnordost.streetcomplete.resources.quest_policeType_type_it_carabinieri
import de.westnordost.streetcomplete.resources.quest_policeType_type_it_guardia_costiera
import de.westnordost.streetcomplete.resources.quest_policeType_type_it_guardia_di_finanza
import de.westnordost.streetcomplete.resources.quest_policeType_type_it_polizia_di_stato
import de.westnordost.streetcomplete.resources.quest_policeType_type_it_polizia_locale
import de.westnordost.streetcomplete.resources.quest_policeType_type_it_polizia_municipale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val PoliceType.title: StringResource get() = when (this) {
    CARABINIERI ->        Res.string.quest_policeType_type_it_carabinieri
    POLIZIA_DI_STATO ->   Res.string.quest_policeType_type_it_polizia_di_stato
    GUARDIA_DI_FINANZA -> Res.string.quest_policeType_type_it_guardia_di_finanza
    POLIZIA_MUNICIPALE -> Res.string.quest_policeType_type_it_polizia_municipale
    POLIZIA_LOCALE ->     Res.string.quest_policeType_type_it_polizia_locale
    GUARDIA_COSTIERA ->   Res.string.quest_policeType_type_it_guardia_costiera
}

val PoliceType.icon: DrawableResource get() = when (this) {
    CARABINIERI ->        Res.drawable.italian_police_type_carabinieri
    POLIZIA_DI_STATO ->   Res.drawable.italian_police_type_polizia
    GUARDIA_DI_FINANZA -> Res.drawable.italian_police_type_finanza
    POLIZIA_MUNICIPALE -> Res.drawable.italian_police_type_municipale
    POLIZIA_LOCALE ->     Res.drawable.italian_police_type_locale
    GUARDIA_COSTIERA ->   Res.drawable.italian_police_type_costiera
}
