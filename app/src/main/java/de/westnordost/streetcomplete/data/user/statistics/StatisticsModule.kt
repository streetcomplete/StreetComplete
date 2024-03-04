package de.westnordost.streetcomplete.data.user.statistics

import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val STATISTICS_BACKEND_URL = "https://www.westnordost.de/streetcomplete/statistics/"
val statisticsModule = module {

    factory(named("EditTypeStatistics")) { EditTypeStatisticsDao(get(), EditTypeStatisticsTables.NAME) }
    factory(named("CountryStatistics")) { CountryStatisticsDao(get(), CountryStatisticsTables.NAME) }

    factory(named("EditTypeStatisticsCurrentWeek")) { EditTypeStatisticsDao(get(), EditTypeStatisticsTables.NAME_CURRENT_WEEK) }
    factory(named("CountryStatisticsCurrentWeek")) { CountryStatisticsDao(get(), CountryStatisticsTables.NAME_CURRENT_WEEK) }

    factory { ActiveDatesDao(get()) }

    factory { StatisticsDownloader(get(), STATISTICS_BACKEND_URL, get()) }
    factory { StatisticsParser(get(named("TypeAliases"))) }

    single<StatisticsSource> { get<StatisticsController>() }
    single { StatisticsController(
        editTypeStatisticsDao = get(named("EditTypeStatistics")),
        countryStatisticsDao = get(named("CountryStatistics")),
        currentWeekEditTypeStatisticsDao = get(named("EditTypeStatisticsCurrentWeek")),
        currentWeekCountryStatisticsDao = get(named("CountryStatisticsCurrentWeek")),
        activeDatesDao = get(),
        countryBoundaries = get(named("CountryBoundariesLazy")),
        prefs = get(),
        userLoginStatusSource = get()
    ) }
}
