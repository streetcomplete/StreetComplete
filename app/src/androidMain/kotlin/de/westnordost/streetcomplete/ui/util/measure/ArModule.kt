package de.westnordost.streetcomplete.ui.util.measure

import androidx.activity.ComponentActivity
import org.koin.dsl.module

val arModule = module {
    factory<ArSupportChecker> { AndroidArSupportChecker(get()) }
    factory { ArQuestsDisabler(get(), get()) }

    scope<ComponentActivity> {
        factory<ArMeasureAppLauncher> { ArMeasureAppLauncher(get()) }
    }
}
