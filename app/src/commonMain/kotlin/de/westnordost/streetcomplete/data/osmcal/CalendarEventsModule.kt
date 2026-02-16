package de.westnordost.streetcomplete.data.osmcal

import org.koin.dsl.module

val calendarEventsModule = module {
    factory { CalendarEventsDao(get()) }
    factory { CalendarEventParser() }
    single { CalendarEventsController(get()) }
    single<CalendarEventsSource> { get<CalendarEventsController>() }
}
