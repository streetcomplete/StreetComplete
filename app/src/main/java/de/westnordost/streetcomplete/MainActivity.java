package de.westnordost.streetcomplete;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestChangesUploadService;
import de.westnordost.streetcomplete.data.QuestController;
import de.westnordost.streetcomplete.data.download.MobileDataAutoDownloadStrategy;
import de.westnordost.streetcomplete.data.download.QuestAutoDownloadStrategy;
import de.westnordost.streetcomplete.data.download.QuestDownloadProgressListener;
import de.westnordost.streetcomplete.data.download.QuestDownloadService;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.download.WifiAutoDownloadStrategy;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuest;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.OsmQuestAnswerListener;
import de.westnordost.streetcomplete.quests.QuestAnswerComponent;
import de.westnordost.streetcomplete.settings.SettingsActivity;
import de.westnordost.streetcomplete.tangram.QuestsMapFragment;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class MainActivity extends AppCompatActivity implements
		OsmQuestAnswerListener, VisibleQuestListener, QuestsMapFragment.Listener, LocationListener
{
	private static final String TAG_AUTO_DOWNLOAD = "AutoQuestDownload";

	private static final int
			LOCATION_PERMISSION_REQUEST = 101,
			REQUEST_CHECK_SETTINGS = 102;

	@Inject QuestController questController;
	@Inject MobileDataAutoDownloadStrategy mobileDataDownloadStrategy;
	@Inject WifiAutoDownloadStrategy wifiDownloadStrategy;

	private QuestsMapFragment mapFragment;

	private Long clickedQuestId = null;
	private QuestGroup clickedQuestGroup = null;

	private ProgressBar progressBar;

	private LostApiClient lostApiClient;
	private LatLon lastAutoDownloadPos;

	private boolean downloadServiceIsBound;
	private QuestDownloadService.Interface downloadService;
	private ServiceConnection downloadServiceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			downloadService = (QuestDownloadService.Interface) service;
			downloadService.setProgressListener(downloadProgressListener);
			downloadService.stopForeground();
		}

		public void onServiceDisconnected(ComponentName className)
		{
			downloadService = null;
		}
	};

	private final BroadcastReceiver uploadChangesErrorReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			Toast.makeText(MainActivity.this, R.string.upload_error, Toast.LENGTH_LONG).show();
		}
	};

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		lostApiClient = new LostApiClient.Builder(this).addConnectionCallbacks(
				new LostApiClient.ConnectionCallbacks()
				{
					boolean alreadyCalled = false;

					@Override
					public void onConnected()
					{
						if(alreadyCalled) return; // TODO https://github.com/mapzen/lost/issues/138
						initLocationTracking();
						alreadyCalled = true;
					}

					@Override
					public void onConnectionSuspended() {}
				}).build();

		progressBar = (ProgressBar) findViewById(R.id.download_progress);
		progressBar.setMax(1000);

		mapFragment = (QuestsMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.getMapAsync();
	}

	@Override public void onStart()
	{
		super.onStart();

		lostApiClient.connect();

		registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);

		IntentFilter uploadChangesErrFilter = new IntentFilter(QuestChangesUploadService.ACTION_ERROR);
		localBroadcaster.registerReceiver(uploadChangesErrorReceiver, uploadChangesErrFilter);

		questController.onStart(this);
		QuestAutoDownloadStrategy newStrategy = isConnectedToWifi() ? wifiDownloadStrategy : mobileDataDownloadStrategy;
		questController.setDownloadStrategy(newStrategy);

		downloadServiceIsBound = bindService(
				new Intent(this, QuestDownloadService.class),
				downloadServiceConnection, BIND_AUTO_CREATE);
	}

	@Override public void onResume()
	{
		super.onResume();
	}

	@Override public void onPause()
	{
		super.onPause();
	}

	@Override public void onStop()
	{
		super.onStop();

		unregisterReceiver(wifiReceiver);

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);

		localBroadcaster.unregisterReceiver(uploadChangesErrorReceiver);

		LocationServices.FusedLocationApi.removeLocationUpdates(lostApiClient, this);
		lostApiClient.disconnect();

		questController.onStop();

		if (downloadServiceIsBound) unbindService(downloadServiceConnection);
		if (downloadService != null)
		{
			downloadService.setProgressListener(null);
			downloadService.startForeground();
			// since we unbound from the service, we won't get the onFinished call. But we will get
			// the onStarted call when we return to this activity when the service is rebound
			progressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		switch (id)
		{
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;

			case R.id.action_download:
				downloadDisplayedArea();
				return true;

			case R.id.action_upload:

				return true;

		}

		return super.onOptionsItemSelected(item);
	}

	private void downloadDisplayedArea()
	{
		BoundingBox displayArea = mapFragment.getDisplayedArea();
		if (displayArea == null)
		{
			Toast.makeText(this, R.string.cannot_find_bbox, Toast.LENGTH_LONG).show();
		}
		else
		{
			final BoundingBox enclosingBBox = SlippyMapMath.asBoundingBoxOfEnclosingTiles(
					displayArea, ApplicationConstants.QUEST_TILE_ZOOM);
			double areaInSqKm = SphericalEarthMath.enclosedArea(enclosingBBox) / 1000000;
			if (areaInSqKm > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM)
			{
				Toast.makeText(this, R.string.download_area_too_big, Toast.LENGTH_LONG).show();
			}
			else
			{
				if (questController.isManualDownloadRunning())
				{
					DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							downloadAreaConfirmed(enclosingBBox);
						}
					};

					new AlertDialog.Builder(this)
							.setMessage(R.string.confirmation_cancel_prev_download_title)
							.setPositiveButton(android.R.string.ok, onYes)
							.setNegativeButton(android.R.string.cancel, null)
							.show();
				}
				else
				{
					downloadAreaConfirmed(enclosingBBox);
				}
			}
		}
	}

	private void downloadAreaConfirmed(BoundingBox bbox)
	{
		double areaInSqKm = SphericalEarthMath.enclosedArea(bbox) / 1000000;
		// below a certain threshold, it does not make sense to download, so let's enlarge it
		if (areaInSqKm < ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM)
		{
			LatLon pos = mapFragment.getPosition();
			if (pos != null)
			{
				questController.manualDownload(SphericalEarthMath.enclosingBoundingBox(pos,
						ApplicationConstants.MIN_DOWNLOADABLE_RADIUS_IN_METERS));
			}
		}
		else
		{
			questController.manualDownload(bbox);
		}
	}


	/* ------------------------------------ Progress bar  --------------------------------------- */

	private final QuestDownloadProgressListener downloadProgressListener
			= new QuestDownloadProgressListener()
	{
		@Override public void onStarted()
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(0);

					// a manual download does not need a notification, the user clicked it himself
					// but for the auto download, it's nice
					if(!downloadService.isManualDownloadRunning())
					{
						Toast.makeText(
								MainActivity.this,
								R.string.now_downloading_toast,
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		}

		@Override public void onProgress(final float progress)
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					progressBar.setProgress((int) (1000 * progress));
				}
			});
		}

		@Override public void onError(final Exception e)
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					int errorResourceId = R.string.download_error;
					Toast.makeText(MainActivity.this, errorResourceId, Toast.LENGTH_LONG).show();
				}
			});
		}

		@Override public void onFinished()
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					progressBar.setVisibility(View.INVISIBLE);

					// after downloading, regardless if triggered manually or automatically, the
					// auto downloader should check whether there are enough quests in the vicinity now
					triggerAutoDownloadFor(lastAutoDownloadPos);
				}
			});
		}
	};

	/* ------------ Managing bottom sheet (quest details) and interaction with map  ------------- */

	private final static String BOTTOM_SHEET = "bottom_sheet";

	@Override public void onBackPressed()
	{
		confirmDiscardChangesIfAny(new Runnable()
		{
			@Override public void run()
			{
				backAndCleanGeometry();
			}
		});
	}

	/* ------------- OsmQuestAnswerListener ------------- */

	@Override public void onAnsweredQuest(long questId, QuestGroup group, Bundle answer)
	{
		questController.solveQuest(questId, group, answer);
		// amount of quests is reduced -> check if redownloding now makes sense
		triggerAutoDownloadFor(lastAutoDownloadPos);
	}

	@Override public void onLeaveNote(long questId, QuestGroup group, String note)
	{
		questController.createNote(questId, note);
		// amount of quests is reduced -> check if redownloding now makes sense
		triggerAutoDownloadFor(lastAutoDownloadPos);
	}

	@Override public void onSkippedQuest(long questId, QuestGroup group)
	{
		questController.hideQuest(questId, group);
		// amount of quests is reduced -> check if redownloding now makes sense
		triggerAutoDownloadFor(lastAutoDownloadPos);
	}

	/* ------------- VisibleQuestListener ------------- */

	@Override public void onQuestCreated(OsmQuest quest, Element element)
	{
		onQuestCreated(quest, QuestGroup.OSM, element);
	}

	@Override public void onQuestCreated(OsmNoteQuest quest)
	{
		onQuestCreated(quest, QuestGroup.OSM_NOTE, null);
	}

	@AnyThread private synchronized void onQuestCreated(final Quest quest, final QuestGroup group,
														final Element element)
	{
		if (clickedQuestId != null && quest.getId().equals(clickedQuestId) && group == clickedQuestGroup)
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					showQuestDetails(quest, group, element, false);
				}
			});

			clickedQuestId = null;
			clickedQuestGroup = null;
		} else if (isQuestDetailsCurrentlyDisplayedFor(quest.getId(), group))
		{
			mapFragment.addQuestGeometry(quest.getGeometry());
		}
		mapFragment.addQuest(quest, group);
	}

	@Override public void onOsmQuestRemoved(long questId)
	{
		onQuestRemoved(QuestGroup.OSM, questId);
	}

	@Override public void onNoteQuestRemoved(long questId)
	{
		onQuestRemoved(QuestGroup.OSM_NOTE, questId);
	}

	@AnyThread public synchronized void onQuestRemoved(QuestGroup group, long questId)
	{
		if (isQuestDetailsCurrentlyDisplayedFor(questId, group))
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					closeQuestDetails();
				}
			});
		}
		mapFragment.removeQuest(group, questId);
	}

	@UiThread private void closeQuestDetails()
	{
		getFragmentManager().popBackStack(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		mapFragment.removeQuestGeometry();

		// sometimes the keyboard fails to close
		View view = this.getCurrentFocus();
		if (view != null)
		{
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	private boolean isQuestDetailsCurrentlyDisplayedFor(long questId, QuestGroup group)
	{
		AbstractQuestAnswerFragment currentFragment = getQuestDetailsFragment();
		return currentFragment != null
				&& currentFragment.getQuestId() == questId
				&& currentFragment.getQuestGroup() == group;
	}

	@UiThread private void showQuestDetails(final Quest quest, final QuestGroup group,
											final Element element, boolean confirmed)
	{
		if (isQuestDetailsCurrentlyDisplayedFor(quest.getId(), group)) return;

		if (getQuestDetailsFragment() != null)
		{
			if (!confirmed)
			{
				confirmDiscardChangesIfAny(
						new Runnable()
						{
							@Override public void run()
							{
								showQuestDetails(quest, group, element, true);
							}
						}
				);
				return;
			}

			closeQuestDetails();
		}

		mapFragment.addQuestGeometry(quest.getGeometry());

		AbstractQuestAnswerFragment f = quest.getType().getForm();
		Bundle args = QuestAnswerComponent.createArguments(quest.getId(), group);
		if (group == QuestGroup.OSM)
		{
			args.putSerializable(AbstractQuestAnswerFragment.ELEMENT, (OsmElement) element);
		}
		f.setArguments(args);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(
				R.animator.enter_from_bottom, R.animator.exit_to_bottom,
				R.animator.enter_from_bottom, R.animator.exit_to_bottom);
		ft.add(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET);
		ft.addToBackStack(BOTTOM_SHEET);
		ft.commit();
	}

	private AbstractQuestAnswerFragment getQuestDetailsFragment()
	{
		return (AbstractQuestAnswerFragment) getFragmentManager().findFragmentByTag(BOTTOM_SHEET);
	}

	private void backAndCleanGeometry()
	{
		mapFragment.removeQuestGeometry();
		super.onBackPressed();
	}

	/** @return true if an action has been taken (run r or show confirmation dialog) */
	@UiThread private boolean confirmDiscardChangesIfAny(final Runnable r)
	{
		AbstractQuestAnswerFragment f = getQuestDetailsFragment();
		if (f == null || !f.hasChanges())
		{
			r.run();
		} else
		{
			DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					r.run();
				}
			};
			new AlertDialog.Builder(this)
					.setMessage(R.string.confirmation_discard_title)
					.setPositiveButton(R.string.confirmation_discard_positive, onYes)
					.setNegativeButton(R.string.confirmation_discard_negative, null)
					.show();
		}
		return f != null;
	}

	/* ---------- QuestsMapFragment.Listener ---------- */

	@Override public void onMapReady()
	{

	}

	@Override public void onFirstInView(BoundingBox bbox)
	{
		questController.retrieve(bbox);
	}

	@Override public void onClickedQuest(QuestGroup questGroup, Long questId)
	{
		clickedQuestId = questId;
		clickedQuestGroup = questGroup;
		questController.retrieve(questGroup, questId);
	}

	@Override public void onClickedMapAt(@Nullable LatLon position)
	{
		confirmDiscardChangesIfAny(new Runnable()
		{
			@Override public void run()
			{
				closeQuestDetails();
			}
		});
	}


	/* ---------- Location listener ---------- */

	@Override public void onLocationChanged(Location location)
	{
		triggerAutoDownloadFor(new OsmLatLon(location.getLatitude(), location.getLongitude()));
	}

	@Override public void onProviderEnabled(String provider) {}
	@Override public void onProviderDisabled(String provider) {}

	private void initLocationTracking()
	{
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(
					this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
					LOCATION_PERMISSION_REQUEST);
			return;
		}

		LocationRequest request = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setSmallestDisplacement(500)
				.setInterval(3*60*1000); // 3 minutes

		Location location = LocationServices.FusedLocationApi.getLastLocation(lostApiClient);
		if(location != null)
		{
			this.onLocationChanged(location);
		}

		LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, this);
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull
			String[] permissions, @NonNull int[] grantResults)
	{
		if(requestCode == LOCATION_PERMISSION_REQUEST)
		{
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				initLocationTracking(); // retry then...
			}
			else
			{
				DialogInterface.OnClickListener onRetry = new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						initLocationTracking();
					}
				};
				new AlertDialog.Builder(this)
						.setMessage(R.string.no_location_permission_warning)
						.setPositiveButton(R.string.retry, onRetry)
						.setNegativeButton(android.R.string.cancel, null)
						.show();
			}
		}
	}

//TODO https://github.com/mapzen/lost/issues/140
/*
	private void requestLocationSettingsToBeOn()
	{
		LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
				lostApiClient, new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build());
		result.setResultCallback(new ResultCallback<LocationSettingsResult>()
		{
			@Override public void onResult(@NonNull LocationSettingsResult result)
			{
				final Status status = result.getStatus();
				switch (status.getStatusCode())
				{
					case Status.SUCCESS:
						initLocationTracking();
						break;
					case Status.RESOLUTION_REQUIRED:

						DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								try
								{
									status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
								}
								catch (IntentSender.SendIntentException e)
								{
									e.printStackTrace();
								}
							}
						};
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.turn_on_location_request)
								.setPositiveButton(android.R.string.yes, onYes)
								.setNegativeButton(android.R.string.no, null)
								.show();

						break;
					case Status.SETTINGS_CHANGE_UNAVAILABLE:
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.no_location_message)
								.show();
						break;
				}
			}
		});
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case REQUEST_CHECK_SETTINGS:
				switch (resultCode) {
					case Activity.RESULT_OK:
						initLocationTracking();
						break;
					case Activity.RESULT_CANCELED:
						new AlertDialog.Builder(MainActivity.this)
								.setMessage(R.string.no_location_message)
								.show();
						break;
					default:
						break;
				}
				break;
		}
	}
*/
	private void triggerAutoDownloadFor(LatLon pos)
	{
		if(pos == null) return;

		Log.i(TAG_AUTO_DOWNLOAD, "Checking whether to automatically download new quests at "
				+ pos.getLatitude() + "," + pos.getLongitude());
		questController.autoDownload(pos);
		lastAutoDownloadPos = pos;
	}

	// ---------------------------------------------------------------------------------------------

	private boolean isInternetAvailable()
	{
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private boolean isConnectedToWifi()
	{
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected() &&
				activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
	}

	private final BroadcastReceiver wifiReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			boolean isWifi = info.isConnected();

			QuestAutoDownloadStrategy newStrategy = isWifi ? wifiDownloadStrategy : mobileDataDownloadStrategy;
			if(newStrategy == questController.getDownloadStrategy()) return;

			Log.i(TAG_AUTO_DOWNLOAD, "Setting download strategy to " + (isWifi ? "Wifi" : "Mobile Data"));
			questController.setDownloadStrategy(newStrategy);
			triggerAutoDownloadFor(lastAutoDownloadPos);
		}
	};
}
