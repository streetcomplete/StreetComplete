package de.westnordost.streetcomplete

import android.content.SharedPreferences
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import de.westnordost.streetcomplete.util.SoundFx
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    factory<AssetManager> { androidContext().assets }
    factory<Resources> { androidContext().resources }
    factory<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    single { CrashReportExceptionHandler(androidContext(), "streetcomplete_errors@westnordost.de", "crashreport.txt") }
    single { SoundFx(androidContext()) }
}
