package de.westnordost.streetcomplete.data

import com.russhwolf.settings.ObservableSettings
import de.westnordost.osmapi.OsmConnection
import de.westnordost.streetcomplete.data.user.UserApiClient
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClient
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiClientImpl
import de.westnordost.streetcomplete.data.osmnotes.NotesApiClient
import de.westnordost.streetcomplete.data.osmnotes.NotesApiParser
import de.westnordost.streetcomplete.data.osmtracks.TracksApiClient
import de.westnordost.streetcomplete.data.osmtracks.TracksSerializer
import de.westnordost.streetcomplete.data.user.UserApiParser
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val OSM_API_URL = "https://api.openstreetmap.org/api/0.6/"

val osmApiModule = module {
    factory { Cleaner(get(), get(), get(), get(), get(), get()) }
    factory { CacheTrimmer(get(), get()) }
    factory<MapDataApiClient> { MapDataApiClientImpl(get()) }
    factory { NotesApiClient(get(), OSM_API_URL, get(), get()) }
    factory { TracksApiClient(get(), OSM_API_URL, get(), get()) }
    factory { Preloader(get(named("CountryBoundariesLazy")), get(named("FeatureDictionaryLazy"))) }
    factory { UserApiClient(get(), OSM_API_URL, get(), get()) }

    factory { UserApiParser() }
    factory { NotesApiParser() }
    factory { TracksSerializer() }

    single { OsmConnection(
        OSM_API_URL,
        ApplicationConstants.USER_AGENT,
        get<ObservableSettings>().getStringOrNull(Prefs.OAUTH2_ACCESS_TOKEN)
    ) }
    single { UnsyncedChangesCountSource(get(), get()) }

    worker { CleanerWorker(get(), get(), get()) }
}
