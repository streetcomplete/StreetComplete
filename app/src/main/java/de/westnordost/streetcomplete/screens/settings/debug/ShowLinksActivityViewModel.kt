package de.westnordost.streetcomplete.screens.settings.debug

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.user.achievements.Link

abstract class ShowLinksActivityViewModel : ViewModel() {
    abstract val links: List<Link>
}

class ShowLinksActivityViewModelImpl(override val links: List<Link>) : ShowLinksActivityViewModel()
