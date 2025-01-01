package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.ApplicationConstants.USE_TEST_API
import de.westnordost.streetcomplete.data.user.oauth.OAuthApiClient
import org.koin.dsl.module

private const val OAUTH2_HOST_LIVE = "https://www.openstreetmap.org/"
private const val OAUTH2_HOST_TEST = "https://master.apis.dev.openstreetmap.org/"

private const val OAUTH2_CLIENT_ID_LIVE = "Yyk4PmTopczrr3BWZYvLK_M-KBloCQwXgPGEzqUYTc8"
private const val OAUTH2_CLIENT_ID_TEST = "ObZ7yPf4lfs4XJ3NWysI3ukJMN0SHey1oPnNQnLmvw8"

val OAUTH2_TOKEN_URL =
    (if (USE_TEST_API) OAUTH2_HOST_TEST else OAUTH2_HOST_LIVE) + "oauth2/token"
val OAUTH2_AUTHORIZATION_URL =
    (if (USE_TEST_API) OAUTH2_HOST_TEST else OAUTH2_HOST_LIVE) + "oauth2/authorize"
val OAUTH2_CLIENT_ID =
    if (USE_TEST_API) OAUTH2_CLIENT_ID_TEST else OAUTH2_CLIENT_ID_LIVE

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
    single<UserDataSource> { get<UserDataController>() }
    single { UserDataController(get()) }

    single<UserLoginSource> { get<UserLoginController>() }
    single { UserLoginController(get()) }

    single { UserUpdater(get(), get(), get(), get(), get(), get()) }

    single { OAuthApiClient(get()) }
}
