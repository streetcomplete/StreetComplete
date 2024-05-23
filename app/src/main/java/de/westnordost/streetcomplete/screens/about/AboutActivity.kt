package de.westnordost.streetcomplete.screens.about

import android.os.Bundle
import de.westnordost.streetcomplete.screens.FragmentContainerActivity
import androidx.activity.compose.setContent
import de.westnordost.streetcomplete.screens.about.logs.LogsScreen
import de.westnordost.streetcomplete.screens.about.logs.LogsViewModel
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutActivity : FragmentContainerActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (savedInstanceState == null) {
            replaceMainFragment(AboutFragment())
        }
    }
}
