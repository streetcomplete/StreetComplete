package de.westnordost.streetcomplete.screens.about

import android.os.Bundle
import androidx.activity.compose.setContent
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.ui.theme.AppTheme

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AboutNavHost(onClickBack = { finish() })
            }
        }
    }
}
