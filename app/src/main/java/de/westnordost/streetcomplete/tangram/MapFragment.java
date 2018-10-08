package de.westnordost.streetcomplete.tangram;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
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
import de.westnordost.streetcomplete.util.BitmapUtil;
import de.westnordost.streetcomplete.util.DpUtil;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.streetcomplete.util.ViewUtils;

import static android.content.Context.SENSOR_SERVICE;

public class MapFragment extends Fragment implements
	LocationListener, LostApiClient.ConnectionCallbacks, TouchInput.ScaleResponder,
	TouchInput.ShoveResponder, TouchInput.RotateResponder, TouchInput.PanResponder,
	TouchInput.DoubleTapResponder, CompassComponent.Listener, MapController.SceneLoadListener
{
	private CompassComponent compass = new CompassComponent();

	private Marker locationMarker;
	private Marker accuracyMarker;
	private Marker directionMarker;
	private String[] directionMarkerSize;

	private MapView mapView;

	private HttpHandler httpHandler;

	/**
	 * controller to the asynchronously loaded map. Since it is loaded asynchronously, could be
	 * null still at any point!
	 */
	protected MapController controller;

	private LostApiClient lostApiClient;

	private boolean isFollowingPosition;
	private Location lastLocation;
	private boolean zoomedYet;
	private boolean isCompassMode;

	private MapControlsFragment mapControls;

	private String apiKey;

	private boolean isShowingDirection;

	private boolean isMapInitialized;

	private Listener listener;
	public interface Listener
	{
		@AnyThread void onMapOrientation(float rotation, float tilt);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		isMapInitialized = false;

		mapView = view.findViewById(R.id.map);
		TextView mapzenLink = view.findViewById(R.id.mapzenLink);

		mapzenLink.setText(Html.fromHtml(
				String.format(getResources().getString(R.string.map_attribution_mapzen),
				"<a href=\"https://mapzen.com/\">Mapzen</a>"))
		);
		mapzenLink.setMovementMethod(LinkMovementMethod.getInstance());
		mapzenLink.setVisibility(View.GONE);

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState == null)
		{
			getChildFragmentManager().beginTransaction().add(R.id.controls_fragment, new MapControlsFragment()).commit();
		}
	}

	/* --------------------------------- Map and Location --------------------------------------- */

	public void getMapAsync(String apiKey)
	{
		getMapAsync(apiKey, "map_theme/scene.yaml");
	}

	@CallSuper public void getMapAsync(String apiKey, @NonNull final String sceneFilePath)
	{
		this.apiKey = apiKey;

		controller = mapView.getMap(this);
		controller.setRotateResponder(this);
		controller.setShoveResponder(this);
		controller.setScaleResponder(this);
		controller.setPanResponder(this);
		controller.setDoubleTapResponder(this);
		updateMapTileCacheSize();
		controller.setHttpHandler(httpHandler);

		restoreMapState();

		compass.setListener(this);

		isMapInitialized = true;
		tryInitializeMapControls();

		loadScene(sceneFilePath);
	}

	protected void loadScene(String sceneFilePath)
	{
		controller.loadSceneFile(sceneFilePath);
	}

	public void onMapControlsCreated(MapControlsFragment mapControls)
	{
		this.mapControls = mapControls;
		tryInitializeMapControls();
	}

	private void tryInitializeMapControls()
	{
		if(isMapInitialized && mapControls != null)
		{
			mapControls.onMapInitialized();
			onMapOrientation();
		}
	}

	@CallSuper @Override public void onSceneReady(int sceneId, SceneError sceneError)
	{
		if(getActivity() != null)
		{
			initMarkers();
			followPosition();
			showLocation();
			ViewUtils.postOnLayout(getView(), this::updateView);
		}
	}

	private void initMarkers()
	{
		locationMarker = createLocationMarker(3);
		directionMarker = createDirectionMarker(2);
		accuracyMarker = createAccuracyMarker(1);
	}

	private Marker createLocationMarker(int order)
	{
		Marker marker = controller.addMarker();
		BitmapDrawable dot = BitmapUtil.createBitmapDrawableFrom(getResources(), R.drawable.location_dot);
		marker.setStylingFromString("{ style: 'points', color: 'white', size: ["+TextUtils.join(",",sizeInDp(dot))+"], order: 2000, flat: true, collide: false }");
		marker.setDrawable(dot);
		marker.setDrawOrder(order);
		return marker;
	}

	private Marker createDirectionMarker(int order)
	{
		BitmapDrawable directionImg = BitmapUtil.createBitmapDrawableFrom(getResources(), R.drawable.location_direction);
		directionMarkerSize = sizeInDp(directionImg);

		Marker marker = controller.addMarker();
		marker.setDrawable(directionImg);
		marker.setDrawOrder(order);
		return marker;
	}

	private Marker createAccuracyMarker(int order)
	{
		Marker marker = controller.addMarker();
		marker.setDrawable(BitmapUtil.createBitmapDrawableFrom(getResources(), R.drawable.accuracy_circle));
		marker.setDrawOrder(order);
		return marker;
	}

	private String[] sizeInDp(Drawable drawable)
	{
		Context ctx = getContext();
		return new String[]{
			// CSS "px" are in fact density dependent pixels
			DpUtil.toDp(drawable.getIntrinsicWidth(), ctx) + "px",
			DpUtil.toDp(drawable.getIntrinsicHeight(),ctx) + "px"};
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
			return new TileHttpHandler(apiKey, new File(cacheDir, "tile_cache"), cacheSize * 1024L * 1024L);
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
		}
		lostApiClient.disconnect();
	}

	public void setIsFollowingPosition(boolean value)
	{
		isFollowingPosition = value;
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
		onMapOrientation();
		updateView();
		return false;
	}

	@Override public boolean onRotate(float x, float y, float rotation)
	{
		if(!requestUnglueViewFromRotation()) return true;
		onMapOrientation();
		updateView();
		return false;
	}

	private void onMapOrientation()
	{
		onMapOrientation(controller.getRotation(), controller.getTilt());
	}

	private void onMapOrientation(float rotation, float tilt)
	{
		if(mapControls != null) mapControls.onMapOrientation(rotation, tilt);
		listener.onMapOrientation(rotation, tilt);
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
			if(mapControls == null || mapControls.requestUnglueViewFromPosition())
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
			if(mapControls == null || mapControls.requestUnglueViewFromRotation())
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
		compass.setLocation(location);
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

	@AnyThread @Override public void onRotationChanged(float rotation, float tilt)
	{
		// we received an event from the compass, so compass is working - direction can be displayed on screen
		isShowingDirection = true;

		if(directionMarker != null)
		{
			if(!directionMarker.isVisible()) directionMarker.setVisible(true);
			double r = rotation * 180 / Math.PI;
			directionMarker.setStylingFromString(
					"{ style: 'points', color: '#cc536dfe', size: [" +
							TextUtils.join(",", directionMarkerSize) +
							"], order: 2000, collide: false, flat: true, angle: " + r + " }");
		}

		if (isCompassMode)
		{
			float mapRotation = -rotation;
			if (controller.getRotation() != mapRotation)
			{
				controller.setRotation(mapRotation);
			}
			onMapOrientation(mapRotation, controller.getTilt());
		}
	}

	public boolean isShowingDirection() { return isShowingDirection; }

	public boolean isCompassMode()
	{
		return isCompassMode;
	}

	public void setCompassMode(boolean isCompassMode)
	{
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

	public static final String
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

	@Override public void onAttach(Context context)
	{
		super.onAttach(context);
		compass.onCreate(
				(SensorManager) context.getSystemService(SENSOR_SERVICE),
				((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay());
		lostApiClient = new LostApiClient.Builder(context).addConnectionCallbacks(this).build();
		listener = (Listener) context;
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
		compass.onDestroy();
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
		onMapOrientation(rotation, tilt);
	}

	public float getRotation()
	{
		return controller != null ? controller.getRotation() : 0;
	}

	public float getZoom()
	{
		return controller.getZoom();
	}

	public void showMapControls()
	{
		if(mapControls != null) mapControls.showControls();
	}

	public void hideMapControls()
	{
		if(mapControls != null) mapControls.hideControls();
	}
}
