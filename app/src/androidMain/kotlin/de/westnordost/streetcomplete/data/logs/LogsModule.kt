package de.westnordost.streetcomplete.data.logs

import org.koin.dsl.module

val logsModule = module {
    factory { LogsDao(get()) }

    single { LogsController(get()) }
}
