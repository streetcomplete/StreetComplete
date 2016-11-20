package de.westnordost.osmagent;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
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

import javax.inject.Inject;

import de.westnordost.osmagent.data.Quest;
import de.westnordost.osmagent.data.QuestChangesUploadService;
import de.westnordost.osmagent.data.QuestController;
import de.westnordost.osmagent.data.QuestDownloadProgressListener;
import de.westnordost.osmagent.data.QuestDownloadService;
import de.westnordost.osmagent.data.QuestGroup;
import de.westnordost.osmagent.data.VisibleQuestListener;
import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuest;
import de.westnordost.osmagent.quests.AbstractQuestAnswerFragment;
import de.westnordost.osmagent.quests.OsmQuestAnswerListener;
import de.westnordost.osmagent.quests.QuestAnswerComponent;
import de.westnordost.osmagent.settings.SettingsActivity;
import de.westnordost.osmagent.tangram.OsmagentMapFragment;
import de.westnordost.osmagent.util.SphericalEarthMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmElement;

public class MainActivity extends AppCompatActivity implements
		OsmQuestAnswerListener, VisibleQuestListener, OsmagentMapFragment.Listener
{

	private OsmagentMapFragment mapFragment;

	private Long clickedQuestId = null;
	private QuestGroup clickedQuestGroup = null;

	@Inject QuestController questController;

	private ProgressBar progressBar;

	private boolean downloadServiceIsBound;
	private QuestDownloadService.Interface downloadService;
	private ServiceConnection downloadServiceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			downloadService = (QuestDownloadService.Interface)service;
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

		progressBar = (ProgressBar) findViewById(R.id.download_progress);
		progressBar.setMax(1000);

		mapFragment = (OsmagentMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.getMapAsync();
	}

	@Override public void onStart()
	{
		super.onStart();
		questController.onStart(this);
		downloadServiceIsBound = bindService(
				new Intent(this, QuestDownloadService.class),
				downloadServiceConnection, BIND_AUTO_CREATE);
	}

	@Override public void onResume()
	{
		super.onResume();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

		registerReceiver(wifiReceiver, intentFilter);

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);

		IntentFilter uploadChangesErrFilter = new IntentFilter(QuestChangesUploadService.ACTION_ERROR);
		localBroadcaster.registerReceiver(uploadChangesErrorReceiver, uploadChangesErrFilter);
	}

	@Override public void onPause()
	{
		super.onPause();

		unregisterReceiver(wifiReceiver);

		LocalBroadcastManager localBroadcaster = LocalBroadcastManager.getInstance(this);

		localBroadcaster.unregisterReceiver(uploadChangesErrorReceiver);
	}

	@Override public void onStop()
	{
		super.onStop();
		questController.onStop();
		if(downloadServiceIsBound) unbindService(downloadServiceConnection);
		if(downloadService != null)
		{
			downloadService.setProgressListener(null);
			downloadService.startForeground();
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

		switch(id)
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
		final BoundingBox bbox = mapFragment.getDisplayedArea();
		if(bbox == null)
		{
			Toast.makeText(this, R.string.cannot_find_bbox, Toast.LENGTH_LONG).show();
		}
		else
		{
			double areaInSqKm = SphericalEarthMath.enclosedArea(bbox) / 1000000;
			if(areaInSqKm > OsmagentConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM)
			{
				Toast.makeText(this, R.string.download_area_too_big, Toast.LENGTH_LONG).show();
			}
			else
			{
				if(questController.isManualDownloadRunning())
				{
					DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							downloadAreaConfirmed(bbox);
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
					downloadAreaConfirmed(bbox);
				}
			}
		}
	}

	private void downloadAreaConfirmed(BoundingBox bbox)
	{
		double areaInSqKm = SphericalEarthMath.enclosedArea(bbox) / 1000000;
		// below a certain threshold, it does not make sense to download, so let's enlarge it
		if(areaInSqKm < OsmagentConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM)
		{
			LatLon pos = mapFragment.getPosition();
			if(pos != null)
			{
				questController.download(SphericalEarthMath.enclosingBoundingBox(pos,
						OsmagentConstants.MIN_DOWNLOADABLE_RADIUS_IN_METERS), null, true);
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
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(0);
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

		@Override public void onError()
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					Toast.makeText(MainActivity.this, R.string.download_error, Toast.LENGTH_LONG).show();
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
	}

	@Override public void onLeaveNote(long questId, QuestGroup group, String note)
	{
		questController.createNote(questId, note);
	}

	@Override public void onSkippedQuest(long questId, QuestGroup group)
	{
		questController.hideQuest(questId, group);
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
		if(clickedQuestId != null && quest.getId().equals(clickedQuestId) && group == clickedQuestGroup)
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
		}
		else if(isQuestDetailsCurrentlyDisplayedFor(quest.getId(), group))
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
		if(isQuestDetailsCurrentlyDisplayedFor(questId, group))
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
		if (view != null) {
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
		if(isQuestDetailsCurrentlyDisplayedFor(quest.getId(), group)) return;

		if(getQuestDetailsFragment() != null)
		{
			if(!confirmed)
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
		if(group == QuestGroup.OSM)
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
		if(f == null || !f.hasChanges())
		{
			r.run();
		}
		else
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

	/* ---------- OsmagentMapFragment.Listener ---------- */

	@Override public void onMapReady()
	{
		// TODO: only for now null (=ALL quests), later only where the user is standing
		questController.retrieve(null);
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

	// ---------------------------------------------------------------------------------------------

	private boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting() &&
				activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
	}

	private BroadcastReceiver wifiReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
			{
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				Log.i("OSMAGENT", info.isConnected() ? "connected" : "disconnected");
			}
		}
	};
}
