package de.westnordost.streetcomplete.data.user.statistics

import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry

@Module
object StatisticsModule {
    private const val STATISTICS_BACKEND_URL = "https://www.westnordost.de/streetcomplete/statistics/"

    @Provides fun statisticsDownloader(parser: StatisticsParser): StatisticsDownloader =
        StatisticsDownloader(STATISTICS_BACKEND_URL, parser)

    @Provides fun statisticsSource(statisticsController: StatisticsController): StatisticsSource =
        statisticsController
}
