package de.westnordost.osmagent;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.AnyThread;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.MapView;
import com.mapzen.tangram.TouchInput;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmagent.data.Quest;
import de.westnordost.osmagent.data.QuestController;
import de.westnordost.osmagent.data.QuestGroup;
import de.westnordost.osmagent.data.VisibleQuestListener;
import de.westnordost.osmagent.data.osm.ElementGeometry;
import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuest;
import de.westnordost.osmagent.quests.AbstractQuestAnswerFragment;
import de.westnordost.osmagent.quests.OsmQuestAnswerListener;
import de.westnordost.osmagent.quests.QuestAnswerComponent;
import de.westnordost.osmagent.settings.SettingsActivity;
import de.westnordost.osmagent.tangram.MapFragment;
import de.westnordost.osmagent.tangram.TangramConst;
import de.westnordost.osmagent.util.SphericalEarthMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.osmapi.map.data.OsmLatLon;

public class MainActivity extends AppCompatActivity implements OsmQuestAnswerListener, VisibleQuestListener
{
	private static final String GEOMETRY_LAYER = "osmagent_geometry";
	private static final String QUESTS_LAYER = "osmagent_quests";

	private static final String MARKER_QUEST_ID = "quest_id";
	private static final String MARKER_QUEST_GROUP = "quest_group";

	private MapController map;
	private MapData questsLayer;
	private MapData geometryLayer;

	private Long clickedQuestId = null;
	private QuestGroup clickedQuestGroup = null;

	@Inject QuestController questController;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Injector.instance.getApplicationComponent().inject(this);

		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.getMapAsync(new MapView.OnMapReadyCallback()
		{
			@Override public void onMapReady(MapController mapController)
			{
				map = mapController;
				geometryLayer = map.addDataLayer(GEOMETRY_LAYER);
				questsLayer = map.addDataLayer(QUESTS_LAYER);

				// TODO "null" for BBOX only provisional, for testing
				questController.retrieve(null);

				map.setFeaturePickListener(new MapController.FeaturePickListener()
				{
					@Override
					public void onFeaturePick(Map<String, String> props, float positionX, float positionY)
					{
						boolean clickedMarker = props != null && props.containsKey(MARKER_QUEST_ID);

						if(clickedMarker)
						{
							clickedQuestGroup = QuestGroup.valueOf(props.get(MARKER_QUEST_GROUP));
							clickedQuestId = Long.valueOf(props.get(MARKER_QUEST_ID));

							questController.retrieve(clickedQuestId, clickedQuestGroup);
						}
					}
				});
				map.setTapResponder(new TouchInput.TapResponder()
				{
					@Override public boolean onSingleTapUp(float x, float y)
					{
						return false;
					}

					@Override public boolean onSingleTapConfirmed(float x, float y)
					{
						map.pickFeature(x,y);

						/*
						TODO use later!:
							confirmDiscardChangesIfAny(new Runnable()
							{
								@Override public void run()
								{
									closeQuestDetails();
								}
							});
						 */

						return true;
					}
				});
			}
		});

		questController.setQuestListener(this);
	}


	@Override protected void onDestroy()
	{
		super.onDestroy();
		questController.setQuestListener(null);
	}

	@Override protected void onStart()
	{
		super.onStart();
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
				BoundingBox yangon = SphericalEarthMath.enclosingBoundingBox(new OsmLatLon(16.77428,96.16560),1000);
				questController.download(yangon, null);
				return true;

			case R.id.action_upload:

				return true;

		}

		return super.onOptionsItemSelected(item);
	}

	// ---------------------------------------------------------------------------------------------

	private final String BOTTOM_SHEET = "bottom_sheet";

	@UiThread private void closeQuestDetails()
	{
		getFragmentManager().popBackStack(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		removeQuestGeometryFromMap();

		// sometimes the keyboard fails to close
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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

		addQuestGeometryToMap(quest);

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

	private void addQuestGeometryToMap(Quest quest)
	{
		if(geometryLayer == null) return; // might still be null - async calls...

		ElementGeometry g = quest.getGeometry();
		Map<String,String> props = new HashMap<>();

		if(g.polygons != null)
		{
			props.put("type", "poly");
			geometryLayer.addPolygon(TangramConst.toLngLat(g.polygons), props);
		}
		else if(g.polylines != null)
		{
			props.put("type", "line");
			List<List<LngLat>> polylines = TangramConst.toLngLat(g.polylines);
			for(List<LngLat> polyline : polylines)
			{
				geometryLayer.addPolyline(polyline, props);
			}
		}
		else if(g.center != null)
		{
			props.put("type", "point");
			geometryLayer.addPoint(TangramConst.toLngLat(g.center), props);
		}
		map.applySceneUpdates();
	}

	private void removeQuestGeometryFromMap()
	{
		if(geometryLayer == null) return; // might still be null - async calls...

		geometryLayer.clear();
	}

	private AbstractQuestAnswerFragment getQuestDetailsFragment()
	{
		return (AbstractQuestAnswerFragment) getFragmentManager().findFragmentByTag(BOTTOM_SHEET);
	}

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

	private void backAndCleanGeometry()
	{
		removeQuestGeometryFromMap();
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
			addQuestGeometryToMap(quest);
		}
		addQuestToMap(quest, group);
	}

	@Override public void onQuestRemoved(OsmQuest quest)
	{
		onQuestRemoved(quest, QuestGroup.OSM);
	}

	@Override public void onQuestRemoved(OsmNoteQuest quest)
	{
		onQuestRemoved(quest, QuestGroup.OSM_NOTE);
	}

	@AnyThread public synchronized void onQuestRemoved(Quest quest, QuestGroup group)
	{
		if(isQuestDetailsCurrentlyDisplayedFor(quest.getId(), group))
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					closeQuestDetails();
				}
			});
		}
		removeQuestFromMap(quest.getId(), group);
	}

	private void addQuestToMap(Quest quest, QuestGroup group)
	{
		if(questsLayer == null) return;

		LngLat pos = TangramConst.toLngLat(quest.getMarkerLocation());
		Map<String, String> props = new HashMap<>();
		props.put("type", "point");
		props.put("kind", quest.getType().getIconName());
		props.put(MARKER_QUEST_GROUP, group.name());
		props.put(MARKER_QUEST_ID, String.valueOf(quest.getId()));
		questsLayer.addPoint(pos, props);

		map.applySceneUpdates();
	}

	private void removeQuestFromMap(long questId, QuestGroup group)
	{
		if(questsLayer == null) return;
		// TODO (currently not possible with tangram, but it has been announced that this will soon
		// be added
	}

	// ---------------------------------------------------------------------------------------------

	private WifiReceiver x;

	private boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting() &&
				activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
	}

	@Override public void onResume()
	{
		super.onResume();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		x = new WifiReceiver();
		registerReceiver(x, intentFilter);
	}

	@Override public void onPause()
	{
		super.onPause();
		unregisterReceiver(x);
	}

	private class WifiReceiver extends BroadcastReceiver
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
	}
}
