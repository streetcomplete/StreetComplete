package de.westnordost.streetcomplete.screens.main

import de.westnordost.streetcomplete.data.location.RecentLocationStore
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import org.koin.dsl.module

val mainModule = module {
    single { LocationAvailabilityReceiver(get()) }
    single { RecentLocationStore() }
}
