package de.westnordost.osmagent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.MapView;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.Quest;
import de.westnordost.osmagent.quests.QuestDownloader;
import de.westnordost.osmagent.quests.QuestListener;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmagent.quests.dialogs.QuestDialogListener;
import de.westnordost.osmagent.quests.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.settings.SettingsActivity;
import de.westnordost.osmagent.tangram.MapFragment;
import de.westnordost.osmagent.util.SphericalEarthMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.OsmLatLon;


public class MainActivity extends AppCompatActivity implements QuestDialogListener, QuestListener
{
	private MapController map;
	private MapData questsLayer;

	@Inject QuestDownloader questDownloader;
	@Inject OsmQuestDao osmQuestDB;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.getMapAsync(new MapView.OnMapReadyCallback()
		{
			@Override public void onMapReady(MapController mapController)
			{
				map = mapController;
				questsLayer = map.addDataLayer("osmagent_quests");

				// TODO provisional, for testing
				for(Quest q : osmQuestDB.getAll(null, QuestStatus.NEW))
				{
					addQuestToMap(q);
				}
			}
		});

		questDownloader.addQuestListener(this);
	}


	@Override protected void onDestroy()
	{
		super.onDestroy();
		questDownloader.removeQuestListener(this);
	}

	@Override protected void onStart()
	{
		super.onStart();
	}


	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		switch(id)
		{
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;

			case R.id.action_download:
				BoundingBox yangon = SphericalEarthMath.enclosingBoundingBox(new OsmLatLon(16.77428,96.16560),1000);
				questDownloader.download(yangon, null);
				return true;

			case R.id.action_upload:

				return true;

			case R.id.action_test:
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting() &&
				activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
	}

	private WifiReceiver x;

	@Override public void onResume()
	{
		super.onResume();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		x = new WifiReceiver();
		registerReceiver(x, intentFilter);

	}

	@Override public void onPause()
	{
		super.onPause();
		unregisterReceiver(x);
	}

	@Override public void onAnsweredQuest(int questId, Bundle answer)
	{
		// TODO
	}

	@Override public void onLeaveNote(int questId, String note)
	{
		// TODO
	}

	@Override public void onSkippedQuest(int questId)
	{
		// TODO
	}

	@Override public void onQuestCreated(Quest quest)
	{
		Log.v("OSMAGENT", "Created quest " + quest.getType().getClass().getSimpleName() + " at " +
				quest.getMarkerLocation().getLatitude() + "," +
				quest.getMarkerLocation().getLongitude());

		if(map != null) // map controller might not be initialized yet
		{
			addQuestToMap(quest);
		}
	}

	private void addQuestToMap(Quest quest)
	{
		LngLat pos = new LngLat(
				quest.getMarkerLocation().getLongitude(),
				quest.getMarkerLocation().getLatitude());
		Map<String, String> props = new HashMap<>();
		props.put("type", "point");
		props.put("kind", quest.getType().getIconName());
		questsLayer.addPoint(pos, props);
	}

	@Override public void onQuestRemoved(Quest quest)
	{
		Log.v("OSMAGENT", "Removed quest " + quest.getType().getClass().getSimpleName() + " at " +
				quest.getMarkerLocation().getLatitude() + "," +
				quest.getMarkerLocation().getLongitude());

		if(map != null) // map controller might not be initialized yet
		{
			// TODO
		}
	}

	private class WifiReceiver extends BroadcastReceiver
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
			{
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				Log.i("OSMAGENT", info.isConnected() ? "connected" : "disconnected");
			}
		}
	}
}
