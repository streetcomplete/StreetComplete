package de.westnordost.streetcomplete.screens.measure

import org.koin.dsl.module

val arModule = module {
    factory<ArSupportChecker> { ArSupportCheckerImpl(get()) }
    factory { ArQuestsDisabler(get(), get()) }
}
