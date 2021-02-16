package de.westnordost.streetcomplete.data.osm.mapdata

import dagger.Module
import dagger.Provides

@Module object ElementModule {
    @Provides fun elementSource(mapDataController: MapDataController): MapDataSource =
        mapDataController
}
