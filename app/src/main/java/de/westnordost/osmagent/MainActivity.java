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

import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapView;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.osmagent.quests.Quest;
import de.westnordost.osmagent.quests.OverpassQuestDownloader;
import de.westnordost.osmagent.quests.dialogs.QuestDialogListener;
import de.westnordost.osmagent.settings.SettingsActivity;
import de.westnordost.osmagent.tangram.MapFragment;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.OsmLatLon;


public class MainActivity extends AppCompatActivity implements QuestDialogListener
{
	MapController map;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.getMapAsync(new MapView.OnMapReadyCallback()
		{
			@Override
			public void onMapReady(MapController mapController)
			{
				map = mapController;
			}
		});

	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.action_settings)
		{
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		if(id == R.id.action_test)
		{

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

	@Override
	public void onResume()
	{
		super.onResume();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		x = new WifiReceiver();
		registerReceiver(x, intentFilter);

	}

	@Override
	public void onPause()
	{
		super.onPause();
		unregisterReceiver(x);
	}

	@Override
	public void onAnsweredQuest(int questId, Bundle answer)
	{
		// TODO
	}

	@Override
	public void onLeaveNote(int questId, String note)
	{
		// TODO
	}

	@Override
	public void onSkippedQuest(int questId)
	{
		// TODO
	}

	private class WifiReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
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
