package de.westnordost.streetcomplete.ui.util.measure

import androidx.activity.ComponentActivity
import org.koin.dsl.module

actual val arPlatformModule = module {
    factory<ArSupportChecker> { AndroidArSupportChecker(get()) }
    scope<ComponentActivity> {
        factory<ArMeasureAppLauncher> { AndroidArMeasureAppLauncher(get()) }
    }
}
