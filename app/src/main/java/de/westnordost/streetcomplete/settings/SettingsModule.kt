package de.westnordost.streetcomplete.settings

import org.koin.dsl.module

val settingsModule = module {
    single { ResurveyIntervalsUpdater(get()) }
}
