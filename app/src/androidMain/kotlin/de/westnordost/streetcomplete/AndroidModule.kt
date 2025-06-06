package de.westnordost.streetcomplete

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import de.westnordost.streetcomplete.data.connection.InternetConnectionState
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    factory<InternetConnectionState> { InternetConnectionState(get()) }

    single<ObservableSettings> { SharedPreferencesSettings.Factory(androidContext()).create() }
}
