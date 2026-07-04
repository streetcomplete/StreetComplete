package de.westnordost.streetcomplete.util.error_reporting

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val errorReportingPlatformModule = module {
    single { CrashReportsUncaughtExceptionHandler(androidContext(), get(), "crashreport.txt") }
    single<CrashReportHolder> { get<CrashReportsUncaughtExceptionHandler>() }
}
