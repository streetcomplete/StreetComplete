package de.westnordost.streetcomplete.data.meta

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundariesImpl
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val metadataModule = module {
    includes(metadataPlatformModule)

    single { NameSuggestionsSource(get()) }
    single { CountryInfos(get()) }

    single<CountryBoundaries> { CountryBoundariesImpl(get()) }
    single<Lazy<CountryBoundaries>>(named("CountryBoundariesLazy")) {
        lazy { get() }
    }

    single<Lazy<FeatureDictionary>>(named("FeatureDictionaryLazy")) {
        lazy { get<FeatureDictionary>() }
    }
}

expect val metadataPlatformModule: Module
