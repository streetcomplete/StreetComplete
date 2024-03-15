package de.westnordost.streetcomplete.screens.user.links

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.user.achievements.Link
import kotlinx.coroutines.flow.StateFlow

abstract class LinksViewModel : ViewModel() {
    abstract val isSynchronizingStatistics: StateFlow<Boolean>
    abstract val links: StateFlow<List<Link>?>
}
