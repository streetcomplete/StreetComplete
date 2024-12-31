package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.ApplicationConstants.USE_TEST_API
import de.westnordost.streetcomplete.data.user.oauth.OAuthApiClient
import org.koin.dsl.module

var OAUTH2_TOKEN_URL = "https://www.openstreetmap.org/oauth2/token"
var OAUTH2_AUTHORIZATION_URL = "https://www.openstreetmap.org/oauth2/authorize"

var OAUTH2_CLIENT_ID = "Yyk4PmTopczrr3BWZYvLK_M-KBloCQwXgPGEzqUYTc8"

const val OAUTH2_CALLBACK_SCHEME = "streetcomplete"
const val OAUTH2_CALLBACK_HOST = "oauth"

val OAUTH2_REDIRECT_URI = "$OAUTH2_CALLBACK_SCHEME://$OAUTH2_CALLBACK_HOST"

val OAUTH2_REQUESTED_SCOPES = listOf(
    "read_prefs",
    "write_api",
    "write_notes",
    "write_gpx",
)

val OAUTH2_REQUIRED_SCOPES = listOf(
    "read_prefs",
    "write_api",
    "write_notes",
    /* the gps traces permissions is only required for "attaching" gpx track recordings
       to notes. People that feel uneasy to give these permission should still be able to
       use this app.
       If those then still use the "attach gpx track recordings" feature and try to upload,
       they will be prompted to re-authenticate (currently) without further explanation
       because the OSM API returned a HTTP 403 (forbidden) error.
     */
    // "write_gpx",
)

val userModule = module {

    if (USE_TEST_API) {
        OAUTH2_TOKEN_URL = "https://master.apis.dev.openstreetmap.org/oauth2/token"
        OAUTH2_AUTHORIZATION_URL = "https://master.apis.dev.openstreetmap.org/oauth2/authorize"
        OAUTH2_CLIENT_ID = "ObZ7yPf4lfs4XJ3NWysI3ukJMN0SHey1oPnNQnLmvw8"
    }

    single<UserDataSource> { get<UserDataController>() }
    single { UserDataController(get()) }

    single<UserLoginSource> { get<UserLoginController>() }
    single { UserLoginController(get()) }

    single { UserUpdater(get(), get(), get(), get(), get(), get()) }

    single { OAuthApiClient(get()) }
}
