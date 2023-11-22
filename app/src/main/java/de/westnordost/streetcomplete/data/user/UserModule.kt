package de.westnordost.streetcomplete.data.user

import org.koin.dsl.module

const val OAUTH2_TOKEN_URL = "https://www.openstreetmap.org/oauth2/token"
const val OAUTH2_AUTHORIZATION_URL = "https://www.openstreetmap.org/oauth2/authorize"

const val CALLBACK_SCHEME = "streetcomplete"
const val CALLBACK_HOST = "oauth"


val userModule = module {

    single<UserDataSource> { get<UserDataController>() }
    single { UserDataController(get(), get()) }

    single<UserLoginStatusSource> { get<UserLoginStatusController>() }
    single { UserLoginStatusController(get(), get()) }

    single { UserUpdater(get(), get(), get(), get(), get()) }
}
