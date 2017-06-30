package de.westnordost.streetcomplete;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapzen.android.lost.api.LocationRequest;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import de.westnordost.osmapi.common.errors.OsmConnectionException;
import de.westnordost.streetcomplete.about.AboutActivity;
import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestAutoSyncer;
import de.westnordost.streetcomplete.data.QuestChangesUploadService;
import de.westnordost.streetcomplete.data.QuestController;
import de.westnordost.streetcomplete.data.download.QuestDownloadProgressListener;
import de.westnordost.streetcomplete.data.download.QuestDownloadService;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.meta.CountryInfos;
import de.westnordost.streetcomplete.location.LocationRequestFragment;
import de.westnordost.streetcomplete.location.LocationUtil;
import de.westnordost.streetcomplete.location.SingleLocationRequest;
import de.westnordost.streetcomplete.oauth.OAuthPrefs;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.OsmQuestAnswerListener;
import de.westnordost.streetcomplete.quests.QuestAnswerComponent;
import de.westnordost.streetcomplete.quests.FindQuestSourceComponent;
import de.westnordost.streetcomplete.settings.SettingsActivity;
import de.westnordost.streetcomplete.statistics.AnswersCounter;
import de.westnordost.streetcomplete.location.LocationState;
import de.westnordost.streetcomplete.location.LocationStateButton;
import de.westnordost.streetcomplete.tangram.MapFragment;
import de.westnordost.streetcomplete.tangram.QuestsMapFragment;
import de.westnordost.streetcomplete.tools.CrashReportExceptionHandler;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

import static android.location.LocationManager.PROVIDERS_CHANGED_ACTION;
import static de.westnordost.streetcomplete.location.LocationUtil.MODE_CHANGED;

public class MainActivity extends AppCompatActivity implements
		OsmQuestAnswerListener, VisibleQuestListener, QuestsMapFragment.Listener,
		MapFragment.Listener, LocationRequestFragment.LocationRequestListener
{
	@Inject CrashReportExceptionHandler crashReportExceptionHandler;

	@Inject LocationRequestFragment locationRequestFragment;
	@Inject QuestAutoSyncer questAutoSyncer;

	@Inject QuestController questController;

	@Inject SharedPreferences prefs;
	@Inject OAuthPrefs oAuth;

	@Inject CountryInfos countryInfos;
	@Inject FindQuestSourceComponent questSource;

	// per application start settings
	private static boolean isFollowingPosition = true;
	private static boolean hasAskedForLocation = false;
	private static boolean dontShowRequestAuthorizationAgain = false;

	private QuestsMapFragment mapFragment;
	private LocationStateButton trackingButton;
	private SingleLocationRequest singleLocationRequest;
	private Location lastLocation;

	private Long clickedQuestId = null;
	private QuestGroup clickedQuestGroup = null;

	private ProgressBar progressBar;
	private AnswersCounter answersCounter;

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
			Exception e = (Exception) intent.getSerializableExtra(QuestChangesUploadService.EXCEPTION);

			if(intent.getBooleanExtra(QuestChangesUploadService.IS_AUTH_FAILED, false))
			{
				// delete secret in case it failed while already having a token -> token is invalid
				oAuth.saveConsumer(null);
				requestOAuthorized();
			}
			else if(intent.getBooleanExtra(QuestChangesUploadService.IS_CONNECTION_ERROR, false))
			{
				// a 5xx error is not the fault of this app. Nothing we can do about it, so
				// just notify the user
				Toast.makeText(MainActivity.this, R.string.upload_server_error, Toast.LENGTH_LONG).show();
			}
			else if(intent.getBooleanExtra(QuestChangesUploadService.IS_VERSION_BANNED, false))
			{
				new AlertDialogBuilder(MainActivity.this)
						.setMessage(R.string.version_banned_message)
						.setPositiveButton(android.R.string.ok, null)
						.show();
			}
			else if(e != null)// any other error
			{
				crashReportExceptionHandler.askUserToSendErrorReport(
						MainActivity.this, R.string.upload_error, e);
			}
		}
	};
	private final BroadcastReceiver uploadChangesFinishedReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			answersCounter.update();
		}
	};

	private BroadcastReceiver locationAvailabilityReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			updateLocationAvailability();
		}
	};

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		crashReportExceptionHandler.askUserToSendCrashReportIfExists(this);

		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		if(prefs.getBoolean(Prefs.KEEP_SCREEN_ON, false))
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		questController.onCreate();

		answersCounter = (AnswersCounter) toolbar.findViewById(R.id.answersCounter);

		questSource.onCreate(this);

		getSupportFragmentManager().beginTransaction()
				.add(locationRequestFragment, LocationRequestFragment.class.getSimpleName())
				.commit();

		singleLocationRequest = new SingleLocationRequest(this);

		progressBar = (ProgressBar) findViewById(R.id.download_progress);
		progressBar.setMax(1000);

		mapFragment = (QuestsMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.setQuestYOffsets(50,
				getResources().getDimensionPixelSize(R.dimen.quest_bottom_sheet_peek_height));

		mapFragment.getMapAsync(BuildConfig.MAPZEN_API_KEY != null ?
				BuildConfig.MAPZEN_API_KEY :
				new String(new char[]{118,101,99,116,111,114,45,116,105,108,101,115,45,102,75,85,99,117,65,74}));

		trackingButton = (LocationStateButton) findViewById(R.id.gps_tracking);
		trackingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				if(trackingButton.getState().isEnabled())
				{
					boolean isFollowing = mapFragment.isFollowingPosition();
					setIsFollowingPosition(!isFollowing);
				}
				else
				{
					locationRequestFragment.startRequest();
				}
			}
		});
        trackingButton.setActivated(isFollowingPosition);

		ImageButton zoomInButton = (ImageButton) findViewById(R.id.zoom_in);
		zoomInButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				mapFragment.zoomIn();
			}
		});
		ImageButton zoomOutButton = (ImageButton) findViewById(R.id.zoom_out);
		zoomOutButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				mapFragment.zoomOut();
			}
		});
	}

	@Override public void onStart()
	{
		super.onStart();

		answersCounter.update();

		String name = LocationUtil.isNewLocationApi() ? MODE_CHANGED : PROVIDERS_CHANGED_ACTION;
		registerReceiver(locationAvailabilityReceiver, new IntentFilter(name));

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);

		IntentFilter uploadChangesErrFilter = new IntentFilter(QuestChangesUploadService.ACTION_ERROR);
		localBroadcaster.registerReceiver(uploadChangesErrorReceiver, uploadChangesErrFilter);

		IntentFilter uploadChangesFinishedFilter = new IntentFilter(QuestChangesUploadService.ACTION_FINISHED);
		localBroadcaster.registerReceiver(uploadChangesFinishedReceiver, uploadChangesFinishedFilter);

		questController.onStart(this);
		questAutoSyncer.onStart();

		progressBar.setAlpha(0f);
		downloadServiceIsBound = bindService(
				new Intent(this, QuestDownloadService.class),
				downloadServiceConnection, BIND_AUTO_CREATE);

		if(!hasAskedForLocation)
		{
			locationRequestFragment.startRequest();
		}
		else if(ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			updateLocationAvailability();
		}
	}

	@Override public void onStop()
	{
		super.onStop();

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);
		localBroadcaster.unregisterReceiver(uploadChangesErrorReceiver);
		localBroadcaster.unregisterReceiver(uploadChangesFinishedReceiver);

		unregisterReceiver(locationAvailabilityReceiver);

		questController.onStop();
		questAutoSyncer.onStop();

		isFollowingPosition = trackingButton.isActivated();

		if (downloadServiceIsBound) unbindService(downloadServiceConnection);
		if (downloadService != null)
		{
			downloadService.setProgressListener(null);
			downloadService.startForeground();
			// since we unbound from the service, we won't get the onFinished call. But we will get
			// the onStarted call when we return to this activity when the service is rebound
			progressBar.setAlpha(0f);
		}
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		questController.onDestroy();
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
			case R.id.action_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			case R.id.action_download:
				if(isConnected()) downloadDisplayedArea();
				else              Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show();
				return true;
			case R.id.action_upload:
				if(isConnected()) uploadChanges();
				else              Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show();
				return true;

		}

		return super.onOptionsItemSelected(item);
	}

	private void uploadChanges()
	{
		// because the app should ask for permission even if there is nothing to upload right now
		if(!oAuth.isAuthorized())
		{
			requestOAuthorized();
		}
		else
		{
			questController.upload();
		}
	}

	private void requestOAuthorized()
	{
		if(dontShowRequestAuthorizationAgain) return;

		View inner = LayoutInflater.from(this).inflate(
				R.layout.authorize_now_dialog_layout, null, false);
		final CheckBox checkBox = (CheckBox) inner.findViewById(R.id.checkBoxDontShowAgain);

		new AlertDialogBuilder(this)
				.setView(inner)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
						intent.putExtra(SettingsActivity.EXTRA_LAUNCH_AUTH, true);
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.later, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						dontShowRequestAuthorizationAgain = checkBox.isChecked();
					}
				}).show();
	}

	private void downloadDisplayedArea()
	{
		BoundingBox displayArea;
		if ((displayArea = mapFragment.getDisplayedArea(0,0)) == null)
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
				if (questController.isPriorityDownloadRunning())
				{
					DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							downloadAreaConfirmed(enclosingBBox);
						}
					};

					new AlertDialogBuilder(this)
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
				questController.download(SphericalEarthMath.enclosingBoundingBox(pos,
						ApplicationConstants.MIN_DOWNLOADABLE_RADIUS_IN_METERS), null, true);
			}
		}
		else
		{
			questController.download(bbox, null, true);
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
					ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(progressBar, View.ALPHA, 1f);
					fadeInAnimator.start();
					progressBar.setProgress(0);

					Toast.makeText(
							MainActivity.this,
							R.string.now_downloading_toast,
							Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override public void onProgress(final float progress)
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					int intProgress = (int) (1000 * progress);
					ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", intProgress);
					progressAnimator.setDuration(1000);
					progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
					progressAnimator.start();
				}
			});
		}

		@Override public void onError(final Exception e)
		{
			// a 5xx error is not the fault of this app. Nothing we can do about it, so it does not
			// make sense to send an error report. Just notify the user
			if(e instanceof OsmConnectionException)
			{
				Toast.makeText(MainActivity.this, R.string.download_server_error, Toast.LENGTH_LONG).show();
			}
			else
			{
				crashReportExceptionHandler.askUserToSendErrorReport(MainActivity.this, R.string.download_error, e);
			}
		}

		@Override public void onSuccess()
		{
			// after downloading, regardless if triggered manually or automatically, the
			// auto downloader should check whether there are enough quests in the vicinity now
			questAutoSyncer.triggerAutoDownload();
		}

		@Override public void onFinished()
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(progressBar, View.ALPHA, 0f);
					fadeOutAnimator.setDuration(1000);
					fadeOutAnimator.start();
				}
			});
		}

		@Override public void onNotStarted()
		{
			if(downloadService.currentDownloadHasPriority())
			{
				Toast.makeText(MainActivity.this, R.string.nothing_more_to_download, Toast.LENGTH_SHORT).show();
			}
		}
	};

	/* ------------ Managing bottom sheet (quest details) and interaction with map  ------------- */

	private final static String BOTTOM_SHEET = "bottom_sheet";

	@Override public void onBackPressed()
	{
		AbstractQuestAnswerFragment f = getQuestDetailsFragment();
		if(f != null)
		{
			f.onClickClose(new Runnable()
			{
				@Override public void run()
				{
					mapFragment.removeQuestGeometry();
					MainActivity.super.onBackPressed();
				}
			});
		} else {
			super.onBackPressed();
		}
	}

	/* ------------- OsmQuestAnswerListener ------------- */

	@Override public void onAnsweredQuest(final long questId, final QuestGroup group, final Bundle answer)
	{
		// line between location now and location when the form was opened
		Location[] locations = new Location[]{ lastLocation, mapFragment.getDisplayedLocation() };
		questSource.findSource(questId, group, locations, new FindQuestSourceComponent.Listener()
		{
			@Override public void onFindQuestSourceResult(String source)
			{
				closeQuestDetailsFor(questId, group);
				answersCounter.answeredQuest(source);
				questController.solveQuest(questId, group, answer, source);
			}
		});
	}

	@Override public void onLeaveNote(long questId, QuestGroup group, String questTitle, String note)
	{
		closeQuestDetailsFor(questId, group);
		questController.createNote(questId, questTitle, note);
	}

	@Override public void onSkippedQuest(long questId, QuestGroup group)
	{
		closeQuestDetailsFor(questId, group);
		questController.hideQuest(questId, group);
	}

	private void closeQuestDetailsFor(long questId, QuestGroup group)
	{
		if (isQuestDetailsCurrentlyDisplayedFor(questId, group))
		{
			closeQuestDetails();
		}
	}

	/* ------------- VisibleQuestListener ------------- */

	@AnyThread
	@Override public void onQuestsCreated(Collection<? extends Quest> quests, QuestGroup group)
	{
		mapFragment.addQuests(quests, group);
		// to recreate element geometry of selected quest (if any) after recreation of activity
		if(getQuestDetailsFragment() != null)
		{
			for (Quest q : quests)
			{
				if (isQuestDetailsCurrentlyDisplayedFor(q.getId(), group))
				{
					questController.retrieve(group, q.getId());
					return;
				}
			}
		}
	}

	@AnyThread
	@Override public synchronized void onQuestCreated(final Quest quest, final QuestGroup group,
														final Element element)
	{
		if (clickedQuestId != null && quest.getId().equals(clickedQuestId) && group == clickedQuestGroup)
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					requestShowQuestDetails(quest, group, element);
				}
			});

			clickedQuestId = null;
			clickedQuestGroup = null;
		} else if (isQuestDetailsCurrentlyDisplayedFor(quest.getId(), group))
		{
			mapFragment.addQuestGeometry(quest.getGeometry());
		}
	}

	@AnyThread
	@Override public synchronized void onQuestsRemoved(Collection<Long> questIds, QuestGroup group)
	{
		removeQuests(questIds, group);
	}

	@AnyThread
	@Override public synchronized void onQuestSolved(long questId, QuestGroup group)
	{
		questAutoSyncer.triggerAutoUpload();
		removeQuests(Collections.singletonList(questId), group);
	}

	private void removeQuests(Collection<Long> questIds, QuestGroup group)
	{
		// amount of quests is reduced -> check if redownloding now makes sense
		questAutoSyncer.triggerAutoDownload();

		for(long questId : questIds)
		{
			if (!isQuestDetailsCurrentlyDisplayedFor(questId, group)) continue;

			runOnUiThread(new Runnable() { @Override public void run() { closeQuestDetails(); }});
			break;
		}

		mapFragment.removeQuests(questIds, group);
	}

	@UiThread private void closeQuestDetails()
	{
		// #285: This method may be called after the user tapped the home button from removeQuests().
		// At this point, it wouldn't be legal to pop the fragment back stack etc.
		// I am not entirely sure if checking for these things will solve #285 though
		// some more info here http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
		if(isDestroyed() || isFinishing() || isChangingConfigurations()) return;

		// manually close the keyboard before popping the fragment
		View view = this.getCurrentFocus();
		if (view != null)
		{
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}

		getFragmentManager().popBackStackImmediate(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		mapFragment.removeQuestGeometry();
	}

	private boolean isQuestDetailsCurrentlyDisplayedFor(long questId, QuestGroup group)
	{
		AbstractQuestAnswerFragment currentFragment = getQuestDetailsFragment();
		return currentFragment != null
				&& currentFragment.getQuestId() == questId
				&& currentFragment.getQuestGroup() == group;
	}

	@UiThread private void requestShowQuestDetails(final Quest quest, final QuestGroup group,
											final Element element)
	{
		if (isQuestDetailsCurrentlyDisplayedFor(quest.getId(), group)) return;

		AbstractQuestAnswerFragment f = getQuestDetailsFragment();
		if (f != null)
		{
			f.onClickClose(new Runnable()
			{
				@Override public void run()
				{
					showQuestDetails(quest, group, element);
				}
			});
		} else {
			showQuestDetails(quest, group, element);
		}
	}

	@UiThread private void showQuestDetails(final Quest quest, final QuestGroup group,
											final Element element)
	{
		if(getQuestDetailsFragment() != null)
		{
			closeQuestDetails();
		}

		lastLocation = mapFragment.getDisplayedLocation();
		mapFragment.addQuestGeometry(quest.getGeometry());

		AbstractQuestAnswerFragment f = quest.getType().createForm();
		Bundle args = QuestAnswerComponent.createArguments(quest.getId(), group);
		if (group == QuestGroup.OSM)
		{
			args.putSerializable(AbstractQuestAnswerFragment.ARG_ELEMENT, (OsmElement) element);
		}
		LatLon latLon = quest.getGeometry().center;
		args.putSerializable(AbstractQuestAnswerFragment.ARG_COUNTRY_INFO,
				countryInfos.get(latLon.getLongitude(), latLon.getLatitude()));
		f.setArguments(args);

		android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
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

	/* ---------- QuestsMapFragment.Listener ---------- */

	@Override public void onMapReady()
	{

	}

	@Override public void onFirstInView(BoundingBox bbox)
	{
		questController.retrieve(bbox);
	}

	@Override public void onUnglueViewFromPosition()
	{
		trackingButton.setActivated(false);
	}

	@Override public void onClickedQuest(QuestGroup questGroup, Long questId)
	{
		clickedQuestId = questId;
		clickedQuestGroup = questGroup;
		questController.retrieve(questGroup, questId);
	}

	@Override public void onClickedMapAt(@Nullable LatLon position)
	{
		AbstractQuestAnswerFragment f = getQuestDetailsFragment();
		if(f != null)
		{
			f.onClickClose(new Runnable()
			{
				@Override public void run()
				{
					mapFragment.removeQuestGeometry();
					closeQuestDetails();
				}
			});
		}
	}

	/* ---------- Location listener ---------- */

	private void updateLocationAvailability()
	{
		if(LocationUtil.isLocationSettingsOn(this))
		{
			onLocationIsEnabled();
		}
		else
		{
			onLocationIsDisabled();
		}
	}

	private void onLocationIsEnabled()
	{
		trackingButton.setState(LocationState.SEARCHING);
		mapFragment.setIsFollowingPosition(trackingButton.isActivated());
		mapFragment.startPositionTracking();
		questAutoSyncer.startPositionTracking();
		singleLocationRequest.startRequest(LocationRequest.PRIORITY_HIGH_ACCURACY,
				new SingleLocationRequest.Callback()
				{
					@Override public void onLocation(Location location)
					{
						trackingButton.setState(LocationState.UPDATING);
					}
				});
	}

	private void onLocationIsDisabled()
	{
		trackingButton.setState(LocationState.ALLOWED);
		setIsFollowingPosition(false);
		mapFragment.stopPositionTracking();
		questAutoSyncer.stopPositionTracking();
		singleLocationRequest.stopRequest();
	}

	private void setIsFollowingPosition(boolean follow)
	{
		trackingButton.setActivated(follow);
		mapFragment.setIsFollowingPosition(follow);
	}

	@Override public void onLocationRequestFinished(LocationState withLocationState)
	{
		hasAskedForLocation = true;
		trackingButton.setState(withLocationState);
		boolean enabled = withLocationState.isEnabled();
		if(enabled)
		{
			onLocationIsEnabled();
		}
		else
		{
			Toast.makeText(MainActivity.this, R.string.no_gps_no_quests, Toast.LENGTH_LONG).show();
		}
	}

	// ---------------------------------------------------------------------------------------------

	/** Does not necessarily mean that the user has internet. But if he is not connected, he will
	 *  not have internet */
	private boolean isConnected()
	{
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
