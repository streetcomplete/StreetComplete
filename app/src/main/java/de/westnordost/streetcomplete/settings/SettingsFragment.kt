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

import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestDao
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.ktx.toast
import javax.inject.Inject
import de.westnordost.streetcomplete.*
import de.westnordost.streetcomplete.data.user.UserController

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject internal lateinit var prefs: SharedPreferences
    @Inject internal lateinit var userController: UserController
    @Inject internal lateinit var applyNoteVisibilityChangedTask: Provider<ApplyNoteVisibilityChangedTask>
    @Inject internal lateinit var downloadedTilesDao: DownloadedTilesDao
    @Inject internal lateinit var osmQuestDao: OsmQuestDao

    interface Listener {
        fun onClickedQuestSelection()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference>("quests")?.setOnPreferenceClickListener {
            listener?.onClickedQuestSelection()
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
        activity?.setTitle(R.string.action_settings)
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
}
