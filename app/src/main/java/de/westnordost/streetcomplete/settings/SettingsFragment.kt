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

import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.IntentListener
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.oauth.OAuthPrefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.oauth.OsmOAuthDialogFragment
import de.westnordost.streetcomplete.settings.questselection.QuestSelectionFragment
import javax.inject.Inject


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, IntentListener {

    private val prefs: SharedPreferences
	private val oAuth: OAuthPrefs
	private val applyNoteVisibilityChangedTask: Provider<ApplyNoteVisibilityChangedTask>
	private val downloadedTilesDao: DownloadedTilesDao
	private val osmQuestDao: OsmQuestDao

	private val fragmentActivity: FragmentContainerActivity?
		get() = activity as FragmentContainerActivity?

	init {
		val fields = InjectedFields()
		Injector.instance.applicationComponent.inject(fields)
		prefs = fields.prefs
		oAuth = fields.oAuth
		applyNoteVisibilityChangedTask = fields.applyNoteVisibilityChangedTask
		downloadedTilesDao = fields.downloadedTilesDao
		osmQuestDao = fields.osmQuestDao
	}

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(context!!, R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

	    preferenceScreen.findPreference("oauth").setOnPreferenceClickListener {
	        fragmentManager?.let { OsmOAuthDialogFragment().show(it, OsmOAuthDialogFragment.TAG) }
            true
        }

	    preferenceScreen.findPreference("quests").setOnPreferenceClickListener {
            fragmentActivity?.setCurrentFragment(QuestSelectionFragment())
            true
        }

	    preferenceScreen.findPreference("quests.invalidation").setOnPreferenceClickListener {
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

        preferenceScreen.findPreference("quests.restore.hidden").setOnPreferenceClickListener {
            val hidden = osmQuestDao.getAll(null, QuestStatus.HIDDEN, null, null, null)
            for (q in hidden) {
                q.status = QuestStatus.NEW
            }
            osmQuestDao.replaceAll(hidden)
	        context?.toast(getString(R.string.restore_hidden_success, hidden.size), Toast.LENGTH_LONG)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        updateOsmAuthSummary()
        activity?.setTitle(R.string.action_settings)
    }

    private fun updateOsmAuthSummary() {
        val oauth = preferenceScreen.findPreference("oauth")
        val username = prefs.getString(Prefs.OSM_USER_NAME, null)
	    oauth.summary = if (oAuth.isAuthorized) {
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
			    if (Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")) != Prefs.Autosync.ON) {
				    context?.let {
					    AlertDialog.Builder(it)
						    .setView(R.layout.dialog_tutorial_upload)
						    .setPositiveButton(android.R.string.ok, null)
						    .show()
				    }

			    }
		    }
		    Prefs.THEME_SELECT -> {
			    val theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "AUTO"))
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

	internal class InjectedFields {
		@Inject internal lateinit var prefs: SharedPreferences
		@Inject internal lateinit var oAuth: OAuthPrefs
		@Inject internal lateinit var applyNoteVisibilityChangedTask: Provider<ApplyNoteVisibilityChangedTask>
		@Inject internal lateinit var downloadedTilesDao: DownloadedTilesDao
		@Inject internal lateinit var osmQuestDao: OsmQuestDao
	}
}
