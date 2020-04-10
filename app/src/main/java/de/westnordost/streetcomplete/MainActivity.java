package de.westnordost.streetcomplete;

import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.graphics.Point;
import androidx.annotation.NonNull;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.AnyThread;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import de.westnordost.osmapi.common.errors.OsmApiException;
import de.westnordost.osmapi.common.errors.OsmApiReadResponseException;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.controls.NotificationButtonFragment;
import de.westnordost.streetcomplete.data.notifications.Notification;
import de.westnordost.streetcomplete.data.notifications.NotificationsSource;
import de.westnordost.streetcomplete.data.quest.Quest;
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer;
import de.westnordost.streetcomplete.data.quest.QuestController;
import de.westnordost.streetcomplete.data.download.QuestDownloadProgressListener;
import de.westnordost.streetcomplete.data.quest.QuestUploadDownloadController;
import de.westnordost.streetcomplete.data.quest.UnsyncedChangesCountSource;
import de.westnordost.streetcomplete.data.upload.UploadProgressListener;
import de.westnordost.streetcomplete.data.upload.VersionBannedException;
import de.westnordost.streetcomplete.data.user.UserController;
import de.westnordost.streetcomplete.location.LocationRequestFragment;
import de.westnordost.streetcomplete.location.LocationState;
import de.westnordost.streetcomplete.location.LocationUtil;
import de.westnordost.streetcomplete.map.MainFragment;
import de.westnordost.streetcomplete.notifications.NotificationsContainerFragment;
import de.westnordost.streetcomplete.map.tangram.CameraPosition;
import de.westnordost.streetcomplete.tools.CrashReportExceptionHandler;
import de.westnordost.streetcomplete.tutorial.TutorialFragment;
import de.westnordost.streetcomplete.util.GeoLocation;
import de.westnordost.streetcomplete.util.GeoUriKt;
import de.westnordost.streetcomplete.view.dialogs.RequestLoginDialog;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.JobKt;


public class MainActivity extends AppCompatActivity implements
		MainFragment.Listener,
		TutorialFragment.Listener,
		CoroutineScope,
		NotificationButtonFragment.Listener
{
	@Inject CrashReportExceptionHandler crashReportExceptionHandler;

	@Inject LocationRequestFragment locationRequestFragment;
	@Inject QuestAutoSyncer questAutoSyncer;

	@Inject QuestController questController;
	@Inject QuestUploadDownloadController questUploadDownloadController;
	@Inject NotificationsSource notificationsSource;
	@Inject UnsyncedChangesCountSource unsyncedChangesCountSource;

	@Inject SharedPreferences prefs;
	@Inject UserController userController;


	// per application start settings
	private static boolean hasAskedForLocation = false;
	private static boolean dontShowRequestAuthorizationAgain = false;

	private MainFragment mapFragment;

	private ProgressBar downloadProgressBar;

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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			findViewById(R.id.main).setOnApplyWindowInsetsListener((v, insets) -> {
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

		questUploadDownloadController.removeUploadProgressListener(uploadProgressListener);
		questUploadDownloadController.removeQuestDownloadProgressListener(downloadProgressListener);

		downloadProgressBar.setAlpha(0f);
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

	private void ensureLoggedIn()
	{
		if(questAutoSyncer.isAllowedByPreference())
		{
			if (!userController.isLoggedIn()) {
				// new users should not be immediately pestered to login after each change (#1446)
				if(unsyncedChangesCountSource.getCount() >= 3 && !dontShowRequestAuthorizationAgain) {
					new RequestLoginDialog(this).show();
					dontShowRequestAuthorizationAgain = true;
				}
			}
		}
	}

	/* ------------------------------ Upload progress listener ---------------------------------- */

	private final UploadProgressListener uploadProgressListener
			= new UploadProgressListener()
	{
		@AnyThread @Override public void onStarted(){}

		@Override public void onProgress(boolean success){}

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
					new RequestLoginDialog(MainActivity.this).show();
				}
				else
				{
					crashReportExceptionHandler.askUserToSendErrorReport(
							MainActivity.this, R.string.upload_error, e);
				}
			});
		}

		@AnyThread @Override public void onFinished(){}
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

		@AnyThread @Override public void onSuccess(){}

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

	/* --------------------------------- NotificationButtonFragment.Listener ---------------------------------- */

	@Override public void onClickShowNotification(@NotNull Notification notification)
	{
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.notifications_container_fragment);
		((NotificationsContainerFragment) f).showNotification(notification);
	}

	/* --------------------------------- MainFragment.Listener ---------------------------------- */

	@Override public void onQuestSolved(@Nullable Quest quest, @Nullable String source)
	{
		ensureLoggedIn();
	}

	@Override public void onCreatedNote(@NotNull Point screenPosition)
	{
		ensureLoggedIn();
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
}
