package de.westnordost.streetcomplete.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.oauth.OAuth;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.util.InlineAsyncTask;
import de.westnordost.streetcomplete.oauth.OAuthWebViewDialogFragment;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.user.Permission;
import de.westnordost.osmapi.user.PermissionsDao;
import de.westnordost.osmapi.user.UserDao;
import oauth.signpost.OAuthConsumer;

public class SettingsActivity extends AppCompatActivity implements OAuthWebViewDialogFragment.OAuthListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		setContentView(R.layout.activity_settings);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onOAuthAuthorized(OAuthConsumer consumer)
	{
		List<String> expectedPermissions = Arrays.asList(
				Permission.READ_PREFERENCES_AND_USER_DETAILS,
				Permission.MODIFY_MAP,
				Permission.WRITE_NOTES,
				Permission.WRITE_GPS_TRACES);
		new VerifyPermissionsTask(consumer, expectedPermissions).execute();
	}

	@Override
	public void onOAuthCancelled()
	{
		showToast(R.string.oauth_cancelled, Toast.LENGTH_SHORT);
		onAuthorizationFailed();
	}

	private void onAuthorizationFailed()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		OAuth.deleteConsumer(prefs);

		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Prefs.OSM_USER_ID);
		editor.apply();
	}

	private void onAuthorizationSuccess(OAuthConsumer consumer)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		OAuth.saveConsumer(prefs, consumer);

		OsmConnection osm = OsmModule.osmConnection(consumer);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(Prefs.OSM_USER_ID, new UserDao(osm).getMine().id);
		editor.apply();
	}

	private class VerifyPermissionsTask extends InlineAsyncTask<Boolean>
	{
		private OAuthConsumer consumer;
		private Collection<String> expectedPermissions;

		public VerifyPermissionsTask(OAuthConsumer consumer, Collection<String> expectedPermissions)
		{
			this.consumer = consumer;
			this.expectedPermissions = expectedPermissions;
		}

		@Override
		protected Boolean doInBackground() throws Exception
		{
			OsmConnection osm = OsmModule.osmConnection(consumer);
			List<String> permissions = new PermissionsDao(osm).get();
			return permissions.containsAll(expectedPermissions);
		}

		@Override
		public void onSuccess(Boolean hasPermissions)
		{
			if (hasPermissions)
			{
				onAuthorizationSuccess(consumer);
			}
			else
			{
				showToast(R.string.oauth_failed_permissions, Toast.LENGTH_LONG);
				onAuthorizationFailed();
			}
		}

		@Override
		public void onError(Exception error)
		{
			int resId = R.string.oauth_failed_verify_permissions;
			showToast(resId, Toast.LENGTH_LONG);
			Log.e(OAuthWebViewDialogFragment.TAG, getResources().getString(resId), error);
			onAuthorizationFailed();
		}
	}

	private void showToast(int resId, int length)
	{
		Toast.makeText(this, resId, length).show();
	}
}
