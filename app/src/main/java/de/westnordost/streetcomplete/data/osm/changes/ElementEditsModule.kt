package de.westnordost.streetcomplete.data.osm.changes

import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import javax.inject.Singleton

@Module object ElementEditsModule {

    @Provides @Singleton fun osmElementChangesSource(
        elementEditsController: ElementEditsController,
        statisticsUpdater: StatisticsUpdater
    ): ElementEditsSource =
        elementEditsController.apply {
            addListener(statisticsUpdater)
        }
}
