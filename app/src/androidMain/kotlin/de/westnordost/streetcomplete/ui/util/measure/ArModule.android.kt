package de.westnordost.streetcomplete.ui.util.measure

import androidx.activity.ComponentActivity
import org.koin.androidx.scope.dsl.activityRetainedScope
import org.koin.androidx.scope.dsl.activityScope
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

actual val arPlatformModule = module {
    factory<ArSupportChecker> { AndroidArSupportChecker(get()) }
    activityScope {
        scoped<ArMeasureAppLauncher> { AndroidArMeasureAppLauncher({ get() }) }
    }
}
