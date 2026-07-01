package de.westnordost.streetcomplete.ui.util.measure

import org.koin.core.module.Module
import org.koin.dsl.module

val arModule = module {
    factory { ArQuestsDisabler(get(), get()) }
    includes(arPlatformModule)
}

expect val arPlatformModule: Module
