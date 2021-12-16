package de.westnordost.streetcomplete.data.user

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import oauth.signpost.OAuthConsumer
import oauth.signpost.OAuthProvider
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.OkHttpOAuthProvider
import javax.inject.Named
import javax.inject.Provider

@Module
object UserModule {
    private const val BASE_OAUTH_URL = "https://www.openstreetmap.org/oauth/"
    private const val CONSUMER_KEY = "L3JyJMjVk6g5atwACVySRWgmnrkBAH7u0U18ALO7"
    private const val CONSUMER_SECRET = "uNjPaXZw15CPHdCSeMzttRm20tyFGaBPO7jHt52c"
    private const val CALLBACK_SCHEME = "streetcomplete"
    private const val CALLBACK_HOST = "oauth"

    @Provides fun oAuthStore(prefs: SharedPreferences): OAuthStore = OAuthStore(
        prefs, Provider { oAuthConsumer() }
    )

	@Provides fun oAuthProvider(): OAuthProvider = OkHttpOAuthProvider(
        BASE_OAUTH_URL + "request_token",
        BASE_OAUTH_URL + "access_token",
        BASE_OAUTH_URL + "authorize"
    )

	@Provides fun oAuthConsumer(): OAuthConsumer =
	    OkHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET)

    @Provides fun userDataSource(controller: UserDataController): UserDataSource = controller
    @Provides fun userLoginStatusSource(controller: UserLoginStatusController): UserLoginStatusSource = controller

	@Provides @Named("OAuthCallbackScheme")
    fun oAuthCallbackScheme(): String = CALLBACK_SCHEME

	@Provides @Named("OAuthCallbackHost")
    fun oAuthCallbackHost(): String = CALLBACK_HOST
}
