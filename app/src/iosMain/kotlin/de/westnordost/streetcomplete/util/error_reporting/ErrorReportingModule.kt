package de.westnordost.streetcomplete.util.error_reporting

import org.koin.dsl.module

actual val errorReportingPlatformModule = module {
    single<CrashReportHolder> { EmptyCrashReportHolder }
}
