package de.westnordost.streetcomplete.data.atp.atpquests

import org.koin.dsl.module

val atpQuestModule = module {
    factory { AtpQuestsHiddenDao(get()) }

    single<AtpQuestSource> { get<AtpQuestController>() }

    single { AtpQuestController(get(), get(), get(), get()) }
}
