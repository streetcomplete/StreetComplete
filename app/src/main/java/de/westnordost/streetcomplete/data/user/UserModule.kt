package de.westnordost.streetcomplete.data.user

import org.koin.dsl.module

const val OAUTH2_TOKEN_URL = "https://www.openstreetmap.org/oauth2/token"
const val OAUTH2_AUTHORIZATION_URL = "https://www.openstreetmap.org/oauth2/authorize"

const val OAUTH2_CLIENT_ID = "Yyk4PmTopczrr3BWZYvLK_M-KBloCQwXgPGEzqUYTc8"

const val OAUTH2_CALLBACK_SCHEME = "streetcomplete"
const val OAUTH2_CALLBACK_HOST = "oauth"

val OAUTH2_REDIRECT_URI = "$OAUTH2_CALLBACK_SCHEME://$OAUTH2_CALLBACK_HOST"

val OAUTH2_REQUIRED_SCOPES = listOf(
    "read_prefs",
    "write_api",
    "write_notes",
    "write_gpx",
)

val userModule = module {

    single<UserDataSource> { get<UserDataController>() }
    single { UserDataController(get(), get()) }

    single<UserLoginStatusSource> { get<UserLoginStatusController>() }
    single { UserLoginStatusController(get(), get()) }

    single { UserUpdater(get(), get(), get(), get(), get()) }
}
