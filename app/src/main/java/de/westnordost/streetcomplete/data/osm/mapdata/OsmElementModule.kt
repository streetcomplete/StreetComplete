package de.westnordost.streetcomplete.data.osm.mapdata

import dagger.Module
import dagger.Provides

@Module object OsmElementModule {
    @Provides fun osmElementSource(osmElementController: OsmElementController): OsmElementSource =
        osmElementController
}
