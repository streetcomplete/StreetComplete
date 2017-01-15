package de.westnordost.streetcomplete.tangram;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.tangram.HttpHandler;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.MapView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.R;

/** The map view with the attribution texts and the "find me" button.  */
public class MapFragment extends Fragment implements
		FragmentCompat.OnRequestPermissionsResultCallback, LocationListener, LostApiClient.ConnectionCallbacks
{
	private static final String LOCATION_LAYER = "streetcomplete_location";

	private MapData locationLayer;

	private MapView mapView;

	/** controller to the asynchronously loaded map. Since it is loaded asynchronously, could be
	 *  null still at any point! */
	protected MapController controller;

	private HttpHandler mapHttpHandler = new HttpHandler();

	private LostApiClient lostApiClient;
	private boolean zoomedYet;
	private boolean startTrackingWhenConnected;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		mapView = (MapView) view.findViewById(R.id.map);

		return view;
	}

	/* --------------------------------- Map and Location --------------------------------------- */

	public void getMapAsync()
	{
		getMapAsync("scene.yaml");
	}

	public void getMapAsync(@NonNull final String sceneFilePath)
	{
		mapView.getMapAsync(new MapView.OnMapReadyCallback()
		{
			@Override public void onMapReady(MapController ctrl)
			{
				controller = ctrl;
				initMap();
			}
		}, sceneFilePath);
	}

	protected void initMap()
	{
		updateMapTileCacheSize();
		controller.setHttpHandler(mapHttpHandler);
		restoreCameraState();
		locationLayer = controller.addDataLayer(LOCATION_LAYER);
	}


	private void updateMapTileCacheSize()
	{
		int cacheSize = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Prefs.MAP_TILECACHE, 50);

		File cacheDir = getActivity().getExternalCacheDir();
		if (cacheDir != null && cacheDir.exists())
		{
			mapHttpHandler.setCache(new File(cacheDir, "tile_cache"), cacheSize * 1024 * 1024);
		}
	}

	public void startPositionTracking() throws SecurityException
	{
		if(!lostApiClient.isConnected())
		{
			startTrackingWhenConnected = true;
			return;
		}
		startTrackingWhenConnected = false;

		zoomedYet = false;

		LocationRequest request = LocationRequest.create()
				.setInterval(2000)
				.setSmallestDisplacement(5)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, this);
	}

	public void stopPositionTracking()
	{
		if(locationLayer != null)
		{
			locationLayer.clear();
		}

		LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, this);
	}

	/* ------------------------------------ LOST ------------------------------------------- */

	@Override public void onLocationChanged(Location location)
	{
		showLocation(location);
		if(!zoomedYet)
		{
			zoomTo(location);
		}
	}

	private void showLocation(Location location)
	{
		if(locationLayer != null)
		{
			locationLayer.clear();
			LngLat pos = new LngLat(location.getLongitude(), location.getLatitude());
			int accuracy = (int)(Math.ceil(location.getAccuracy()));

			Map<String, String> props = new HashMap<>();
			props.put("type", "point");
			locationLayer.addPoint(pos, props);
			controller.requestRender();
		}
	}

	@Override public void onProviderEnabled(String provider)
	{

	}

	@Override public void onProviderDisabled(String provider)
	{

	}

	private void zoomTo(Location location)
	{
		if(controller == null) return;

		zoomedYet = true;
		if(controller.getZoom() < 16) controller.setZoomEased(17, 1000);
		controller.setPositionEased(new LngLat(location.getLongitude(), location.getLatitude()),1000);
	}

	private static final String PREF_ROTATION = "map_rotation";
	private static final String PREF_TILT = "map_tilt";
	private static final String PREF_ZOOM = "map_zoom";
	private static final String PREF_LAT = "map_lat";
	private static final String PREF_LON = "map_lon";

	private void restoreCameraState()
	{
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);

		if(prefs.contains(PREF_ROTATION)) controller.setRotation(prefs.getFloat(PREF_ROTATION,0));
		if(prefs.contains(PREF_TILT)) controller.setTilt(prefs.getFloat(PREF_TILT,0));
		if(prefs.contains(PREF_ZOOM)) controller.setZoom(prefs.getFloat(PREF_ZOOM,0));

		if(prefs.contains(PREF_LAT) && prefs.contains(PREF_LON))
		{
			LngLat pos = new LngLat(
					Double.longBitsToDouble(prefs.getLong(PREF_LON,0)),
					Double.longBitsToDouble(prefs.getLong(PREF_LAT,0))
			);
			controller.setPosition(pos);
		}
	}

	private void saveCameraState()
	{
		if(controller == null) return;

		SharedPreferences.Editor editor = getActivity().getPreferences(Activity.MODE_PRIVATE).edit();
		editor.putFloat(PREF_ROTATION, controller.getRotation());
		editor.putFloat(PREF_TILT, controller.getTilt());
		editor.putFloat(PREF_ZOOM, controller.getZoom());
		LngLat pos = controller.getPosition();
		editor.putLong(PREF_LAT, Double.doubleToRawLongBits(pos.latitude));
		editor.putLong(PREF_LON, Double.doubleToRawLongBits(pos.longitude));
		editor.apply();
	}


	/* ------------------------------------ Lifecycle ------------------------------------------- */

	@Override public void onCreate(@Nullable Bundle bundle)
	{
		super.onCreate(bundle);
		if(mapView != null) mapView.onCreate(bundle);
	}

	@Override public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		lostApiClient = new LostApiClient.Builder(activity).addConnectionCallbacks(this).build();
	}

	@Override public void onStart()
	{
		super.onStart();
		lostApiClient.connect();
		updateMapTileCacheSize();
	}

	@Override public void onResume()
	{
		super.onResume();
		if(mapView != null) mapView.onResume();
	}

	@Override public void onPause()
	{
		super.onPause();
		if(mapView != null) mapView.onPause();
		saveCameraState();
	}

	@Override public void onStop()
	{
		super.onStop();
		try
		{
			stopPositionTracking();
			lostApiClient.disconnect();
		} catch (NullPointerException e)
		{
			// TODO: remove when https://github.com/mapzen/lost/issues/143 is solved
			e.printStackTrace();
		}
	}

	@Override public void onConnected()
	{
		if(startTrackingWhenConnected) startPositionTracking();
	}

	@Override public void onConnectionSuspended()
	{

	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		if(mapView != null) mapView.onDestroy();
	}

	@Override public void onLowMemory()
	{
		super.onLowMemory();
		if(mapView != null) mapView.onLowMemory();
	}

}
