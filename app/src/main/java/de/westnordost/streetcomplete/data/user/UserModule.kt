package de.westnordost.streetcomplete.data.user

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.user.UserDao
import de.westnordost.streetcomplete.data.OsmModule
import de.westnordost.streetcomplete.data.user.achievements.*
import oauth.signpost.OAuthConsumer
import oauth.signpost.OAuthProvider
import oauth.signpost.basic.DefaultOAuthConsumer
import oauth.signpost.basic.DefaultOAuthProvider
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
object UserModule {
    private const val STATISTICS_BACKEND_URL = "https://www.westnordost.de/stats/"
    private const val BASE_OAUTH_URL = "https://www.openstreetmap.org/oauth/"
    private const val CONSUMER_KEY = "L3JyJMjVk6g5atwACVySRWgmnrkBAH7u0U18ALO7"
    private const val CONSUMER_SECRET = "uNjPaXZw15CPHdCSeMzttRm20tyFGaBPO7jHt52c"
    private const val CALLBACK_SCHEME = "streetcomplete"
    private const val CALLBACK_HOST = "oauth"

	@Provides fun statisticsDownloader(): StatisticsDownloader =
        StatisticsDownloader(STATISTICS_BACKEND_URL)

    @Provides fun oAuthStore(prefs: SharedPreferences): OAuthStore = OAuthStore(
        prefs, Provider { defaultOAuthConsumer() }
    )

	@Provides fun oAuthProvider(): OAuthProvider = DefaultOAuthProvider(
        BASE_OAUTH_URL + "request_token",
        BASE_OAUTH_URL + "access_token",
        BASE_OAUTH_URL + "authorize"
    )

	@Provides fun defaultOAuthConsumer(): OAuthConsumer =
        DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET)

	@Provides @Named("OAuthCallbackScheme")
    fun oAuthCallbackScheme(): String = CALLBACK_SCHEME

	@Provides @Named("OAuthCallbackHost")
    fun oAuthCallbackHost(): String = CALLBACK_HOST

    /* Don't know why this is necessary, as all information is already given in UserController,
    *  but it is necessary :-/ */
	@Provides @Singleton fun userController(
        userDao: UserDao, userStore: UserStore, achievementGiver: AchievementGiver,
        oAuthStore: OAuthStore, userAchievementsDao: UserAchievementsDao,
        userLinksDao: UserLinksDao, @Named("Achievements") achievements: List<Achievement>,
        @Named("Links") links: List<Link>,
        @Named("QuestAliases") questAliases: List<Pair<String, String>>,
        context: Context, statisticsDownloader: StatisticsDownloader,
        statisticsDao: QuestStatisticsDao, osmConnection: OsmConnection
    ): UserController = UserController(
            userDao, oAuthStore, userStore, achievementGiver, userAchievementsDao, userLinksDao,
            achievements, links, questAliases, OsmModule.getAvatarsCacheDirectory(context),
            statisticsDownloader, statisticsDao, osmConnection
        )
}