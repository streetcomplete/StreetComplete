package de.westnordost.streetcomplete.data.osm.edits

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module object ElementEditsModule {

    @Provides @Singleton fun osmElementChangesSource(
        elementEditsController: ElementEditsController
    ): ElementEditsSource = elementEditsController
}
