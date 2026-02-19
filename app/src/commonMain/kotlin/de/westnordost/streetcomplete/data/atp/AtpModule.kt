package de.westnordost.streetcomplete.data.atp

import org.koin.dsl.module

const val OSM_ATP_COMPARISON_API_BASE_URL = "https://bbox-filter-for-atp.bulwersator-cloudflare.workers.dev/api/"

val atpModule = module {
    factory { AtpDao(get()) }
    factory { AtpDownloader(get(), get()) }
    // TODO API: connect to actual bidirectional API
    factory { AtpApiClient(get(), OSM_ATP_COMPARISON_API_BASE_URL, get()) }
    factory { AtpApiParser() }

    single { AtpController(get()) }
}
