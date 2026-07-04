package de.westnordost.streetcomplete.util.error_reporting

import org.koin.core.module.Module
import org.koin.dsl.module

val errorReportingModule = module {
    factory { ErrorReportBuilder(get()) }

    includes(errorReportingPlatformModule)
}

expect val errorReportingPlatformModule: Module
