package de.westnordost.streetcomplete.data.osm.edits

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module object ElementEditsModule {

    @Provides @Singleton fun elementEditsSource(
        elementEditsController: ElementEditsController
    ): ElementEditsSource = elementEditsController
}
