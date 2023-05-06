package de.westnordost.streetcomplete.data

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.user.UserApi
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiImpl
import de.westnordost.streetcomplete.data.osmnotes.NotesApi
import de.westnordost.streetcomplete.data.osmnotes.NotesApiImpl
import de.westnordost.streetcomplete.data.osmtracks.TracksApi
import de.westnordost.streetcomplete.data.osmtracks.TracksApiImpl
import de.westnordost.streetcomplete.data.user.OAuthStore
import oauth.signpost.OAuthConsumer
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

val osmApiModule = module {
    factory { Cleaner(get(), get(), get(), get()) }
    factory { CacheTrimmer(get(), get()) }
    factory<MapDataApi> { MapDataApiImpl(get()) }
    factory<NotesApi> { NotesApiImpl(get()) }
    factory<TracksApi> { TracksApiImpl(get()) }
    factory { Preloader(get(named("CountryBoundariesFuture")), get(named("FeatureDictionaryFuture"))) }
    factory { UserApi(get()) }

    single { osmConnection(get<OAuthStore>().oAuthConsumer) }
    single { UnsyncedChangesCountSource(get(), get()) }

    worker { CleanerWorker(get(), get(), get()) }
}

private const val OSM_API_URL = "https://api.openstreetmap.org/api/0.6/"
/** Returns an osm connection with the supplied consumer (i.e. not the one from the OAuthStore) */
fun osmConnection(consumer: OAuthConsumer?): OsmConnection {
    return OsmConnection(OSM_API_URL, ApplicationConstants.USER_AGENT, consumer)
}
