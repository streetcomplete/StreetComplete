package de.westnordost.streetcomplete

import android.content.res.AssetManager
import android.content.res.Resources
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import de.westnordost.streetcomplete.util.SoundFx
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    factory<AssetManager> { androidContext().assets }
    factory<Resources> { androidContext().resources }

    single { CrashReportExceptionHandler(androidContext(), get(), "streetcomplete_errors@westnordost.de", "crashreport.txt") }
    single { DatabaseLogger(get()) }
    single { SoundFx(androidContext()) }
}
