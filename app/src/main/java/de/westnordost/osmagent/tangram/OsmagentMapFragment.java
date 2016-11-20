package de.westnordost.osmagent.tangram;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.Nullable;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.TouchInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmagent.data.Quest;
import de.westnordost.osmagent.data.QuestGroup;
import de.westnordost.osmagent.data.osm.ElementGeometry;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

public class OsmagentMapFragment extends MapFragment
{
	private static final String MARKER_QUEST_ID = "quest_id";
	private static final String MARKER_QUEST_GROUP = "quest_group";

	private static final String GEOMETRY_LAYER = "osmagent_geometry";
	private static final String QUESTS_LAYER = "osmagent_quests";

	private MapData questsLayer;
	private MapData geometryLayer;

	private Listener listener;

	public interface Listener
	{
		void onMapReady();
		void onClickedQuest(QuestGroup questGroup, Long questId);
		void onClickedMapAt(@Nullable LatLon position);
	}

	@Override public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		listener = (Listener) activity;
	}

	protected void initMap()
	{
		super.initMap();

		geometryLayer = controller.addDataLayer(GEOMETRY_LAYER);
		questsLayer = controller.addDataLayer(QUESTS_LAYER);

		controller.setFeaturePickListener(new MapController.FeaturePickListener()
		{
			@Override
			public void onFeaturePick(Map<String, String> props, float positionX, float positionY)
			{
				boolean clickedMarker = props != null && props.containsKey(MARKER_QUEST_ID);

				if(clickedMarker)
				{
					listener.onClickedQuest(
							QuestGroup.valueOf(props.get(MARKER_QUEST_GROUP)),
							Long.valueOf(props.get(MARKER_QUEST_ID))
					);
				}
			}
		});

		controller.setTapResponder(new TouchInput.TapResponder()
		{
			@Override public boolean onSingleTapUp(float x, float y)
			{
				return false;
			}

			@Override public boolean onSingleTapConfirmed(float x, float y)
			{
				controller.pickFeature(x,y);

				// TODO use later!:
				/*LngLat lngLat = controller.screenPositionToLngLat(new PointF(x,y));
				LatLon latLon = null;
				if(lngLat != null)
				{
					latLon = TangramConst.toLatLon(lngLat);
				}
				listener.onClickedMapAt(latLon);*/

				return true;
			}
		});

		controller.setRotateResponder(new TouchInput.RotateResponder()
		{
			@Override public boolean onRotate(float x, float y, float rotation)
			{
				// no rotating the map! (for now)
				return true;
			}
		});

		controller.setShoveResponder(new TouchInput.ShoveResponder()
		{
			@Override public boolean onShove(float distance)
			{
				// no tilting the map! (for now)
				return true;
			}
		});

		listener.onMapReady();
	}

	public void addQuestGeometry(ElementGeometry g)
	{
		if(geometryLayer == null) return; // might still be null - async calls...

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
		controller.applySceneUpdates();
	}

	public void removeQuestGeometry()
	{
		if(geometryLayer == null) return; // might still be null - async calls...

		geometryLayer.clear();
	}

	public void addQuest(Quest quest, QuestGroup group)
	{
		// TODO: this method may also be called for quests that are already displayed on this map
		if(questsLayer == null) return;

		LngLat pos = TangramConst.toLngLat(quest.getMarkerLocation());
		Map<String, String> props = new HashMap<>();
		props.put("type", "point");
		props.put("kind", quest.getType().getIconName());
		props.put(MARKER_QUEST_GROUP, group.name());
		props.put(MARKER_QUEST_ID, String.valueOf(quest.getId()));
		questsLayer.addPoint(pos, props);

		controller.applySceneUpdates();
	}

	public void removeQuest(QuestGroup group, long questId)
	{
		// TODO: this method may also be called for quests that are not displayed on this map (anymore)

		if(questsLayer == null) return;
		// TODO (currently not possible with tangram, but it has been announced that this will soon
		// be added
	}

	public BoundingBox getDisplayedArea()
	{
		if(controller == null) return null;
		if(getView() == null) return null;

		Point size = new Point(getView().getMeasuredWidth(), getView().getMeasuredHeight());

		// the special cases here are: map tilt and map rotation:
		// * map tilt makes the screen area -> world map area into a trapezoid
		// * map rotation makes the screen area -> world map area into a rotated rectangle

		// dealing with tilt: this method is just not defined if the tilt is above a certain limit
		if(controller.getTilt() > Math.PI / 4f) return null; // 45°

		LatLon[] positions = new LatLon[4];
		try
		{
			positions[0] = getLatLonAtPos(new PointF(0,0));
			positions[1] = getLatLonAtPos(new PointF(size.x, 0));
			positions[2] = getLatLonAtPos(new PointF(0,size.y));
			positions[3] = getLatLonAtPos(new PointF(size));
		}
		// screenPositionToLngLat returns positions out of range (crossing 180th meridian?)
		catch(IllegalArgumentException e)
		{
			// in any case, this special case is not really relevant for this app, we are not
			// downloading huge areas here
			return null;
		}

		// dealing with rotation: find each the largest latlon and the smallest latlon, that'll
		// be our bounding box

		Double latMin = null, lonMin = null, latMax = null, lonMax = null;
		for (LatLon position : positions)
		{
			double lat = position.getLatitude();
			double lon = position.getLongitude();

			if (latMin == null || latMin > lat) latMin = lat;
			if (latMax == null || latMax < lat) latMax = lat;
			if (lonMin == null || lonMin > lon) lonMin = lon;
			if (lonMax == null || lonMax < lon) lonMax = lon;
		}

		return new BoundingBox(latMin, lonMin, latMax, lonMax);
	}

	private LatLon getLatLonAtPos(PointF pointF)
	{
		return TangramConst.toLatLon(controller.screenPositionToLngLat(pointF));
	}

	public LatLon getPosition()
	{
		if(controller == null) return null;
		return TangramConst.toLatLon(controller.getPosition());
	}

}
