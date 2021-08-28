package de.westnordost.streetcomplete.data.osm.created_elements

import dagger.Module
import dagger.Provides

@Module
object CreatedElementsModule {
    @Provides fun createdElementsSource(
        createdElementsController: CreatedElementsController
    ): CreatedElementsSource = createdElementsController
}
