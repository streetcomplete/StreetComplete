package de.westnordost.streetcomplete.data.osmcal

import org.koin.dsl.module

val calendarEventsModule = module {
    factory { OsmCalApiClient(get(), get()) }
    factory { OsmCalParser() }
    factory { OsmCalUpdater(get(), get()) }
    factory { CalendarEventsDao(get()) }
    single { CalendarEventsController(get()) }
    single<CalendarEventsSource> { get<CalendarEventsController>() }
}
