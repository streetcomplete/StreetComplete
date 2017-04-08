package de.westnordost.streetcomplete.oauth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.user.UserDao;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import oauth.signpost.OAuthConsumer;

/** Manages callback from OAuthWebViewDialogFragment */
public class OAuthComponent
{
	private final SharedPreferences prefs;
	private final Context context;
	private final UserDao userDao;
	private final QuestStatisticsDao statisticsDao;
	private final OsmConnection osmConnection;

	private Listener listener;

	public interface Listener
	{
		void onOAuthAuthorizationVerified();
	}

	@Inject public OAuthComponent(SharedPreferences prefs, Context context, UserDao userDao, QuestStatisticsDao statisticsDao, OsmConnection osmConnection)
	{
		this.prefs = prefs;
		this.context = context;
		this.userDao = userDao;
		this.statisticsDao = statisticsDao;
		this.osmConnection = osmConnection;
	}

	public void setListener(Listener listener)
	{
		this.listener = listener;
	}

	public void onOAuthAuthorized(OAuthConsumer consumer, List<String> permissions)
	{
		if(permissions.containsAll(OAuth.REQUIRED_PERMISSIONS))
		{
			onAuthorizationSuccess(consumer);
		}
		else
		{
			Toast.makeText(context, R.string.oauth_failed_permissions, Toast.LENGTH_LONG).show();
			onAuthorizationFailed();
		}
	}

	public void onOAuthCancelled()
	{
		Toast.makeText(context,R.string.oauth_cancelled, Toast.LENGTH_SHORT).show();
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

		new PostAuthorizationTask().execute();
	}

	private class PostAuthorizationTask extends AsyncTask <Void, Void, Void>
	{
		@Override protected Void doInBackground(Void... params)
		{
			long userId = userDao.getMine().id;

			SharedPreferences.Editor editor = prefs.edit();
			editor.putLong(Prefs.OSM_USER_ID, userId);
			editor.apply();

			// DISABLED: this is too data intensive for the app to do for itself. Should be done by
			// an external server once and be queried therafter from it
			//statisticsDao.syncFromOsmServer(userId);
			return null;
		}

		@Override protected void onPostExecute(Void result)
		{
			if(listener != null) listener.onOAuthAuthorizationVerified();
		}
	}
}