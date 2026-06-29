package de.westnordost.streetcomplete.screens.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.core.app.ActivityCompat
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.koin.android.ext.android.inject

class SettingsActivity : BaseActivity() {

    private val prefs by inject<Preferences>()

    private val listeners = mutableListOf<SettingsListener>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launchQuestSelection = intent.getBooleanExtra(EXTRA_LAUNCH_QUEST_SETTINGS, false)
        val startDestination = if (launchQuestSelection) SettingsDestination.QuestSelection else null
        setContent {
            AppTheme {
                Surface {
                    SettingsNavHost(
                        onClickBack = { finish() },
                        startDestination = startDestination
                    )
                }
            }
        }

        listeners += prefs.onLanguageChanged { ActivityCompat.recreate(this) }
        listeners += prefs.onThemeChanged { ActivityCompat.recreate(this) }
    }

    override fun onDestroy() {
        super.onDestroy()
        listeners.forEach { it.deactivate() }
        listeners.clear()
    }

    companion object {
        fun createLaunchQuestSettingsIntent(context: Context) =
            Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_LAUNCH_QUEST_SETTINGS, true)
            }

        private const val EXTRA_LAUNCH_QUEST_SETTINGS = "launch_quest_settings"
    }
}
