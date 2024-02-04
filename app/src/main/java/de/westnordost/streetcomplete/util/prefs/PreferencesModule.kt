package de.westnordost.streetcomplete.util.prefs

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {
    factory<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    single<Preferences> { AndroidPreferences(get()) }
}
