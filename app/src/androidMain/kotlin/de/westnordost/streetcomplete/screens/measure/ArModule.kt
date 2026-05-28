package de.westnordost.streetcomplete.screens.measure

import org.koin.dsl.module

val arModule = module {
    factory<ArSupportChecker> { AndroidArSupportChecker(get()) }
    factory { ArQuestsDisabler(get(), get()) }
}
