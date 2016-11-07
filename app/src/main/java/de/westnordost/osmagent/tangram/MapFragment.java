package de.westnordost.osmagent.tangram;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
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
import com.mapzen.tangram.MapView;

import java.io.File;

import de.westnordost.osmagent.Prefs;
import de.westnordost.osmagent.R;

/** The map view with the attribution texts and the "find me" button.  */
public class MapFragment extends Fragment implements
		FragmentCompat.OnRequestPermissionsResultCallback, LocationListener
{
	private MapView mapView;
	private ToggleImageButton findMe;

	/** controller to the asynchronously loaded map. Since it is loaded asynchronously, could be
	 *  null still at any point! */
	private MapController controller;

	private HttpHandler mapHttpHandler = new HttpHandler();

	private LostApiClient lostApiClient;

	/** whether the user's location is currently tracked. This is not equal to findMe.isChecked()
	 *  whenever tracking did not start yet (user did not agree to the permission yet / the
	 *  fragment was just restarted)*/
	private boolean isTracking;

	private static final int LOCATION_PERMISSION_REQUEST = 1;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		mapView = (MapView) view.findViewById(R.id.map);
		findMe = (ToggleImageButton) view.findViewById(R.id.btn_find_me);
		findMe.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangeListener()
		{
			@Override public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					if(!tryStartTrackPosition())
					{
						requestPermissionForPositionTracking();
					}
				}
				else
				{
					stopTrackPosition();
				}
			}
		});


		return view;
	}

	/* ---------------------------------- Permission dance -------------------------------------- */

	private void requestPermissionForPositionTracking()
	{
		FragmentCompat.requestPermissions(
				this,
				new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
				LOCATION_PERMISSION_REQUEST);
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults)
	{
		if(requestCode == LOCATION_PERMISSION_REQUEST)
		{
			boolean allGranted = true;
			for(int i = 0; i<permissions.length; ++i)
			{
				allGranted &= grantResults[i] == PackageManager.PERMISSION_GRANTED;
			}

			if(findMe.isChecked())
			{
				// user did not grant permission -> uncheck tracking again
				if (!allGranted)
				{
					findMe.setChecked(false);
				}
				else
				{
					tryStartTrackPosition();
				}
			}
		}
	}

	/* --------------------------------- Map and Location --------------------------------------- */

	public void getMapAsync(@NonNull final MapView.OnMapReadyCallback callback)
	{
		getMapAsync(callback, "scene.yaml");
	}

	public void getMapAsync(@NonNull final MapView.OnMapReadyCallback callback,
							@NonNull final String sceneFilePath)
	{
		mapView.getMapAsync(new MapView.OnMapReadyCallback()
		{
			@Override public void onMapReady(MapController map)
			{
				controller = map;

				updateMapTileCacheSize();
				controller.setHttpHandler(mapHttpHandler);
				restoreCameraState();

				callback.onMapReady(controller);
			}
		}, sceneFilePath);
	}

	private void updateMapTileCacheSize()
	{
		int cacheSize = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(Prefs.MAP_TILECACHE,50);

		File cacheDir = getActivity().getExternalCacheDir();
		if (cacheDir != null && cacheDir.exists()) {
			mapHttpHandler.setCache(new File(cacheDir, "tile_cache"), cacheSize * 1024 * 1024);
		}
	}

	private boolean tryStartTrackPosition()
	{
		if(ActivityCompat.checkSelfPermission(getActivity(),
				Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return false;
		if(isTracking) return true;

		Location location = LocationServices.FusedLocationApi.getLastLocation(lostApiClient);
		if (location != null)
		{
			zoomTo(location);
		}

		LocationRequest request = LocationRequest.create()
				.setInterval(2000)
				.setSmallestDisplacement(5)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, this);
		isTracking = true;

		return true;
	}

	private void stopTrackPosition()
	{
		if(!isTracking) return;

		LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, this);

		isTracking = false;
	}

	/* ------------------------------------ LOST ------------------------------------------- */

	@Override public void onLocationChanged(Location location)
	{
		// TODO draw position on map
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
		lostApiClient = new LostApiClient.Builder(activity).build();
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
		if(findMe.isChecked())
		{
			// user might have revoked permissions
			if(!tryStartTrackPosition())
				findMe.setChecked(false);
		}
	}

	@Override public void onPause()
	{
		super.onPause();
		if(mapView != null) mapView.onPause();
		stopTrackPosition();
		saveCameraState();
	}

	@Override public void onStop()
	{
		super.onStop();
		lostApiClient.disconnect();
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
