package de.westnordost.streetcomplete.screens.user

import de.westnordost.streetcomplete.screens.user.profile.ProfileViewModel
import de.westnordost.streetcomplete.screens.user.profile.ProfileViewModelImpl
import de.westnordost.streetcomplete.screens.user.statistics.EditStatisticsViewModel
import de.westnordost.streetcomplete.screens.user.statistics.EditStatisticsViewModelImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val userScreenModule = module {
    factory<ProfileViewModel> { ProfileViewModelImpl(
        get(), get(), get(), get(), get(), get(), get(named("AvatarsCacheDirectory")), get()
    ) }

    factory<EditStatisticsViewModel> { EditStatisticsViewModelImpl(get(), get()) }
}
