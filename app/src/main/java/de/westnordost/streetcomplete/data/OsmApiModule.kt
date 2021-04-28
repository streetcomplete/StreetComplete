package de.westnordost.streetcomplete.data

import dagger.Module
import dagger.Provides
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.user.UserApi
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApiImpl
import de.westnordost.streetcomplete.data.osmnotes.NotesApi
import de.westnordost.streetcomplete.data.osmnotes.NotesApiImpl
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

    @Provides fun userApi(osm: OsmConnection): UserApi = UserApi(osm)

    @Provides fun notesApi(osm: OsmConnection): NotesApi = NotesApiImpl(osm)

    @Provides fun mapDataApi(osm: OsmConnection): MapDataApi = MapDataApiImpl(osm)
}
