package de.westnordost.streetcomplete.screens.user

import de.westnordost.streetcomplete.screens.user.achievements.AchievementsViewModel
import de.westnordost.streetcomplete.screens.user.achievements.AchievementsViewModelImpl
import de.westnordost.streetcomplete.screens.user.links.LinksViewModel
import de.westnordost.streetcomplete.screens.user.links.LinksViewModelImpl
import de.westnordost.streetcomplete.screens.user.login.LoginViewModel
import de.westnordost.streetcomplete.screens.user.login.LoginViewModelImpl
import de.westnordost.streetcomplete.screens.user.profile.ProfileViewModel
import de.westnordost.streetcomplete.screens.user.profile.ProfileViewModelImpl
import de.westnordost.streetcomplete.screens.user.statistics.EditStatisticsViewModel
import de.westnordost.streetcomplete.screens.user.statistics.EditStatisticsViewModelImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val userScreenModule = module {
    factory<ProfileViewModel> { ProfileViewModelImpl(
        get(), get(), get(), get(), get(), get(), get(named("AvatarsCacheDirectory"))
    ) }

    factory<LoginViewModel> { LoginViewModelImpl(get(), get(), get(), get()) }

    factory<EditStatisticsViewModel> { EditStatisticsViewModelImpl(get(), get()) }

    factory<LinksViewModel> { LinksViewModelImpl(get(), get()) }

    factory<AchievementsViewModel> { AchievementsViewModelImpl(get(), get()) }

    factory<UserViewModel> { UserViewModelImpl(get()) }
}
