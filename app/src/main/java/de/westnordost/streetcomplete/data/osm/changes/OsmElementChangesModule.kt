package de.westnordost.streetcomplete.data.osm.changes

import dagger.Module
import dagger.Provides

@Module object OsmElementChangesModule {
    @Provides fun osmElementchangesSource(osmElementChangesController: OsmElementChangesController): OsmElementChangesSource =
        osmElementChangesController
}
