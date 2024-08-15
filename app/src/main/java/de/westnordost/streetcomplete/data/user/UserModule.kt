package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.user.oauth.OAuthApiClient
import org.koin.dsl.module

const val OAUTH2_TOKEN_URL = "https://www.openstreetmap.org/oauth2/token"
const val OAUTH2_AUTHORIZATION_URL = "https://www.openstreetmap.org/oauth2/authorize"

const val OAUTH2_CLIENT_ID = "Yyk4PmTopczrr3BWZYvLK_M-KBloCQwXgPGEzqUYTc8"

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
