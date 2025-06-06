package de.westnordost.streetcomplete.data.preferences

import org.koin.dsl.module

val preferencesModule = module {
    single { Preferences(get()) }
    single { ResurveyIntervalsUpdater(get()) }
}
