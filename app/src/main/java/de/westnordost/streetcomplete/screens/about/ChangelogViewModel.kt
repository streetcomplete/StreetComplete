package de.westnordost.streetcomplete.screens.about

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.getRawTextFile
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class ChangelogViewModel : ViewModel() {
    abstract val changelog: StateFlow<String?>
}

class ChangelogViewModelImpl(resources: Resources) : ChangelogViewModel() {
    override val changelog = MutableStateFlow<String?>(null)

    init {
        launch(Dispatchers.IO) { changelog.value = resources.getRawTextFile(R.raw.changelog) }
    }
}
