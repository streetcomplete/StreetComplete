package de.westnordost.streetcomplete;

import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.AnyThread;
import androidx.annotation.UiThread;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import de.westnordost.osmapi.common.errors.OsmApiException;
import de.westnordost.osmapi.common.errors.OsmApiReadResponseException;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmfeatures.FeatureDictionary;
import de.westnordost.streetcomplete.about.AboutFragment;
import de.westnordost.streetcomplete.about.WhatsNewDialog;
import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestAutoSyncer;
import de.westnordost.streetcomplete.data.QuestController;
import de.westnordost.streetcomplete.data.download.QuestDownloadProgressListener;
import de.westnordost.streetcomplete.data.download.QuestDownloadService;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.upload.QuestChangesUploadProgressListener;
import de.westnordost.streetcomplete.data.upload.QuestChangesUploadService;
import de.westnordost.streetcomplete.data.upload.VersionBannedException;
import de.westnordost.streetcomplete.location.LocationRequestFragment;
import de.westnordost.streetcomplete.location.LocationState;
import de.westnordost.streetcomplete.location.LocationUtil;
import de.westnordost.streetcomplete.map.MainFragment;
import de.westnordost.streetcomplete.oauth.OAuthPrefs;
import de.westnordost.streetcomplete.quests.QuestUtilKt;
import de.westnordost.streetcomplete.settings.SettingsActivity;
import de.westnordost.streetcomplete.sound.SoundFx;
import de.westnordost.streetcomplete.statistics.AnswersCounter;
import de.westnordost.streetcomplete.map.tangram.CameraPosition;
import de.westnordost.streetcomplete.tools.CrashReportExceptionHandler;
import de.westnordost.streetcomplete.util.DpUtil;
import de.westnordost.streetcomplete.util.GeoLocation;
import de.westnordost.streetcomplete.util.GeoUriKt;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.streetcomplete.util.SphericalEarthMathKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.JobKt;


import static de.westnordost.streetcomplete.ApplicationConstants.MANUAL_DOWNLOAD_QUEST_TYPE_COUNT;

public class MainActivity extends AppCompatActivity implements  MainFragment.Listener, CoroutineScope
{
	@Inject CrashReportExceptionHandler crashReportExceptionHandler;

	@Inject LocationRequestFragment locationRequestFragment;
	@Inject QuestAutoSyncer questAutoSyncer;

	@Inject QuestController questController;

	@Inject SharedPreferences prefs;
	@Inject OAuthPrefs oAuth;

	@Inject AnswersCounter answersCounter;

	@Inject SoundFx soundFx;

	@Inject FutureTask<FeatureDictionary> featureDictionaryFutureTask;

	private final Random random = new Random();

	// per application start settings
	private static boolean hasAskedForLocation = false;
	private static boolean dontShowRequestAuthorizationAgain = false;

	private MainFragment mapFragment;

	private ProgressBar downloadProgressBar;
	private ProgressBar uploadProgressBar;

	private View unsyncedChangesContainer;
	private MenuItem btnUndo;

	private CoroutineScope coroutineScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getMain());

	private boolean downloadServiceIsBound;
	private QuestDownloadService.Interface downloadService;
	private final ServiceConnection downloadServiceConnection = new ServiceConnection()
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
	private boolean uploadServiceIsBound;
	private QuestChangesUploadService.Interface uploadService;
	private final ServiceConnection uploadServiceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			uploadService = (QuestChangesUploadService.Interface) service;
			uploadService.setProgressListener(uploadProgressListener);
		}

		public void onServiceDisconnected(ComponentName className)
		{
			uploadService = null;
		}
	};

	private final BroadcastReceiver locationAvailabilityReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			updateLocationAvailability();
		}
	};

	private final BroadcastReceiver locationRequestFinishedReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			LocationState state = LocationState.valueOf(intent.getStringExtra(LocationRequestFragment.STATE));
			onLocationRequestFinished(state);
		}
	};

	@NotNull @Override public CoroutineContext getCoroutineContext()
	{
		return coroutineScope.getCoroutineContext();
	}

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		getLifecycle().addObserver(questAutoSyncer);

		crashReportExceptionHandler.askUserToSendCrashReportIfExists(this);

		soundFx.prepare(R.raw.plop0);
		soundFx.prepare(R.raw.plop1);
		soundFx.prepare(R.raw.plop2);
		soundFx.prepare(R.raw.plop3);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		if(prefs.getBoolean(Prefs.KEEP_SCREEN_ON, false))
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		getSupportFragmentManager().beginTransaction()
			.add(locationRequestFragment, LocationRequestFragment.class.getSimpleName())
			.commit();

		setContentView(R.layout.activity_main);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("");
		setSupportActionBar(toolbar);

		TextView uploadedAnswersView = findViewById(R.id.uploadedAnswersCounter);
		TextView unsyncedChangesView = findViewById(R.id.unsyncedAnswersCounter);
		unsyncedChangesContainer = findViewById(R.id.unsyncedAnswersContainer);
		answersCounter.setViews(uploadedAnswersView, unsyncedChangesView, unsyncedChangesContainer);
		unsyncedChangesContainer.setOnClickListener(view ->
		{
			if (isConnected())
			{
				uploadChanges();
			}
			else
			{
				Toast.makeText(MainActivity.this, R.string.offline, Toast.LENGTH_SHORT).show();
			}
		});

		downloadProgressBar = findViewById(R.id.download_progress);
		downloadProgressBar.setMax(1000);

		RelativeLayout starMenu = findViewById(R.id.uploadedAnswersContainer);
		starMenu.setOnClickListener(this::starInfoMenu);

		mapFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

		if(savedInstanceState == null)
		{
			questController.deleteOld(new Continuation<Unit>()
			{
				@NotNull @Override public CoroutineContext getContext() { return coroutineScope.getCoroutineContext(); }
				@Override public void resumeWith(@NotNull Object o){ }
			});
		}

		handleGeoUri();
	}

	private void handleGeoUri()
	{
		Intent intent = getIntent();
		if (!Intent.ACTION_VIEW.equals(intent.getAction())) return;
		Uri data = intent.getData();
		if (data == null) return;
		if (!"geo".equals(data.getScheme())) return;

		GeoLocation geoLocation = GeoUriKt.parseGeoUri(data);
		if (geoLocation == null) return;

		float zoom;
		if (geoLocation.getZoom() == null || geoLocation.getZoom() < 14) {
			zoom = 18;
		} else {
			zoom = geoLocation.getZoom();
		}
		LatLon pos = new OsmLatLon(geoLocation.getLatitude(), geoLocation.getLongitude());
		mapFragment.setCameraPosition(pos, zoom);
	}

	@Override public void onStart()
	{
		super.onStart();

		String lastVersion = prefs.getString(Prefs.LAST_VERSION, null);
		if (!(BuildConfig.VERSION_NAME).equals(lastVersion))
		{
			prefs.edit().putString(Prefs.LAST_VERSION, BuildConfig.VERSION_NAME).apply();
			if (lastVersion != null)
			{
				new WhatsNewDialog(this, "v" + lastVersion).show();
			}
		}

		boolean isAutosync = Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC,"ON")) == Prefs.Autosync.ON;
		ProgressBar uploadedAnswersProgressBar = findViewById(R.id.uploadedAnswersProgress);
		ProgressBar unsyncedAnswersProgressBar = findViewById(R.id.unsyncedAnswersProgress);
		uploadedAnswersProgressBar.setVisibility(View.INVISIBLE);
		unsyncedAnswersProgressBar.setVisibility(View.INVISIBLE);

		uploadProgressBar = isAutosync ? uploadedAnswersProgressBar : unsyncedAnswersProgressBar;
		answersCounter.setAutosync(isAutosync);
		answersCounter.update();

		registerReceiver(locationAvailabilityReceiver, LocationUtil.createLocationAvailabilityIntentFilter());

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);

		localBroadcaster.registerReceiver(locationRequestFinishedReceiver,
				new IntentFilter(LocationRequestFragment.ACTION_FINISHED));

		downloadProgressBar.setAlpha(0f);
		downloadServiceIsBound = bindService(new Intent(this, QuestDownloadService.class),
				downloadServiceConnection, BIND_AUTO_CREATE);
		uploadServiceIsBound = bindService(new Intent(this, QuestChangesUploadService.class),
				uploadServiceConnection, BIND_AUTO_CREATE);

		if(!hasAskedForLocation && !prefs.getBoolean(Prefs.LAST_LOCATION_REQUEST_DENIED, false))
		{
			locationRequestFragment.startRequest();
		}
		else
		{
			updateLocationAvailability();
		}
	}

	@Override public void onBackPressed()
	{
		if(!mapFragment.onBackPressed()) super.onBackPressed();
	}

	@Override protected void onResume()
	{
		super.onResume();
		questAutoSyncer.triggerAutoUpload();
	}

	@Override public void onPause()
	{
		super.onPause();
		if (mapFragment != null) {
			CameraPosition camera = mapFragment.getCameraPosition();
			if (camera != null)
			{
				LatLon pos = camera.getPosition();
				prefs.edit()
					.putLong(Prefs.MAP_LATITUDE, Double.doubleToRawLongBits(pos.getLatitude()))
					.putLong(Prefs.MAP_LONGITUDE, Double.doubleToRawLongBits(pos.getLongitude()))
					.apply();
			}
		}

	}

	@Override public void onStop()
	{
		super.onStop();

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);
		localBroadcaster.unregisterReceiver(locationRequestFinishedReceiver);

		unregisterReceiver(locationAvailabilityReceiver);

		if (downloadServiceIsBound) unbindService(downloadServiceConnection);
		downloadServiceIsBound = false;
		if (downloadService != null)
		{
			downloadService.setProgressListener(null);
			downloadService.startForeground();
			// since we unbound from the service, we won't get the onFinished call. But we will get
			// the onStarted call when we return to this activity when the service is rebound
			downloadProgressBar.setAlpha(0f);
		}

		if (uploadServiceIsBound) unbindService(uploadServiceConnection);
		uploadServiceIsBound = false;
		if (uploadService != null)
		{
			uploadService.setProgressListener(null);
		}
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		JobKt.cancel(coroutineScope.getCoroutineContext(), null);
	}

	@Override public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		findViewById(R.id.main).requestLayout();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		btnUndo = menu.findItem(R.id.action_undo);
		updateUndoButtonVisibility();
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	private void confirmUndo(final OsmQuest quest)
	{
		Element element = questController.getOsmElement(quest);

		View inner = LayoutInflater.from(this).inflate(R.layout.dialog_undo, null, false);
		ImageView icon = inner.findViewById(R.id.icon);
		icon.setImageResource(quest.getType().getIcon());
		TextView text = inner.findViewById(R.id.text);

		text.setText(QuestUtilKt.getHtmlQuestTitle(getResources(), quest.getType(), element, featureDictionaryFutureTask));

		new AlertDialog.Builder(this)
			.setTitle(R.string.undo_confirm_title)
			.setView(inner)
			.setPositiveButton(R.string.undo_confirm_positive, (dialog, which) ->
			{
				questController.undo(quest);
				questAutoSyncer.triggerAutoUpload();
				answersCounter.subtractOneUnsynced(quest.getChangesSource());
				updateUndoButtonVisibility();
				setUndoButtonEnabled(true);
			})
			.setNegativeButton(R.string.undo_confirm_negative, (dialog, which) -> { setUndoButtonEnabled(true); })
			.setOnCancelListener(dialog -> { setUndoButtonEnabled(true); })
			.show();
	}

	private void updateUndoButtonVisibility()
	{
		btnUndo.setVisible(questController.getLastSolvedOsmQuest() != null);
	}

	private void setUndoButtonEnabled(boolean enabled)
	{
		btnUndo.setEnabled(enabled);
		btnUndo.getIcon().setAlpha(enabled ? 255 : 127);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		Intent intent;

		switch (id)
		{
			case R.id.action_undo:
				setUndoButtonEnabled(false);
				OsmQuest quest = questController.getLastSolvedOsmQuest();
				if (quest != null) confirmUndo(quest);
				else setUndoButtonEnabled(true);
				return true;
			case R.id.action_settings:
				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_about:
				intent = new Intent(this, FragmentContainerActivity.class);
				intent.putExtra(FragmentContainerActivity.EXTRA_FRAGMENT_CLASS, AboutFragment.class.getName());
				startActivity(intent);
				return true;
			case R.id.action_download:
				if(isConnected()) downloadDisplayedArea();
				else              Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show();
				return true;
			case R.id.action_open_location:
				CameraPosition camera = mapFragment.getCameraPosition();
				if (camera == null) return true;

				LatLon position = camera.getPosition();
				float zoom = camera.getZoom();
				Uri uri = GeoUriKt.buildGeoUri(position.getLatitude(), position.getLongitude(), zoom);
				intent = new Intent(Intent.ACTION_VIEW, uri);
				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivity(intent);
				} else {
					Toast.makeText(this, R.string.map_application_missing, Toast.LENGTH_LONG).show();
				}
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@UiThread private void uploadChanges()
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

	@UiThread private void requestOAuthorized()
	{
		if(dontShowRequestAuthorizationAgain) return;

		View inner = LayoutInflater.from(this).inflate(R.layout.dialog_authorize_now, null, false);

		new AlertDialog.Builder(this)
				.setView(inner)
				.setPositiveButton(android.R.string.ok, (dialog, which) ->
				{
					Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
					intent.putExtra(SettingsActivity.EXTRA_LAUNCH_AUTH, true);
					startActivity(intent);
				})
				.setNegativeButton(R.string.later, (dialog, which) ->
				{
					dontShowRequestAuthorizationAgain = true;
				}).show();
	}

	@UiThread private void downloadDisplayedArea()
	{
		BoundingBox displayArea;
		if ((displayArea = mapFragment.getDisplayedArea()) == null)
		{
			Toast.makeText(this, R.string.cannot_find_bbox_or_reduce_tilt, Toast.LENGTH_LONG).show();
		}
		else
		{
			final BoundingBox enclosingBBox = SlippyMapMath.asBoundingBoxOfEnclosingTiles(
					displayArea, ApplicationConstants.QUEST_TILE_ZOOM);
			double areaInSqKm = SphericalEarthMathKt.area(enclosingBBox, SphericalEarthMathKt.EARTH_RADIUS) / 1000000;
			if (areaInSqKm > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM)
			{
				Toast.makeText(this, R.string.download_area_too_big, Toast.LENGTH_LONG).show();
			}
			else
			{
				if (questController.isPriorityDownloadInProgress())
				{
					new AlertDialog.Builder(this)
							.setMessage(R.string.confirmation_cancel_prev_download_title)
							.setPositiveButton(R.string.confirmation_cancel_prev_download_confirmed, (dialog, which) -> downloadAreaConfirmed(enclosingBBox))
							.setNegativeButton(R.string.confirmation_cancel_prev_download_cancel, null)
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
		double areaInSqKm = SphericalEarthMathKt.area(bbox, SphericalEarthMathKt.EARTH_RADIUS) / 1000000;
		// below a certain threshold, it does not make sense to download, so let's enlarge it
		if (areaInSqKm < ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM)
		{
			CameraPosition cameraPosition = mapFragment.getCameraPosition();
			if (cameraPosition != null)
			{
				LatLon pos = cameraPosition.getPosition();
				bbox = SphericalEarthMathKt.enclosingBoundingBox(pos,
					ApplicationConstants.MIN_DOWNLOADABLE_RADIUS_IN_METERS,
					SphericalEarthMathKt.EARTH_RADIUS);
			}
		}
		questController.download(bbox, MANUAL_DOWNLOAD_QUEST_TYPE_COUNT, true);
	}

	private void triggerAutoUploadByUserInteraction()
	{
		if(questAutoSyncer.isAllowedByPreference())
		{
			if (!oAuth.isAuthorized()) {
				// new users should not be immediately pestered to login after each change (#1446)
				if(answersCounter.waitingForUpload() >= 3) {
					requestOAuthorized();
				}
			}
			else {
				questAutoSyncer.triggerAutoUpload();
			}
		}
	}

	/* ------------------------------ Upload progress listener ---------------------------------- */

	private final QuestChangesUploadProgressListener uploadProgressListener
			= new QuestChangesUploadProgressListener()
	{
		@AnyThread @Override public void onStarted()
		{
			runOnUiThread(() ->
			{
				unsyncedChangesContainer.setEnabled(false);
				setUndoButtonEnabled(false);
				if(uploadProgressBar != null) uploadProgressBar.setVisibility(View.VISIBLE);
			});
		}

		@Override public void onProgress(boolean success)
		{
			runOnUiThread(() ->
			{
				if(success) answersCounter.uploadedOne();
				else        answersCounter.discardedOne();
			});
		}

		@AnyThread @Override public void onError(final Exception e)
		{
			runOnUiThread(() ->
			{
				if(e instanceof VersionBannedException)
				{
					String message = getString(R.string.version_banned_message);
					VersionBannedException vbe = (VersionBannedException) e;
					if(vbe.getBanReason() != null)
					{
						message += "\n\n" + vbe.getBanReason();
					}

					AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
							.setMessage(message)
							.setPositiveButton(android.R.string.ok, null)
							.create();

					dialog.show();

					// Makes links in the alert dialog clickable
					View messageView = dialog.findViewById(android.R.id.message);
					if(messageView != null && messageView instanceof TextView)
					{
						TextView messageText = (TextView) messageView;
						messageText.setMovementMethod(LinkMovementMethod.getInstance());
						Linkify.addLinks(messageText, Linkify.WEB_URLS);
					}
				}
				else if(e instanceof OsmConnectionException)
				{
					// a 5xx error is not the fault of this app. Nothing we can do about it, so
					// just notify the user
					Toast.makeText(MainActivity.this, R.string.upload_server_error, Toast.LENGTH_LONG).show();
				}
				else if(e instanceof OsmAuthorizationException)
				{
					// delete secret in case it failed while already having a token -> token is invalid
					oAuth.saveConsumer(null);
					requestOAuthorized();
				}
				else
				{
					crashReportExceptionHandler.askUserToSendErrorReport(
							MainActivity.this, R.string.upload_error, e);
				}
			});
		}

		@AnyThread @Override public void onFinished()
		{
			runOnUiThread(() ->
			{
				unsyncedChangesContainer.setEnabled(true);
				setUndoButtonEnabled(true);
				if(uploadProgressBar != null) uploadProgressBar.setVisibility(View.INVISIBLE);
			});
			answersCounter.update();
		}
	};

	/* ----------------------------- Download Progress listener  -------------------------------- */

	private final QuestDownloadProgressListener downloadProgressListener
			= new QuestDownloadProgressListener()
	{
		@AnyThread @Override public void onStarted()
		{
			runOnUiThread(() ->
			{
				downloadProgressBar.animate().alpha(1);
				downloadProgressBar.setProgress(0);

				Toast.makeText(
						MainActivity.this,
						R.string.now_downloading_toast,
						Toast.LENGTH_SHORT).show();
			});
		}

		@AnyThread @Override public void onProgress(final float progress)
		{
			runOnUiThread(() ->
			{
				int intProgress = (int) (1000 * progress);
				ObjectAnimator progressAnimator = ObjectAnimator.ofInt(downloadProgressBar, "progress", intProgress);
				progressAnimator.setDuration(1000);
				progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
				progressAnimator.start();
			});
		}

		@AnyThread @Override public void onError(final Exception e)
		{
			runOnUiThread(() ->
			{
				// a 5xx error is not the fault of this app. Nothing we can do about it, so it does
				// not make sense to send an error report. Just notify the user. Further, we treat
				// the following errors the same as a (temporary) connection error:
				// - an invalid response (OsmApiReadResponseException)
				// - request timeout (OsmApiException with error code 408)
				boolean isEnvironmentError =
					e instanceof OsmConnectionException ||
					e instanceof OsmApiReadResponseException ||
					(e instanceof OsmApiException && ((OsmApiException) e).getErrorCode() == 408);
				if (isEnvironmentError)
				{
					Toast.makeText(MainActivity.this,R.string.download_server_error, Toast.LENGTH_LONG).show();
				}
				else
				{
					crashReportExceptionHandler.askUserToSendErrorReport(MainActivity.this, R.string.download_error, e);
				}
			});
		}

		@AnyThread @Override public void onSuccess()
		{
			// after downloading, regardless if triggered manually or automatically, the
			// auto downloader should check whether there are enough quests in the vicinity now
			questAutoSyncer.triggerAutoDownload();
		}

		@AnyThread @Override public void onFinished()
		{
			runOnUiThread(() ->
			{
				downloadProgressBar.animate().alpha(0).setDuration(1000);
			});
		}

		@AnyThread @Override public void onNotStarted()
		{
			runOnUiThread(() ->
			{
				if (questController.isPriorityDownloadInProgress())
				{
					Toast.makeText(MainActivity.this, R.string.nothing_more_to_download, Toast.LENGTH_SHORT).show();
				}
			});
		}
	};

	/* --------------------------------- MainFragment.Listener ---------------------------------- */

	@Override public void onQuestSolved(@Nullable Quest quest, @Nullable String source)
	{
		updateUndoButtonVisibility();
		showQuestSolvedAnimation(quest, source);
		triggerAutoUploadByUserInteraction();
	}

	@Override public void onCreatedNote(@NotNull Point screenPosition)
	{
		showMarkerSolvedAnimation(R.drawable.ic_quest_create_note, new PointF(screenPosition), null);
		triggerAutoUploadByUserInteraction();
	}

	/* ---------------------------------------- Animation ---------------------------------------- */


	private void showQuestSolvedAnimation(Quest quest, String source)
	{
		if(quest == null) return;

		int size = (int) DpUtil.toPx(42, this);
		int[] offset = new int[2];
		mapFragment.getView().getLocationOnScreen(offset);
		PointF startPos = mapFragment.getPointOf(quest.getCenter());
		startPos.x += offset[0] - size/2;
		startPos.y += offset[1] - size*1.5;
		showMarkerSolvedAnimation(quest.getType().getIcon(), startPos, source);
	}

	private void showMarkerSolvedAnimation(@DrawableRes int iconResId, PointF startScreenPos, String source)
	{
		soundFx.play(getResources().getIdentifier("plop"+random.nextInt(4), "raw", getPackageName()));

		ViewGroup root = (ViewGroup) getWindow().getDecorView();
		ImageView img = (ImageView) getLayoutInflater().inflate(R.layout.effect_quest_plop, root, false);
		img.setX(startScreenPos.x);
		img.setY(startScreenPos.y);
		img.setImageResource(iconResId);
		root.addView(img);

		flingQuestMarkerTo(img, answersCounter.getAnswerTarget(), () -> {
			root.removeView(img);
			answersCounter.addOneUnsynced(source);
		});
	}

	private void flingQuestMarkerTo(View quest, View target, Runnable onFinished)
	{
		int[] targetPos = new int[2];
		target.getLocationOnScreen(targetPos);

		quest.animate()
			.scaleX(1.6f).scaleY(1.6f)
			.setInterpolator(new OvershootInterpolator(8f))
			.setDuration(250)
			.withEndAction(() -> {
				quest.animate()
					.scaleX(0.2f).scaleY(0.2f)
					.alpha(0.8f)
					.x(targetPos[0]).y(targetPos[1])
					.setDuration(250)
					.setInterpolator(new AccelerateInterpolator())
					.withEndAction(onFinished);
			});
	}

	/* ------------------------------------ Location listener ----------------------------------- */

	private void updateLocationAvailability()
	{
		if(LocationUtil.isLocationOn(this))
		{
			questAutoSyncer.startPositionTracking();
		}
		else
		{
			questAutoSyncer.stopPositionTracking();
		}
	}

	private void onLocationRequestFinished(LocationState withLocationState)
	{
		hasAskedForLocation = true;
		boolean enabled = withLocationState.isEnabled();
		prefs.edit().putBoolean(Prefs.LAST_LOCATION_REQUEST_DENIED, !enabled).apply();

		if(enabled)
		{
			updateLocationAvailability();
		}
		else
		{
			Toast.makeText(MainActivity.this, R.string.no_gps_no_quests, Toast.LENGTH_LONG).show();
		}
	}

	// ---------------------------------------------------------------------------------------------

	/** Menu raised by clicking on the star icon */
	public void starInfoMenu(View view) {
		String message = getString(R.string.about_contributing) + "\n\n" + getString(R.string.about_missing_stars);
		if(answersCounter.waitingForUpload() + answersCounter.uploaded() == 0){
			message = getString(R.string.how_to_get_stars) + "\n\n" + getString(R.string.about_contributing);
		}
		new AlertDialog.Builder(MainActivity.this)
			.setMessage(message)
			.show();
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
