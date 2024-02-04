package de.westnordost.streetcomplete.data

import android.content.SharedPreferences
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.user.UserApi
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiImpl
import de.westnordost.streetcomplete.data.osmnotes.NotesApi
import de.westnordost.streetcomplete.data.osmnotes.NotesApiImpl
import de.westnordost.streetcomplete.data.osmtracks.TracksApi
import de.westnordost.streetcomplete.data.osmtracks.TracksApiImpl
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val OSM_API_URL = "https://api.openstreetmap.org/api/0.6/"

val osmApiModule = module {
    factory { Cleaner(get(), get(), get(), get(), get()) }
    factory { CacheTrimmer(get(), get()) }
    factory<MapDataApi> { MapDataApiImpl(get()) }
    factory<NotesApi> { NotesApiImpl(get()) }
    factory<TracksApi> { TracksApiImpl(get()) }
    factory { Preloader(get(named("CountryBoundariesLazy")), get(named("FeatureDictionaryLazy"))) }
    factory { UserApi(get()) }

    single { OsmConnection(
        OSM_API_URL,
        ApplicationConstants.USER_AGENT,
        get<SharedPreferences>().getString(Prefs.OAUTH2_ACCESS_TOKEN, null)
    ) }
    single { UnsyncedChangesCountSource(get(), get()) }

    worker { CleanerWorker(get(), get(), get()) }
}
