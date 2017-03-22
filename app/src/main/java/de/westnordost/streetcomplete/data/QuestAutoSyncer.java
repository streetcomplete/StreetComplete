package de.westnordost.streetcomplete.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.download.MobileDataAutoDownloadStrategy;
import de.westnordost.streetcomplete.data.download.QuestAutoDownloadStrategy;
import de.westnordost.streetcomplete.data.download.WifiAutoDownloadStrategy;

/** Automatically downloads and uploads new quests around the user's location and uploads quests.
 *
 *  Respects the user preference to only sync on wifi or not sync automatically at all
 * */
public class QuestAutoSyncer implements LocationListener, LostApiClient.ConnectionCallbacks
{
	private static final String TAG_AUTO_DOWNLOAD = "AutoQuestSyncer";

	private final QuestController questController;
	private final MobileDataAutoDownloadStrategy mobileDataDownloadStrategy;
	private final WifiAutoDownloadStrategy wifiDownloadStrategy;
	private final Context context;
	private final SharedPreferences prefs;

	private LostApiClient lostApiClient;
	private LatLon pos;
	private boolean isWifi;

	@Inject public QuestAutoSyncer(QuestController questController,
								   MobileDataAutoDownloadStrategy mobileDataDownloadStrategy,
								   WifiAutoDownloadStrategy wifiDownloadStrategy,
								   Context context, SharedPreferences prefs)
	{
		this.questController = questController;
		this.mobileDataDownloadStrategy = mobileDataDownloadStrategy;
		this.wifiDownloadStrategy = wifiDownloadStrategy;
		this.context = context;
		this.prefs = prefs;
		lostApiClient = new LostApiClient.Builder(context).addConnectionCallbacks(this).build();
	}

	public void onStart()
	{
		isWifi = isConnected(ConnectivityManager.TYPE_WIFI);
		context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
	}

	public void onStop()
	{
		stopPositionTracking();
		context.unregisterReceiver(wifiReceiver);
	}

	public void startPositionTracking()
	{
		if(!lostApiClient.isConnected()) lostApiClient.connect();
	}

	public void stopPositionTracking()
	{
		try // TODO remove when https://github.com/mapzen/lost/issues/143 is solved
		{
			if(lostApiClient.isConnected())
			{
				LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, this);
				lostApiClient.disconnect();
			}
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnected() throws SecurityException
	{
		LocationRequest request = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setSmallestDisplacement(500)
				.setInterval(3 * 60 * 1000); // 3 minutes

		LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, this);
	}

	@Override
	public void onConnectionSuspended() {}

	@Override public void onLocationChanged(Location location)
	{
		this.pos = new OsmLatLon(location.getLatitude(), location.getLongitude());
		triggerAutoDownload();
	}

	@Override public void onProviderEnabled(String provider) {}
	@Override public void onProviderDisabled(String provider) {}

	public void triggerAutoDownload()
	{
		if(!isAllowedByPreference()) return;
		if(pos == null) return;
		if(!isConnected()) return;
		if(questController.isPriorityDownloadRunning()) return;

		Log.i(TAG_AUTO_DOWNLOAD, "Checking whether to automatically download new quests at "
				+ pos.getLatitude() + "," + pos.getLongitude());

		final QuestAutoDownloadStrategy downloadStrategy = isWifi ? wifiDownloadStrategy : mobileDataDownloadStrategy;

		new Thread(){ @Override public void run() {

			if(!downloadStrategy.mayDownloadHere(pos)) return;

			questController.download(
					downloadStrategy.getDownloadBoundingBox(pos),
					downloadStrategy.getQuestTypeDownloadCount(), false);
		}}.start();
	}

	public void triggerAutoUpload()
	{
		if(!isAllowedByPreference()) return;
		if(!isConnected()) return;
		questController.upload();
	}

	private boolean isConnected()
	{
		return isConnected(null);
	}

	private boolean isConnected(Integer networkType)
	{
		ConnectivityManager connectivityManager
				= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected() &&
				(networkType == null || activeNetworkInfo.getType() == networkType);
	}

	private final BroadcastReceiver wifiReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			// only do something if the state really changes (broadcast is called more often)
			if(info.isConnected() == isWifi) return;
			isWifi = info.isConnected();
			if(isWifi)
			{
				triggerAutoDownload();
				triggerAutoUpload();
			}
		}
	};

	private boolean isAllowedByPreference()
	{
		Prefs.Autosync p = Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC,"ON"));
		return  p == Prefs.Autosync.ON || p == Prefs.Autosync.WIFI && isWifi;
	}
}
