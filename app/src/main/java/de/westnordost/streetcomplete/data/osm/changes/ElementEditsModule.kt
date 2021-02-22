package de.westnordost.streetcomplete.data.osm.changes

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module object ElementEditsModule {

    @Provides @Singleton fun osmElementChangesSource(
        elementEditsController: ElementEditsController
    ): ElementEditsSource = elementEditsController
}
