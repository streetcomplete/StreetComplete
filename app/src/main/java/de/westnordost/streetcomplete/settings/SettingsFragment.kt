package de.westnordost.streetcomplete.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import android.widget.Toast

import javax.inject.Provider

import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.oauth.OAuthPrefs
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.oauth.OsmOAuthDialogFragment
import de.westnordost.streetcomplete.settings.questselection.QuestSelectionFragment
import javax.inject.Inject
import de.westnordost.streetcomplete.*


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, IntentListener {

    @Inject internal lateinit var prefs: SharedPreferences
    @Inject internal lateinit var oAuth: OAuthPrefs
    @Inject internal lateinit var applyNoteVisibilityChangedTask: Provider<ApplyNoteVisibilityChangedTask>
    @Inject internal lateinit var downloadedTilesDao: DownloadedTilesDao
    @Inject internal lateinit var osmQuestDao: OsmQuestDao

    private val fragmentActivity: FragmentContainerActivity?
        get() = activity as FragmentContainerActivity?

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(context!!, R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference>("oauth")?.setOnPreferenceClickListener {
            if (oAuth.isAuthorized) {
                context?.let {
                    AlertDialog.Builder(it)
                        .setMessage(R.string.oauth_remove_authorization_dialog_message)
                        .setPositiveButton(R.string.oauth_remove_authorization_confirmation) { _, _ ->
                            oAuth.saveConsumer(null)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
            } else {
                fragmentManager?.let { OsmOAuthDialogFragment().show(it, OsmOAuthDialogFragment.TAG) }
            }
            true
        }

        findPreference<Preference>("quests")?.setOnPreferenceClickListener {
            fragmentActivity?.setCurrentFragment(QuestSelectionFragment())
            true
        }

        findPreference<Preference>("quests.invalidation")?.setOnPreferenceClickListener {
            context?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.invalidation_dialog_message)
                    .setPositiveButton(R.string.invalidate_confirmation) { _, _ ->
                        downloadedTilesDao.removeAll()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            true
        }

        findPreference<Preference>("quests.restore.hidden")?.setOnPreferenceClickListener {
            val hidden = osmQuestDao.unhideAll()
            context?.toast(getString(R.string.restore_hidden_success, hidden), Toast.LENGTH_LONG)
            true
        }

        findPreference<Preference>("debug")?.isVisible = BuildConfig.DEBUG

        findPreference<Preference>("debug.quests")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowQuestFormsActivity::class.java))
            true
        }
    }

    override fun onStart() {
        super.onStart()
        updateOsmAuthSummary()
        activity?.setTitle(R.string.action_settings)
    }

    private fun updateOsmAuthSummary() {
        val oauth = preferenceScreen?.findPreference<Preference>("oauth")
        val username = prefs.getString(Prefs.OSM_USER_NAME, null)
        oauth?.summary = if (oAuth.isAuthorized) {
            if (username != null) resources.getString(R.string.pref_title_authorized_username_summary, username)
            else resources.getString(R.string.pref_title_authorized_summary)
        } else {
            resources.getString(R.string.pref_title_not_authorized_summary2)
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when(key) {
            Prefs.OAUTH_ACCESS_TOKEN_SECRET -> {
                updateOsmAuthSummary()
            }
            Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS -> {
                val task = applyNoteVisibilityChangedTask.get()
                task.setPreference(preferenceScreen.findPreference(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS))
                task.execute()
            }
            Prefs.AUTOSYNC -> {
                if (Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) != Prefs.Autosync.ON) {
                    context?.let {
                        AlertDialog.Builder(it)
                            .setView(R.layout.dialog_tutorial_upload)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }

                }
            }
            Prefs.THEME_SELECT -> {
                val theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "AUTO")!!)
                AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
                activity?.recreate()
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is DialogPreferenceCompat) {
            fragmentManager?.let {
                val fragment = preference.createDialog()
                val bundle = Bundle(1)
                bundle.putString("key", preference.getKey())
                fragment.arguments = bundle
                fragment.setTargetFragment(this, 0)
                fragment.show(it, "androidx.preference.PreferenceFragment.DIALOG")
            }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onNewIntent(intent: Intent) {
        val oauthFragment = fragmentManager?.findFragmentByTag(OsmOAuthDialogFragment.TAG) as OsmOAuthDialogFragment?
        oauthFragment?.onNewIntent(intent)
    }
}
