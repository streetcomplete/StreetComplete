package de.westnordost.streetcomplete.data.osm.created_elements

import org.koin.dsl.module

val createdElementsModule = module {
    factory { CreatedElementsDao(get()) }
    factory<CreatedElementsSource> { get<CreatedElementsController>() }

    single { CreatedElementsController(get()) }
}
