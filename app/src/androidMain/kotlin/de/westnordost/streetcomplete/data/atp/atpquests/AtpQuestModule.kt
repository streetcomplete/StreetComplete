package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.atpquests.AtpQuestController
import org.koin.dsl.module

val atpQuestModule = module {
    single<AtpQuestSource> { get<AtpQuestController>() }

    single { AtpQuestController(get(), get(), get(), get()) }
}
