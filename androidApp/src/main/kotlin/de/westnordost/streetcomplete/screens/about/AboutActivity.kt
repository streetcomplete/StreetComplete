package de.westnordost.streetcomplete.screens.about

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.ui.theme.AppTheme

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    AboutNavHost(onClickBack = { finish() })
                }
            }
        }
    }
}
