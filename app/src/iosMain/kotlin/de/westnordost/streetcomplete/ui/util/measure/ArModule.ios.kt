package de.westnordost.streetcomplete.ui.util.measure

import org.koin.dsl.module

actual val arPlatformModule = module {
    factory<ArSupportChecker> { IosArSupportChecker() }
    factory<ArMeasureAppLauncher> { IosArMeasureAppLauncher() }
}
