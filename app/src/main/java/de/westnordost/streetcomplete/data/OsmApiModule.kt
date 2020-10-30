package de.westnordost.streetcomplete.data

import dagger.Module
import dagger.Provides
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.map.LightweightOsmMapDataFactory
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.user.OAuthStore
import oauth.signpost.OAuthConsumer
import javax.inject.Singleton

@Module
object OsmApiModule {

    private const val OSM_API_URL = "https://api.openstreetmap.org/api/0.6/"

    /** Returns the osm connection singleton used for all daos with the saved oauth consumer  */
    @Provides @Singleton fun osmConnection(oAuthStore: OAuthStore): OsmConnection {
        return osmConnection(oAuthStore.oAuthConsumer)
    }

    /** Returns an osm connection with the supplied consumer (note the difference to the above function)  */
    fun osmConnection(consumer: OAuthConsumer?): OsmConnection {
        return OsmConnection(OSM_API_URL, ApplicationConstants.USER_AGENT, consumer)
    }

    @Provides fun userDao(osm: OsmConnection): UserApi = UserApi(osm)

    @Provides fun notesDao(osm: OsmConnection): NotesApi = NotesApi(osm)

    @Provides fun mapDataDao(osm: OsmConnection): MapDataApi {
        // generally we are not interested in certain data returned by the OSM API, so we use a
        // map data factory that does not include that data
        return MapDataApi(osm, LightweightOsmMapDataFactory())
    }
}