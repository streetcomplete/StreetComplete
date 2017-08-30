package de.westnordost.streetcomplete.tangram;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.tangram.HttpHandler;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapView;
import com.mapzen.tangram.Marker;
import com.mapzen.tangram.SceneError;
import com.mapzen.tangram.TouchInput;

import java.io.File;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

import static android.content.Context.SENSOR_SERVICE;

public class MapFragment extends Fragment implements
		FragmentCompat.OnRequestPermissionsResultCallback, LocationListener,
		LostApiClient.ConnectionCallbacks, TouchInput.ScaleResponder,
		TouchInput.ShoveResponder, TouchInput.RotateResponder,
		TouchInput.PanResponder, TouchInput.DoubleTapResponder, CompassComponent.Listener
{
	private CompassComponent compass = new CompassComponent();

	private Marker locationMarker;
	private Marker accuracyMarker;
	private Marker directionMarker;
	private String[] directionMarkerSize;

	private MapView mapView;

	private HttpHandler httpHandler;

	/** controller to the asynchronously loaded map. Since it is loaded asynchronously, could be
	 *  null still at any point! */
	protected MapController controller;

	private LostApiClient lostApiClient;

	private boolean isFollowingPosition;
	private Location lastLocation;
	private boolean zoomedYet;
	private boolean isCompassMode;

	private MapControlsFragment mapControls;

	private Listener listener;

	private String apiKey;

	private boolean isShowingDirection;

	public interface Listener
	{
		void onMapReady();
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		mapView = view.findViewById(R.id.map);
		TextView mapzenLink = view.findViewById(R.id.mapzenLink);

		mapzenLink.setText(Html.fromHtml(
				String.format(getResources().getString(R.string.map_attribution_mapzen),
				"<a href=\"https://mapzen.com/\">Mapzen</a>"))
		);
		mapzenLink.setMovementMethod(LinkMovementMethod.getInstance());

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		mapControls = (MapControlsFragment) getChildFragmentManager().findFragmentById(R.id.controls_fragment);
		mapControls.setMapFragment(this);
	}

	/* --------------------------------- Map and Location --------------------------------------- */

	public void getMapAsync(String apiKey)
	{
		getMapAsync(apiKey, "scene.yaml");
	}

	public void getMapAsync(String apiKey, @NonNull final String sceneFilePath)
	{
		this.apiKey = apiKey;
		controller = mapView.getMap(new MapController.SceneLoadListener()
		{
			@Override public void onSceneReady(int sceneId, SceneError sceneError)
			{
				initMap();
			}
		});
		controller.loadSceneFile(sceneFilePath);
	}

	protected void initMap()
	{
		updateMapTileCacheSize();
		controller.setHttpHandler(httpHandler);
		restoreMapState();

		controller.setRotateResponder(this);
		controller.setShoveResponder(this);
		controller.setScaleResponder(this);
		controller.setPanResponder(this);
		controller.setDoubleTapResponder(this);

		locationMarker = controller.addMarker();
		BitmapDrawable dot = createBitmapDrawableFrom(R.drawable.location_dot);
		locationMarker.setStylingFromString("{ style: 'points', color: 'white', size: ["+TextUtils.join(",",sizeInDp(dot))+"], order: 2000, flat: true, collide: false }");
		locationMarker.setDrawable(dot);
		locationMarker.setDrawOrder(3);

		directionMarker = controller.addMarker();
		BitmapDrawable directionImg = createBitmapDrawableFrom(R.drawable.location_direction);
		directionMarkerSize = sizeInDp(directionImg);
		directionMarker.setDrawable(directionImg);
		directionMarker.setDrawOrder(2);

		accuracyMarker = controller.addMarker();
		accuracyMarker.setDrawable(createBitmapDrawableFrom(R.drawable.accuracy_circle));
		accuracyMarker.setDrawOrder(1);

		compass.setListener(this);

		showLocation();
		followPosition();

		updateView();

		listener.onMapReady();
		mapControls.onMapReady();
	}

	private String[] sizeInDp(Drawable drawable)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float d = metrics.density;
		return new String[]{
				drawable.getIntrinsicWidth() / d + "px",
				drawable.getIntrinsicHeight() / d + "px"};
	}

	private BitmapDrawable createBitmapDrawableFrom(int resId)
	{
		Drawable drawable = getResources().getDrawable(resId);
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return new BitmapDrawable(getResources(), bitmap);
	}

	private void updateMapTileCacheSize()
	{
		httpHandler = createHttpHandler();
	}

	private HttpHandler createHttpHandler()
	{
		int cacheSize = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(Prefs.MAP_TILECACHE, 50);

		File cacheDir = getContext().getExternalCacheDir();
		if (cacheDir != null && cacheDir.exists())
		{
			return new TileHttpHandler(apiKey, new File(cacheDir, "tile_cache"), cacheSize * 1024 * 1024);
		}
		return new TileHttpHandler(apiKey);
	}

	public void startPositionTracking()
	{
		if(!lostApiClient.isConnected()) lostApiClient.connect();
	}

	public void stopPositionTracking()
	{
		if(locationMarker != null)
		{
			locationMarker.setVisible(false);
			accuracyMarker.setVisible(false);
			directionMarker.setVisible(false);
		}
		lastLocation = null;
		zoomedYet = false;
		isShowingDirection = false;

		if(lostApiClient.isConnected())
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, this);
			lostApiClient.disconnect();
		}
	}

	public void setIsFollowingPosition(boolean value)
	{
		isFollowingPosition = value;
		if(!isFollowingPosition) {
			zoomedYet = false;
			isShowingDirection = false;
			isCompassMode = false;
		}
		followPosition();
	}

	public boolean isFollowingPosition()
	{
		return isFollowingPosition;
	}

	protected void followPosition()
	{
		if(shouldCenterCurrentPosition())
		{
			controller.setPositionEased(new LngLat(lastLocation.getLongitude(), lastLocation.getLatitude()),500);
			if(!zoomedYet)
			{
				zoomedYet = true;
				controller.setZoomEased(19, 500);
			}
			updateView();
		}
	}

	/* -------------------------------- Touch responders --------------------------------------- */

	@Override public boolean onDoubleTap(float x, float y)
	{
		if(requestUnglueViewFromPosition())
		{
			LngLat zoomTo = controller.screenPositionToLngLat(new PointF(x, y));
			controller.setPositionEased(zoomTo, 500);
		}
		controller.setZoomEased(controller.getZoom() + 1.5f, 500);
		updateView();
		return true;
	}

	@Override public boolean onScale(float x, float y, float scale, float velocity)
	{
		updateView();
		return false;
	}

	@Override public boolean onPan(float startX, float startY, float endX, float endY)
	{
		if(!requestUnglueViewFromPosition()) return true;
		updateView();
		return false;
	}

	@Override public boolean onFling(float posX, float posY, float velocityX, float velocityY)
	{
		if(!requestUnglueViewFromPosition()) return true;
		updateView();
		return false;
	}

	@Override public boolean onShove(float distance)
	{
		mapControls.onMapOrientation(controller.getRotation(), controller.getTilt());
		updateView();
		return false;
	}

	@Override public boolean onRotate(float x, float y, float rotation)
	{
		if(!requestUnglueViewFromRotation()) return true;
		mapControls.onMapOrientation(controller.getRotation(), controller.getTilt());
		updateView();
		return false;
	}

	protected void updateView()
	{
		updateAccuracy();
		if(shouldCenterCurrentPosition())
		{
			controller.setPositionEased(new LngLat(lastLocation.getLongitude(), lastLocation.getLatitude()),500);
		}
	}

	protected boolean shouldCenterCurrentPosition()
	{
		return isFollowingPosition && controller != null && lastLocation != null;
	}

	private boolean requestUnglueViewFromPosition()
	{
		if(isFollowingPosition)
		{
			if(mapControls.requestUnglueViewFromPosition())
			{
				setIsFollowingPosition(false);
				setCompassMode(false);
				return true;
			}
			return false;
		}
		return true;
	}

	private boolean requestUnglueViewFromRotation()
	{
		if(isCompassMode)
		{
			if(mapControls.requestUnglueViewFromRotation())
			{
				setCompassMode(false);
				return true;
			}
			return false;
		}
		return true;
	}

	/* ------------------------------------ LOST ------------------------------------------- */

	@Override public void onLocationChanged(Location location)
	{
		lastLocation = location;
		showLocation();
		followPosition();
	}

	private void showLocation()
	{
		if(accuracyMarker != null && locationMarker != null && directionMarker != null && lastLocation != null)
		{
			LngLat pos = new LngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
			locationMarker.setVisible(true);
			accuracyMarker.setVisible(true);
			directionMarker.setVisible(isShowingDirection);
			locationMarker.setPointEased(pos, 1000, MapController.EaseType.CUBIC);
			accuracyMarker.setPointEased(pos, 1000, MapController.EaseType.CUBIC);
			directionMarker.setPointEased(pos, 1000, MapController.EaseType.CUBIC);

			updateAccuracy();
		}
	}

	private void updateAccuracy()
	{
		if(accuracyMarker != null && lastLocation != null && accuracyMarker.isVisible())
		{
			LngLat pos = new LngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
			float size = meters2Pixels(pos, lastLocation.getAccuracy());
			accuracyMarker.setStylingFromString("{ style: 'points', color: 'white', size: ["+size+"px, "+size+"px], order: 2000, flat: true, collide: false }");
		}
	}

	@Override public void onRotationChanged(float rotation, float tilt)
	{
		// we received an event from the compass, so compass is working - direction can be displayed on screen
		isShowingDirection = true;

		if(directionMarker != null)
		{
			directionMarker.setVisible(true);
			double r = rotation * 180 / Math.PI;
			directionMarker.setStylingFromString(
					"{ style: 'points', color: '#cc536dfe', size: [" +
							TextUtils.join(",", directionMarkerSize) +
							"], order: 2000, collide: false, flat: true, angle: " + r + " }");
		}

		if (isCompassMode)
		{
			float mapRotation = -rotation;

			// though the rotation and tilt are already smoothened by the CompassComponent, when it
			// involves rotating the whole view, it feels better for the user if this is smoothened
			// even further
			if (controller.getRotation() != mapRotation)
			{
				controller.setRotationEased(mapRotation, 50);
			}
			mapControls.onMapOrientation(mapRotation, controller.getTilt());
		}
	}

	public boolean isShowingDirection() { return isShowingDirection; }

	public boolean isCompassMode()
	{
		return isCompassMode;
	}

	public void setCompassMode(boolean isCompassMode)
	{
		if(!isFollowingPosition) return;
		this.isCompassMode = isCompassMode;
		if(isCompassMode)
		{
			if(controller != null) controller.setTilt((float) (Math.PI / 5));
		}
	}

	private float meters2Pixels(LngLat at, float meters) {
		LatLon pos0 = TangramConst.toLatLon(at);
		LatLon pos1 = SphericalEarthMath.translate(pos0, meters, 0);
		PointF screenPos0 = controller.lngLatToScreenPosition(at);
		PointF screenPos1 = controller.lngLatToScreenPosition(TangramConst.toLngLat(pos1));
		double tiltFactor = Math.sin(controller.getTilt()/2.0) * Math.cos(controller.getRotation());
		return (float) ((1/(1-Math.abs(tiltFactor))) *
				Math.sqrt(
						Math.pow(screenPos1.y - screenPos0.y,2) +
						Math.pow(screenPos1.x - screenPos0.x,2)
				)
		);
	}

	private static final String
			PREF_ROTATION = "map_rotation",
			PREF_TILT = "map_tilt",
			PREF_ZOOM = "map_zoom",
			PREF_LAT = "map_lat",
			PREF_LON = "map_lon",
			PREF_FOLLOWING = "map_following",
			PREF_COMPASS_MODE = "map_compass_mode";

	private void restoreMapState()
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

		setIsFollowingPosition(prefs.getBoolean(PREF_FOLLOWING, true));
		setCompassMode(prefs.getBoolean(PREF_COMPASS_MODE, false));

		mapControls.onMapOrientation(controller.getRotation(), controller.getTilt());
	}

	private void saveMapState()
	{
		if(controller == null) return;

		SharedPreferences.Editor editor = getActivity().getPreferences(Activity.MODE_PRIVATE).edit();
		editor.putFloat(PREF_ROTATION, controller.getRotation());
		editor.putFloat(PREF_TILT, controller.getTilt());
		editor.putFloat(PREF_ZOOM, controller.getZoom());
		LngLat pos = controller.getPosition();
		editor.putLong(PREF_LAT, Double.doubleToRawLongBits(pos.latitude));
		editor.putLong(PREF_LON, Double.doubleToRawLongBits(pos.longitude));
		editor.putBoolean(PREF_FOLLOWING, isFollowingPosition);
		editor.putBoolean(PREF_COMPASS_MODE, isCompassMode);
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
		listener = (Listener) activity;
		compass.onCreate(
				(SensorManager) activity.getSystemService(SENSOR_SERVICE),
				activity.getWindowManager().getDefaultDisplay());
		lostApiClient = new LostApiClient.Builder(activity).addConnectionCallbacks(this).build();
	}

	@Override public void onStart()
	{
		super.onStart();
		updateMapTileCacheSize();
	}

	@Override public void onResume()
	{
		super.onResume();
		compass.onResume();
		if(mapView != null) mapView.onResume();
	}

	@Override public void onPause()
	{
		super.onPause();
		compass.onPause();
		if(mapView != null) mapView.onPause();
		saveMapState();
	}

	@Override public void onStop()
	{
		super.onStop();
		stopPositionTracking();
	}

	@Override public void onConnected() throws SecurityException
	{
		zoomedYet = false;
		lastLocation = null;

		LocationRequest request = LocationRequest.create()
				.setInterval(2000)
				.setSmallestDisplacement(5)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, this);
	}

	@Override public void onConnectionSuspended()
	{

	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		compass.setListener(null);
		if(mapView != null) mapView.onDestroy();
		controller = null;
		directionMarker = null;
		accuracyMarker = null;
		locationMarker = null;
	}

	@Override public void onLowMemory()
	{
		super.onLowMemory();
		if(mapView != null) mapView.onLowMemory();
	}

	public void zoomIn()
	{
		if(controller == null) return;
		controller.setZoomEased(controller.getZoom() + 1, 500);
		updateView();
	}

	public void zoomOut()
	{
		if(controller == null) return;
		controller.setZoomEased(controller.getZoom() - 1, 500);
		updateView();
	}

	public Location getDisplayedLocation()
	{
		return lastLocation;
	}

	public void setMapOrientation(float rotation, float tilt)
	{
		if(controller == null) return;
		controller.setRotation(rotation);
		controller.setTilt(tilt);
		mapControls.onMapOrientation(rotation, tilt);
	}

	public float getRotation()
	{
		return controller != null ? controller.getRotation() : 0;
	}
}
