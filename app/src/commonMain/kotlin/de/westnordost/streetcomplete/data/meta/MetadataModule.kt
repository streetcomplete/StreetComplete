package de.westnordost.streetcomplete.data.meta

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmfeatures.FeatureDictionary
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val metadataModule = module {
    includes(metadataPlatformModule)

    single { NameSuggestionsSource(get()) }
    single { CountryInfos(get()) }

    single<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")) {
        lazy { get<CountryBoundaries>() }
    }

    single<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy")) {
        lazy { get<FeatureDictionary>() }
    }
}

expect val metadataPlatformModule: Module
