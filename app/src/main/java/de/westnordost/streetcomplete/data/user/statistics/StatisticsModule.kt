package de.westnordost.streetcomplete.data.user.statistics

import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val STATISTICS_BACKEND_URL = "https://www.westnordost.de/streetcomplete/statistics/"
val statisticsModule = module {
    factory { CountryStatisticsDao(get()) }
    factory { QuestTypeStatisticsDao(get()) }
    factory { StatisticsDownloader(STATISTICS_BACKEND_URL, get()) }
    factory { StatisticsParser(get(), get(named("QuestAliases"))) }

    single<StatisticsSource> { get<StatisticsController>() }
    single { StatisticsController(get(), get(), get(named("CountryBoundariesFuture")), get(), get(), get()) }
}
