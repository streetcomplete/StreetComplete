package de.westnordost.streetcomplete.screens.user

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.ui.theme.AppTheme

class UserActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launchAuth = intent.getBooleanExtra(EXTRA_LAUNCH_AUTH, false)
        setContent {
            AppTheme {
                Surface {
                    UserNavHost(
                        launchAuth = launchAuth,
                        onClickBack = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_LAUNCH_AUTH = "de.westnordost.streetcomplete.screens.user.launch_auth"
    }
}
