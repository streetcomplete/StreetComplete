package de.westnordost.streetcomplete.oauth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.user.PermissionsDao;
import de.westnordost.osmapi.user.UserDao;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.util.InlineAsyncTask;
import oauth.signpost.OAuthConsumer;

/** Manages callback from OAuthWebViewDialogFragment */
public class OAuthComponent
{
	private final SharedPreferences prefs;
	private final Context context;
	private final UserDao userDao;
	private final OsmConnection osmConnection;

	private Listener listener;

	public interface Listener
	{
		void onOAuthAuthorizationVerified();
	}

	@Inject public OAuthComponent(SharedPreferences prefs, Context context, UserDao userDao, OsmConnection osmConnection)
	{
		this.prefs = prefs;
		this.context = context;
		this.userDao = userDao;
		this.osmConnection = osmConnection;
	}

	public void setListener(Listener listener)
	{
		this.listener = listener;
	}

	public void onOAuthAuthorized(OAuthConsumer consumer, List<String> expectedPermissions)
	{
		new VerifyPermissionsTask(consumer, expectedPermissions).execute();
	}

	public void onOAuthCancelled()
	{
		showToast(R.string.oauth_cancelled, Toast.LENGTH_SHORT);
		onAuthorizationFailed();
	}

	private void onAuthorizationFailed()
	{
		OAuth.deleteConsumer(prefs);
		// the osm connection is a singleton. Updating its oauth consumer will provide the new consumer
		// for all daos out there (using this connection)
		osmConnection.setOAuth(OAuth.loadConsumer(prefs));

		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Prefs.OSM_USER_ID);
		editor.apply();
	}

	private void onAuthorizationSuccess(OAuthConsumer consumer)
	{
		OAuth.saveConsumer(prefs, consumer);
		// the osm connection is a singleton. Updating its oauth consumer will provide the new consumer
		// for all daos out there (using this connection)
		osmConnection.setOAuth(OAuth.loadConsumer(prefs));

		listener.onOAuthAuthorizationVerified();

		new Thread() { @Override public void run()
		{
			SharedPreferences.Editor editor = prefs.edit();
			editor.putLong(Prefs.OSM_USER_ID, userDao.getMine().id);
			editor.apply();
		}
		}.start();
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
			Log.e(OAuthWebViewDialogFragment.TAG, context.getResources().getString(resId), error);
			onAuthorizationFailed();
		}
	}

	private void showToast(int resId, int length)
	{
		Toast.makeText(context, resId, length).show();
	}
}