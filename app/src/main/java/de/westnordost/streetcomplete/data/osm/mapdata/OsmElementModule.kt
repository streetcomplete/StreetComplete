package de.westnordost.streetcomplete.data.osm.mapdata

import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestUpdater
import javax.inject.Singleton

@Module object OsmElementModule {
    @Provides fun osmElementSource(osmElementController: OsmElementController): OsmElementSource =
        osmElementController

    @Provides @Singleton
    fun osmElementController(
        elementDB: MergedElementDao,
        wayDB: WayDao,
        nodeDB: NodeDao,
        geometryDB: ElementGeometryDao,
        elementGeometryCreator: ElementGeometryCreator,
        osmQuestUpdater: OsmQuestUpdater
    ) = OsmElementController(elementDB, wayDB, nodeDB, geometryDB, elementGeometryCreator).apply {
        addElementUpdatesListener(osmQuestUpdater)
    }
}
