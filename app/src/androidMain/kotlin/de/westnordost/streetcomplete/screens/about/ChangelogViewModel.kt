package de.westnordost.streetcomplete.screens.about

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.changelog.Changelog
import de.westnordost.streetcomplete.data.changelog.readChangelog
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class ChangelogViewModel : ViewModel() {
    /* version name -> html */
    abstract val changelog: StateFlow<Changelog?>
}

@Stable
class ChangelogViewModelImpl(private val res: Res) : ChangelogViewModel() {
    override val changelog = MutableStateFlow<Changelog?>(null)

    init {
        launch {
            changelog.value = res.readChangelog()
        }
    }
}
