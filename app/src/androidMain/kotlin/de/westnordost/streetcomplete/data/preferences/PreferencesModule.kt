package de.westnordost.streetcomplete.data.preferences

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {
    single<ObservableSettings> { SharedPreferencesSettings.Factory(androidContext()).create() }
    single { Preferences(get()) }
    single { ResurveyIntervalsUpdater(get()) }
}
