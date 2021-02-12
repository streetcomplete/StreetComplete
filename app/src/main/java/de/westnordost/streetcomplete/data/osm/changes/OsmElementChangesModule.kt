package de.westnordost.streetcomplete.data.osm.changes

import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import javax.inject.Singleton

@Module object OsmElementChangesModule {

    @Provides @Singleton fun osmElementChangesSource(
        osmElementChangesController: OsmElementChangesController,
        statisticsUpdater: StatisticsUpdater
    ): OsmElementChangesSource =
        osmElementChangesController.apply {
            addListener(statisticsUpdater)
        }
}
