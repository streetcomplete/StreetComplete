package de.westnordost.streetcomplete.screens.user

import de.westnordost.streetcomplete.screens.user.profile.ProfileViewModel
import de.westnordost.streetcomplete.screens.user.profile.ProfileViewModelImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val userScreenModule = module {
    factory<ProfileViewModel> { ProfileViewModelImpl(
        get(), get(), get(), get(), get(), get(), get(named("AvatarsCacheDirectory")), get()
    ) }
}
