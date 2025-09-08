package de.westnordost.streetcomplete.data.user.achievements

import org.koin.core.qualifier.named
import org.koin.dsl.module

val achievementsModule = module {
    factory { UserAchievementsDao(get()) }
    factory { UserLinksDao(get()) }

    single<AchievementsSource> { get<AchievementsController>() }
    single { AchievementsController(get(), get(), get(), get(), get(named("Achievements")), get(named("Links"))) }
}
