package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.screens.about.LogsController
import org.koin.dsl.module

val logsModule = module {
    factory { LogsDao(get()) }

    single { LogsController(get()) }
}
