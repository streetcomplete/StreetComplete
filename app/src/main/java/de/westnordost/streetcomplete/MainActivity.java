package de.westnordost.streetcomplete;

import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.AnyThread;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import de.westnordost.streetcomplete.controls.AnswersCounterView;
import de.westnordost.streetcomplete.controls.MainMenuDialog;
import de.westnordost.streetcomplete.controls.NotificationButton;
import de.westnordost.streetcomplete.controls.UploadButton;
import de.westnordost.streetcomplete.data.notifications.Notification;
import de.westnordost.streetcomplete.data.notifications.NotificationsDao;
import de.westnordost.streetcomplete.data.quest.Quest;
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer;
import de.westnordost.streetcomplete.data.quest.QuestController;
import de.westnordost.streetcomplete.data.download.QuestDownloadProgressListener;
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest;
import de.westnordost.streetcomplete.data.quest.QuestUploadDownloadController;
import de.westnordost.streetcomplete.data.quest.UndoableOsmQuestsCountListener;
import de.westnordost.streetcomplete.data.quest.UndoableOsmQuestsSource;
import de.westnordost.streetcomplete.data.upload.UploadProgressListener;
import de.westnordost.streetcomplete.data.upload.VersionBannedException;
import de.westnordost.streetcomplete.data.user.UserController;
import de.westnordost.streetcomplete.location.LocationRequestFragment;
import de.westnordost.streetcomplete.location.LocationState;
import de.westnordost.streetcomplete.location.LocationUtil;
import de.westnordost.streetcomplete.map.MainFragment;
import de.westnordost.streetcomplete.notifications.NotificationsContainerFragment;
import de.westnordost.streetcomplete.quests.QuestUtilKt;
import de.westnordost.streetcomplete.sound.SoundFx;
import de.westnordost.streetcomplete.statistics.AnswersCounter;
import de.westnordost.streetcomplete.map.tangram.CameraPosition;
import de.westnordost.streetcomplete.tools.CrashReportExceptionHandler;
import de.westnordost.streetcomplete.tutorial.TutorialFragment;
import de.westnordost.streetcomplete.user.UserActivity;
import de.westnordost.streetcomplete.util.DpUtil;
import de.westnordost.streetcomplete.util.GeoLocation;
import de.westnordost.streetcomplete.util.GeoUriKt;
import de.westnordost.streetcomplete.util.SlippyMapMathKt;
import de.westnordost.streetcomplete.util.SphericalEarthMathKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.JobKt;


import static de.westnordost.streetcomplete.ApplicationConstants.MANUAL_DOWNLOAD_QUEST_TYPE_COUNT;

public class MainActivity extends AppCompatActivity implements
		MainFragment.Listener,
		TutorialFragment.Listener,
		NotificationsDao.UpdateListener,
		CoroutineScope
{
	@Inject CrashReportExceptionHandler crashReportExceptionHandler;

	@Inject LocationRequestFragment locationRequestFragment;
	@Inject QuestAutoSyncer questAutoSyncer;

	@Inject QuestController questController;
	@Inject QuestUploadDownloadController questUploadDownloadController;
	@Inject UndoableOsmQuestsSource undoableOsmQuestsSource;
	@Inject NotificationsDao notificationsDao;

	@Inject SharedPreferences prefs;
	@Inject UserController userController;

	@Inject AnswersCounter answersCounter;

	@Inject SoundFx soundFx;

	@Inject FutureTask<FeatureDictionary> featureDictionaryFutureTask;

	private final Random random = new Random();

	// per application start settings
	private static boolean hasAskedForLocation = false;
	private static boolean dontShowRequestAuthorizationAgain = false;

	private MainFragment mapFragment;

	private ProgressBar downloadProgressBar;

	private View undoButton;
	private UploadButton uploadButton;
	private AnswersCounterView answersCounterView;
	private NotificationButton notificationButton;

	private boolean isAutosync = false;

	private CoroutineScope coroutineScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getMain());

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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}

		if(prefs.getBoolean(Prefs.KEEP_SCREEN_ON, false))
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		getSupportFragmentManager().beginTransaction()
			.add(locationRequestFragment, LocationRequestFragment.class.getSimpleName())
			.commit();

		setContentView(R.layout.activity_main);
		setupFittingToSystemWindowInsets();

		undoButton = findViewById(R.id.undoButton);
		undoButton.setOnClickListener(v -> {
			setUndoButtonEnabled(false);
			OsmQuest quest = questController.getLastSolvedOsmQuest();
			if (quest != null) confirmUndo(quest);
			else setUndoButtonEnabled(true);
		});

		findViewById(R.id.downloadButton).setOnClickListener(v -> {
			if(isConnected()) downloadDisplayedArea();
			else              Toast.makeText(this, R.string.offline, Toast.LENGTH_SHORT).show();
		});

		findViewById(R.id.mainMenuButton).setOnClickListener(v -> {
			new MainMenuDialog(this).show();
		});

		uploadButton = findViewById(R.id.uploadButton);
		answersCounterView = findViewById(R.id.answersCounterView);
		answersCounter.setViews(uploadButton, answersCounterView);
		uploadButton.setOnClickListener(view ->
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

		notificationButton = findViewById(R.id.notificationButton);
		notificationButton.setOnClickListener(view ->
		{
			Notification notification = notificationsDao.popNextNotification();
			if (notification != null)
			{
				Fragment f = getSupportFragmentManager().findFragmentById(R.id.notifications_container_fragment);
				((NotificationsContainerFragment) f).showNotification(notification);
			}
		});

		downloadProgressBar = findViewById(R.id.download_progress);
		downloadProgressBar.setMax(1000);

		mapFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

		if(savedInstanceState == null)
		{
			questController.cleanUp(new Continuation<Unit>()
			{
				@NotNull @Override public CoroutineContext getContext() { return coroutineScope.getCoroutineContext(); }
				@Override public void resumeWith(@NotNull Object o){ }
			});
			if (userController.isLoggedIn()) {
				userController.updateUser(new Continuation<Unit>()
				{
					@NotNull @Override public CoroutineContext getContext() { return coroutineScope.getCoroutineContext(); }
					@Override public void resumeWith(@NotNull Object o){ }
				});
			}
		}

		handleGeoUri();
	}

	private void setupFittingToSystemWindowInsets() {
		ViewGroup buttonsContainer = findViewById(R.id.buttonsContainer);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			findViewById(R.id.main).setOnApplyWindowInsetsListener((v, insets) -> {
				ViewGroup.MarginLayoutParams buttonsParams = (ViewGroup.MarginLayoutParams) buttonsContainer.getLayoutParams();
				buttonsParams.setMargins(
						insets.getSystemWindowInsetLeft(),
						insets.getSystemWindowInsetTop(),
						insets.getSystemWindowInsetRight(),
						insets.getSystemWindowInsetBottom()
				);
				buttonsContainer.setLayoutParams(buttonsParams);

				// download progress will be behind the status bar
				ViewGroup.LayoutParams downloadProgressParams = downloadProgressBar.getLayoutParams();
				downloadProgressParams.height = insets.getSystemWindowInsetTop();
				downloadProgressBar.setLayoutParams(downloadProgressParams);
				return insets;
			});
		}
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
		boolean hasShownTutorial = prefs.getBoolean(Prefs.HAS_SHOWN_TUTORIAL, false);

		if (!hasShownTutorial) {
			prefs.edit().putBoolean(Prefs.HAS_SHOWN_TUTORIAL, true).apply();
			if (lastVersion == null) {
				getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
						.add(R.id.fragment_container, new TutorialFragment())
						.commit();
			}
		}

		isAutosync = Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC,"ON")) == Prefs.Autosync.ON;
		answersCounter.setAutosync(isAutosync);
		answersCounter.update();

		registerReceiver(locationAvailabilityReceiver, LocationUtil.createLocationAvailabilityIntentFilter());

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);

		localBroadcaster.registerReceiver(locationRequestFinishedReceiver,
				new IntentFilter(LocationRequestFragment.ACTION_FINISHED));

		questUploadDownloadController.setShowNotification(false);

		if (questUploadDownloadController.isDownloadInProgress()) {
			downloadProgressBar.setAlpha(1f);
			downloadProgressBar.setProgress((int)(questUploadDownloadController.getDownloadProgress() * 1000));
		} else {
			downloadProgressBar.setAlpha(0f);
		}

		questUploadDownloadController.addUploadProgressListener(uploadProgressListener);
		questUploadDownloadController.addQuestDownloadProgressListener(downloadProgressListener);

		undoButton.setVisibility(undoableOsmQuestsSource.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
		undoableOsmQuestsSource.addListener(undoableOsmQuestsCountListener);

		onNumberOfNotificationsUpdated(notificationsDao.getNumberOfNotifications());
		notificationsDao.addListener(this);

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

		questUploadDownloadController.setShowNotification(true);

		undoableOsmQuestsSource.removeListener(undoableOsmQuestsCountListener);

		questUploadDownloadController.removeUploadProgressListener(uploadProgressListener);
		questUploadDownloadController.removeQuestDownloadProgressListener(downloadProgressListener);

		downloadProgressBar.setAlpha(0f);

		notificationsDao.removeListener(this);
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
				answersCounter.subtractOneUnsynced();
				setUndoButtonEnabled(true);
			})
			.setNegativeButton(R.string.undo_confirm_negative, (dialog, which) -> { setUndoButtonEnabled(true); })
			.setOnCancelListener(dialog -> { setUndoButtonEnabled(true); })
			.show();
	}

	private void setUndoButtonEnabled(boolean enabled)
	{
		undoButton.setEnabled(enabled);
		undoButton.setAlpha(enabled ? 1f : 0.5f);
	}

	@UiThread private void uploadChanges()
	{
		// because the app should ask for permission even if there is nothing to upload right now
		if(!userController.isLoggedIn())
		{
			requestOAuthorized();
		}
		else
		{
			questUploadDownloadController.upload();
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
					Intent intent = new Intent(MainActivity.this, UserActivity.class);
					intent.putExtra(UserActivity.EXTRA_LAUNCH_AUTH, true);
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
			final BoundingBox enclosingBBox = SlippyMapMathKt.asBoundingBoxOfEnclosingTiles(
					displayArea, ApplicationConstants.QUEST_TILE_ZOOM);
			double areaInSqKm = SphericalEarthMathKt.area(enclosingBBox, SphericalEarthMathKt.EARTH_RADIUS) / 1000000;
			if (areaInSqKm > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM)
			{
				Toast.makeText(this, R.string.download_area_too_big, Toast.LENGTH_LONG).show();
			}
			else
			{
				if (questUploadDownloadController.isPriorityDownloadInProgress())
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
		questUploadDownloadController.download(bbox, MANUAL_DOWNLOAD_QUEST_TYPE_COUNT, true);
	}

	private void triggerAutoUploadByUserInteraction()
	{
		if(questAutoSyncer.isAllowedByPreference())
		{
			if (!userController.isLoggedIn()) {
				// new users should not be immediately pestered to login after each change (#1446)
				if(answersCounter.waitingForUpload() >= 3) {
					requestOAuthorized();
				}
			}
		}
	}

	/* ------------------------------ Upload progress listener ---------------------------------- */

	private final UploadProgressListener uploadProgressListener
			= new UploadProgressListener()
	{
		@AnyThread @Override public void onStarted()
		{
			runOnUiThread(() ->
			{
				uploadButton.setEnabled(false);
				setUndoButtonEnabled(false);
				if(isAutosync) answersCounterView.setShowProgress(true);
				else uploadButton.setShowProgress(true);
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
					userController.logOut();
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
				uploadButton.setEnabled(true);
				setUndoButtonEnabled(true);

				if(isAutosync) answersCounterView.setShowProgress(false);
				else uploadButton.setShowProgress(false);
			});
			answersCounter.update();
		}
	};

	/* --------------------------- UndoableOsmQuestsCountListener  ------------------------------ */

	private final UndoableOsmQuestsCountListener undoableOsmQuestsCountListener = new UndoableOsmQuestsCountListener() {

		@AnyThread @Override public void onUndoableOsmQuestsCountDecreased()
		{
			if (undoableOsmQuestsSource.getCount() <= 0) {
				runOnUiThread(() -> undoButton.setVisibility(View.INVISIBLE));
			}
		}

		@AnyThread @Override public void onUndoableOsmQuestsCountIncreased()
		{
			runOnUiThread(() -> undoButton.setVisibility(View.VISIBLE));
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
				if (questUploadDownloadController.isPriorityDownloadInProgress())
				{
					Toast.makeText(MainActivity.this, R.string.nothing_more_to_download, Toast.LENGTH_SHORT).show();
				}
			});
		}
	};

	/* ---------------------------------- NotificationsListener --------------------------------- */

	@Override public void onNumberOfNotificationsUpdated(int numberOfNotifications) {
		notificationButton.setVisibility(numberOfNotifications > 0 ? View.VISIBLE : View.INVISIBLE);
		notificationButton.setNotificationsCount(numberOfNotifications);
	}

	/* --------------------------------- MainFragment.Listener ---------------------------------- */

	@Override public void onQuestSolved(@Nullable Quest quest, @Nullable String source)
	{
		showQuestSolvedAnimation(quest);
		triggerAutoUploadByUserInteraction();
	}

	@Override public void onCreatedNote(@NotNull Point screenPosition)
	{
		showMarkerSolvedAnimation(R.drawable.ic_quest_create_note, new PointF(screenPosition));
		triggerAutoUploadByUserInteraction();
	}

	/* ------------------------------- TutorialFragment.Listener -------------------------------- */


	@Override public void onFinishedTutorial()
	{
		Fragment tutorialFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if (tutorialFragment != null)
		{
			getSupportFragmentManager().beginTransaction()
					.setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
					.remove(tutorialFragment)
					.commit();
		}
	}

	/* ---------------------------------------- Animation ---------------------------------------- */


	private void showQuestSolvedAnimation(Quest quest)
	{
		if(quest == null) return;

		int size = (int) DpUtil.toPx(42, this);
		int[] offset = new int[2];
		mapFragment.getView().getLocationOnScreen(offset);
		PointF startPos = mapFragment.getPointOf(quest.getCenter());
		startPos.x += offset[0] - size/2;
		startPos.y += offset[1] - size*1.5;
		showMarkerSolvedAnimation(quest.getType().getIcon(), startPos);
	}

	private void showMarkerSolvedAnimation(@DrawableRes int iconResId, PointF startScreenPos)
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
			answersCounter.addOneUnsynced();
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
