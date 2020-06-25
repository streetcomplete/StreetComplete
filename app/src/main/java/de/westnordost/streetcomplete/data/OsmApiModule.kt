package de.westnordost.streetcomplete.data

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.overpass.OverpassMapDataDao
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.user.OAuthStore
import oauth.signpost.OAuthConsumer
import javax.inject.Singleton

@Module
object OsmApiModule {

    const val OSM_API_URL = "https://api.openstreetmap.org/api/0.6/"
    const val OVERPASS_API_URL = "https://lz4.overpass-api.de/api/"

    // see https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#timeout:
    // default value is 180 seconds
    // give additional 4 seconds to get and process refusal from Overpass
    // or maybe a bit late response rather than trigger timeout exception
    private const val OVERPASS_QUERY_TIMEOUT_IN_MILISECONDS = (180 + 4) * 1000

    /** Returns the osm connection singleton used for all daos with the saved oauth consumer  */
    @Provides @Singleton fun osmConnection(oAuthStore: OAuthStore): OsmConnection {
        return osmConnection(oAuthStore.oAuthConsumer)
    }

    /** Returns an osm connection with the supplied consumer (note the difference to the above function)  */
    fun osmConnection(consumer: OAuthConsumer?): OsmConnection {
        return OsmConnection(OSM_API_URL, ApplicationConstants.USER_AGENT, consumer)
    }

    @Provides @Singleton
    fun overpassMapDataDao(prefs: SharedPreferences): OverpassMapDataDao {
        val timeout = OVERPASS_QUERY_TIMEOUT_IN_MILISECONDS
        val overpassConnection = OsmConnection(
            prefs.getString(Prefs.OVERPASS_URL, OVERPASS_API_URL),
            ApplicationConstants.USER_AGENT,
            null,
            timeout
        )
        return OverpassMapDataDao(overpassConnection)
    }

    @Provides fun userDao(osm: OsmConnection): UserApi = UserApi(osm)

    @Provides fun notesDao(osm: OsmConnection): NotesApi = NotesApi(osm)

    @Provides fun mapDataDao(osm: OsmConnection): MapDataApi = MapDataApi(osm)
}