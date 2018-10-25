package de.westnordost.streetcomplete;

import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.inject.Inject;

import de.westnordost.osmapi.common.errors.OsmApiReadResponseException;
import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.about.AboutFragment;
import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestAutoSyncer;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteListener;
import de.westnordost.streetcomplete.data.QuestController;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.download.QuestDownloadProgressListener;
import de.westnordost.streetcomplete.data.download.QuestDownloadService;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.upload.QuestChangesUploadProgressListener;
import de.westnordost.streetcomplete.data.upload.QuestChangesUploadService;
import de.westnordost.streetcomplete.data.upload.VersionBannedException;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteFragment;
import de.westnordost.streetcomplete.location.LocationRequestFragment;
import de.westnordost.streetcomplete.location.LocationState;
import de.westnordost.streetcomplete.location.LocationUtil;
import de.westnordost.streetcomplete.oauth.OAuthPrefs;
import de.westnordost.streetcomplete.quests.AbstractBottomSheetFragment;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.FindQuestSourceComponent;
import de.westnordost.streetcomplete.quests.LeaveNoteInsteadFragment;
import de.westnordost.streetcomplete.quests.OsmQuestAnswerListener;
import de.westnordost.streetcomplete.quests.QuestAnswerComponent;
import de.westnordost.streetcomplete.quests.QuestUtil;
import de.westnordost.streetcomplete.settings.SettingsActivity;
import de.westnordost.streetcomplete.sound.SoundFx;
import de.westnordost.streetcomplete.statistics.AnswersCounter;
import de.westnordost.streetcomplete.tangram.MapControlsFragment;
import de.westnordost.streetcomplete.tangram.MapFragment;
import de.westnordost.streetcomplete.tangram.QuestsMapFragment;
import de.westnordost.streetcomplete.tools.CrashReportExceptionHandler;
import de.westnordost.streetcomplete.util.DpUtil;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.streetcomplete.util.SphericalEarthMath;


import static de.westnordost.streetcomplete.ApplicationConstants.MANUAL_DOWNLOAD_QUEST_TYPE_COUNT;

public class MainActivity extends AppCompatActivity implements
		OsmQuestAnswerListener, CreateNoteListener, VisibleQuestListener,
		QuestsMapFragment.Listener, MapFragment.Listener, MapControlsFragment.Listener
{
	@Inject CrashReportExceptionHandler crashReportExceptionHandler;

	@Inject LocationRequestFragment locationRequestFragment;
	@Inject QuestAutoSyncer questAutoSyncer;

	@Inject QuestController questController;

	@Inject SharedPreferences prefs;
	@Inject OAuthPrefs oAuth;

	@Inject FindQuestSourceComponent questSource;

	@Inject AnswersCounter answersCounter;

	@Inject SoundFx soundFx;

	private final Random random = new Random();

	// per application start settings
	private static boolean hasAskedForLocation = false;
	private static boolean dontShowRequestAuthorizationAgain = false;

	private QuestsMapFragment mapFragment;

	private ProgressBar downloadProgressBar;
	private ProgressBar uploadProgressBar;

	private View unsyncedChangesContainer;

	private float mapRotation, mapTilt;
	private boolean isFollowingPosition;
	private boolean isCompassMode;

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

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

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

		questSource.onCreate(this);
		questController.onCreate();

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

		mapFragment = (QuestsMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.getMapAsync(BuildConfig.MAPZEN_API_KEY);
		updateMapQuestOffsets();
	}

	@Override public void onStart()
	{
		super.onStart();

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

		questController.onStart(this);

		downloadProgressBar.setAlpha(0f);
		downloadServiceIsBound = bindService(new Intent(this, QuestDownloadService.class),
				downloadServiceConnection, BIND_AUTO_CREATE);
		uploadServiceIsBound = bindService(new Intent(this, QuestChangesUploadService.class),
				uploadServiceConnection, BIND_AUTO_CREATE);

		if(!hasAskedForLocation)
		{
			locationRequestFragment.startRequest();
		}
		else
		{
			updateLocationAvailability();
		}
	}

	@Override protected void onResume()
	{
		super.onResume();
		questAutoSyncer.onResume();
		questAutoSyncer.triggerAutoUpload();
	}

	@Override public void onPause()
	{
		super.onPause();
		questAutoSyncer.onPause();

		LatLon pos = mapFragment.getPosition();
		prefs.edit()
			.putLong(Prefs.MAP_LATITUDE, Double.doubleToRawLongBits(pos.getLatitude()))
			.putLong(Prefs.MAP_LONGITUDE, Double.doubleToRawLongBits(pos.getLongitude()))
			.apply();
	}

	@Override public void onStop()
	{
		super.onStop();

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);
		localBroadcaster.unregisterReceiver(locationRequestFinishedReceiver);

		unregisterReceiver(locationAvailabilityReceiver);

		questController.onStop();

		if (downloadServiceIsBound) unbindService(downloadServiceConnection);
		if (downloadService != null)
		{
			downloadService.setProgressListener(null);
			downloadService.startForeground();
			// since we unbound from the service, we won't get the onFinished call. But we will get
			// the onStarted call when we return to this activity when the service is rebound
			downloadProgressBar.setAlpha(0f);
		}

		if (uploadServiceIsBound) unbindService(uploadServiceConnection);
		if (uploadService != null)
		{
			uploadService.setProgressListener(null);
		}
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		questController.onDestroy();
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		findViewById(R.id.main).requestLayout();
		updateMapQuestOffsets();
	}

	private void updateMapQuestOffsets()
	{
		mapFragment.setQuestOffsets(new Rect(
			getResources().getDimensionPixelSize(R.dimen.quest_form_leftOffset),
			0,
			getResources().getDimensionPixelSize(R.dimen.quest_form_rightOffset),
			getResources().getDimensionPixelSize(R.dimen.quest_form_bottomOffset)));
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
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

		text.setText(QuestUtil.getHtmlTitle(getResources(), quest.getType(), element));

		new AlertDialog.Builder(this)
			.setTitle(R.string.undo_confirm_title)
			.setView(inner)
			.setPositiveButton(R.string.undo_confirm_positive, (dialog, which) ->
			{
				questController.undo(quest);
				questAutoSyncer.triggerAutoUpload();
				answersCounter.subtractOneUnsynced(quest.getChangesSource());
			})
			.setNegativeButton(R.string.undo_confirm_negative, null)
			.show();
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		Intent intent;

		switch (id)
		{
			case R.id.action_undo:
				OsmQuest quest = questController.getLastSolvedOsmQuest();
				if(quest != null) confirmUndo(quest);
				else              Toast.makeText(this, R.string.no_changes_to_undo, Toast.LENGTH_SHORT).show();
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

		View inner = LayoutInflater.from(this).inflate(
				R.layout.dialog_authorize_now, null, false);
		final CheckBox checkBox = inner.findViewById(R.id.checkBoxDontShowAgain);

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
					dontShowRequestAuthorizationAgain = checkBox.isChecked();
				}).show();
	}

	@UiThread private void downloadDisplayedArea()
	{
		BoundingBox displayArea;
		if ((displayArea = mapFragment.getDisplayedArea(new Rect())) == null)
		{
			Toast.makeText(this, R.string.cannot_find_bbox_or_reduce_tilt, Toast.LENGTH_LONG).show();
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
					new AlertDialog.Builder(this)
							.setMessage(R.string.confirmation_cancel_prev_download_title)
							.setPositiveButton(android.R.string.ok, (dialog, which) -> downloadAreaConfirmed(enclosingBBox))
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
				bbox = SphericalEarthMath.enclosingBoundingBox(pos,
						ApplicationConstants.MIN_DOWNLOADABLE_RADIUS_IN_METERS);
			}
		}
		questController.download(bbox, MANUAL_DOWNLOAD_QUEST_TYPE_COUNT, true);
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
				// a 5xx error is not the fault of this app. Nothing we can do about it, so it does not
				// make sense to send an error report. Just notify the user
				// Also, we treat an invalid response the same as a (temporary) connection error
				if (e instanceof OsmConnectionException || e instanceof OsmApiReadResponseException)
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
				if (downloadService.currentDownloadHasPriority())
				{
					Toast.makeText(MainActivity.this, R.string.nothing_more_to_download, Toast.LENGTH_SHORT).show();
				}
			});
		}
	};

	/* ------------ Managing bottom sheet (quest details) and interaction with map  ------------- */

	private final static String BOTTOM_SHEET = "bottom_sheet";

	@Override public void onBackPressed()
	{
		AbstractBottomSheetFragment f = getBottomSheetFragment();
		if(f != null)
		{
			f.onClickClose(() ->
			{
				mapFragment.removeQuestGeometry();
				mapFragment.setIsFollowingPosition(isFollowingPosition);
				mapFragment.setCompassMode(isCompassMode);
				mapFragment.showMapControls();
				MainActivity.super.onBackPressed();
			});
		}
		else
		{
			super.onBackPressed();
		}
	}

	/* ------------- OsmQuestAnswerListener ------------- */

	@Override public void onAnsweredQuest(long questId, QuestGroup group, Bundle answer)
	{
		questSource.findSource(questId, group, mapFragment.getDisplayedLocation(), source ->
		{
			closeQuestDetailsFor(questId, group);
			Quest quest = questController.get(questId, group);
			if(questController.solve(questId, group, answer, source))
			{
				showQuestSolvedAnimation(quest, source);
			}
			questAutoSyncer.triggerAutoUpload();
		});
	}

	@Override public void onComposeNote(long questId, QuestGroup group, String questTitle)
	{
		LeaveNoteInsteadFragment f = new LeaveNoteInsteadFragment();
		Bundle args = QuestAnswerComponent.createArguments(questId, group);
		args.putString(LeaveNoteInsteadFragment.ARG_QUEST_TITLE, questTitle);
		f.setArguments(args);

		getSupportFragmentManager().popBackStack(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(
			0, R.animator.quest_answer_form_disappear,
			0, R.animator.quest_answer_form_disappear);
		ft.add(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET);
		ft.addToBackStack(BOTTOM_SHEET);
		ft.commit();
	}

	@Override public void onLeaveNote(long questId, QuestGroup group, String questTitle, String note, ArrayList<String> imagePaths)
	{
		closeBottomSheet();
		// the quest is deleted from DB on creating a note, so need to fetch quest before
		Quest quest = questController.get(questId, group);
		if(questController.createNote(questId, questTitle, note, imagePaths))
		{
			showQuestSolvedAnimation(quest, null);
		}
		questAutoSyncer.triggerAutoUpload();
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

	private void showQuestSolvedAnimation(Quest quest, String source)
	{
		if(quest == null) return;

		int size = (int) DpUtil.toPx(42, this);
		int[] offset = new int[2];
		mapFragment.getView().getLocationOnScreen(offset);
		PointF startPos = mapFragment.getPointOf(quest.getMarkerLocation());
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

	@Override public void onSkippedQuest(long questId, QuestGroup group)
	{
		closeQuestDetailsFor(questId, group);
		questController.hide(questId, group);
	}

	private void closeQuestDetailsFor(long questId, QuestGroup group)
	{
		if (isQuestDetailsCurrentlyDisplayedFor(questId, group))
		{
			closeBottomSheet();
		}
	}

	/* ------------- creating notes ------------- */

	@Override public void onClickCreateNote()
	{
		if (mapFragment.getZoom() < ApplicationConstants.NOTE_MIN_ZOOM)
		{
			Toast.makeText(this, R.string.create_new_note_unprecise, Toast.LENGTH_LONG).show();
			return;
		}

		AbstractBottomSheetFragment f = getBottomSheetFragment();
		if (f != null)   f.onClickClose(this::composeNote);
		else             composeNote();
	}

	private void composeNote()
	{
		showInBottomSheet(new CreateNoteFragment());
	}

	@Override public void onLeaveNote(String note, ArrayList<String> imagePaths, Point screenPosition)
	{
		showMarkerSolvedAnimation(R.drawable.ic_quest_create_note, new PointF(screenPosition), null);
		closeBottomSheet();

		int[] mapPosition = new int[2];
		View mapView = mapFragment.getView();
		if(mapView == null) return;

		mapView.getLocationInWindow(mapPosition);

		PointF notePosition = new PointF(screenPosition);
		notePosition.offset(-mapPosition[0], -mapPosition[1]);

		LatLon position = mapFragment.getPositionAt(notePosition);
		if(position == null) throw new NullPointerException();
		questController.createNote(note, imagePaths, position);
		questAutoSyncer.triggerAutoUpload();
	}

	/* ------------- VisibleQuestListener ------------- */

	@AnyThread @Override
	public void onQuestsCreated(final Collection<? extends Quest> quests, final QuestGroup group)
	{
		runOnUiThread(() -> mapFragment.addQuests(quests, group));
		// to recreate element geometry of selected quest (if any) after recreation of activity
		if(getQuestDetailsFragment() != null)
		{
			for (Quest q : quests)
			{
				if (isQuestDetailsCurrentlyDisplayedFor(q.getId(), group))
				{
					runOnUiThread(() -> showQuestDetails(q, group));
					return;
				}
			}
		}
	}

	@AnyThread @Override
	public synchronized void onQuestsRemoved(Collection<Long> questIds, QuestGroup group)
	{
		runOnUiThread(() -> mapFragment.removeQuests(questIds, group));

		// amount of quests is reduced -> check if redownloding now makes sense
		questAutoSyncer.triggerAutoDownload();

		for(long questId : questIds)
		{
			if (!isQuestDetailsCurrentlyDisplayedFor(questId, group)) continue;

			runOnUiThread(this::closeBottomSheet);
			// disabled this feature (for now), it does not feel good
			/*Quest quest = questController.getNextAt(questId, group);
			if(quest != null)
			{
				runOnUiThread(() -> showQuestDetails(quest, group));
			}*/

			break;
		}
	}

	@UiThread private void closeBottomSheet()
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
			if(inputMethodManager != null)
			{
				inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}

		getSupportFragmentManager().popBackStackImmediate(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		mapFragment.setIsFollowingPosition(isFollowingPosition);
		mapFragment.setCompassMode(isCompassMode);
		mapFragment.removeQuestGeometry();
		mapFragment.showMapControls();
	}

	private boolean isQuestDetailsCurrentlyDisplayedFor(long questId, QuestGroup group)
	{
		AbstractQuestAnswerFragment currentFragment = getQuestDetailsFragment();
		return currentFragment != null
				&& currentFragment.getQuestId() == questId
				&& currentFragment.getQuestGroup() == group;
	}

	@UiThread private void showQuestDetails(Quest quest, QuestGroup group)
	{
		mapFragment.addQuestGeometry(quest.getGeometry());

		if(isQuestDetailsCurrentlyDisplayedFor(quest.getId(), group)) return;

		if(getBottomSheetFragment() != null)
		{
			closeBottomSheet();
		}

		mapFragment.addQuestGeometry(quest.getGeometry());

		AbstractQuestAnswerFragment f = quest.getType().createForm();
		Bundle args = QuestAnswerComponent.createArguments(quest.getId(), group);
		if (group == QuestGroup.OSM)
		{
			OsmElement element = questController.getOsmElement((OsmQuest) quest);
			args.putSerializable(AbstractQuestAnswerFragment.ARG_ELEMENT, element);
		}
		args.putSerializable(AbstractQuestAnswerFragment.ARG_GEOMETRY, quest.getGeometry());
		args.putString(AbstractQuestAnswerFragment.ARG_QUESTTYPE, quest.getType().getClass().getSimpleName());
		args.putFloat(AbstractQuestAnswerFragment.ARG_MAP_ROTATION, mapRotation);
		args.putFloat(AbstractQuestAnswerFragment.ARG_MAP_TILT, mapTilt);
		f.setArguments(args);

		showInBottomSheet(f);
	}

	private void showInBottomSheet(Fragment f)
	{
		isFollowingPosition = mapFragment.isFollowingPosition();
		isCompassMode = mapFragment.isCompassMode();
		mapFragment.setIsFollowingPosition(false);
		mapFragment.setCompassMode(false);
		mapFragment.hideMapControls();

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(
				R.animator.quest_answer_form_appear, R.animator.quest_answer_form_disappear,
				R.animator.quest_answer_form_appear, R.animator.quest_answer_form_disappear);
		ft.add(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET);
		ft.addToBackStack(BOTTOM_SHEET);
		ft.commit();
	}

	private AbstractBottomSheetFragment getBottomSheetFragment()
	{
		return (AbstractBottomSheetFragment) getSupportFragmentManager().findFragmentByTag(BOTTOM_SHEET);
	}

	private AbstractQuestAnswerFragment getQuestDetailsFragment()
	{
		AbstractBottomSheetFragment f = getBottomSheetFragment();

		return f instanceof AbstractQuestAnswerFragment ? (AbstractQuestAnswerFragment) f : null ;
	}

	@AnyThread @Override public void onMapOrientation(float rotation, float tilt)
	{
		mapRotation = rotation;
		mapTilt = tilt;
		AbstractQuestAnswerFragment f = getQuestDetailsFragment();
		if (f != null)
		{
			f.onMapOrientation(rotation, tilt);
		}
	}
	/* ---------- QuestsMapFragment.Listener ---------- */

	@Override public void onFirstInView(BoundingBox bbox)
	{
		questController.retrieve(bbox);
	}

	@Override public synchronized void onClickedQuest(QuestGroup questGroup, Long questId)
	{
		if (isQuestDetailsCurrentlyDisplayedFor(questId, questGroup)) return;

		Runnable retrieveQuest = () ->
		{
			Quest quest = questController.get(questId, questGroup);
			if(quest != null) showQuestDetails(quest, questGroup);
		};

		AbstractBottomSheetFragment f = getBottomSheetFragment();
		if (f != null)  f.onClickClose(retrieveQuest);
		else            retrieveQuest.run();
	}

	@Override public void onClickedMapAt(@Nullable LatLon position)
	{
		AbstractBottomSheetFragment f = getBottomSheetFragment();
		if(f != null)
		{
			f.onClickClose(this::closeBottomSheet);
		}
	}

	/* ---------- Location listener ---------- */

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
