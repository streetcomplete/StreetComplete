package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.ApplicationConstants.USE_TEST_API
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetApiClient
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetApiSerializer
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiParser
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiSerializer
import de.westnordost.streetcomplete.data.osmnotes.NotesApiClient
import de.westnordost.streetcomplete.data.osmnotes.NotesApiParser
import de.westnordost.streetcomplete.data.osmtracks.TracksApiClient
import de.westnordost.streetcomplete.data.osmtracks.TracksSerializer
import de.westnordost.streetcomplete.data.user.UserApiClient
import de.westnordost.streetcomplete.data.user.UserApiParser
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val OSM_API_URL_LIVE = "https://api.openstreetmap.org/api/0.6/"
private const val OSM_API_URL_TEST = "https://master.apis.dev.openstreetmap.org/api/0.6/"

val OSM_API_URL =
    if (USE_TEST_API) OSM_API_URL_TEST else OSM_API_URL_LIVE

val osmApiModule = module {
    factory { Cleaner(get(), get(), get(), get(), get(), get()) }
    factory { CacheTrimmer(get(), get()) }
    factory { MapDataApiClient(get(), OSM_API_URL, get(), get(), get()) }
    factory { NotesApiClient(get(), OSM_API_URL, get(), get()) }
    factory { TracksApiClient(get(), OSM_API_URL, get(), get()) }
    factory { UserApiClient(get(), OSM_API_URL, get(), get()) }
    factory { ChangesetApiClient(get(), OSM_API_URL, get(), get()) }

    factory { Preloader(get(named("CountryBoundariesLazy")), get(named("FeatureDictionaryLazy"))) }

    factory { UserApiParser() }
    factory { NotesApiParser() }
    factory { TracksSerializer() }
    factory { MapDataApiParser() }
    factory { MapDataApiSerializer() }
    factory { ChangesetApiSerializer() }

    single { UnsyncedChangesCountSource(get(), get()) }
}
