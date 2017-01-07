package de.westnordost.streetcomplete.tangram;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.mapzen.tangram.LabelPickResult;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.TouchInput;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

public class QuestsMapFragment extends MapFragment implements TouchInput.ScaleResponder,
		TouchInput.ShoveResponder, TouchInput.RotateResponder, MapController.LabelPickListener,
		TouchInput.TapResponder, TouchInput.PanResponder, TouchInput.DoubleTapResponder
{
	private static final String MARKER_QUEST_ID = "quest_id";
	private static final String MARKER_QUEST_GROUP = "quest_group";

	private static final String GEOMETRY_LAYER = "streetcomplete_geometry";
	private static final String QUESTS_LAYER = "streetcomplete_quests";

	private MapData questsLayer;
	private MapData geometryLayer;

	private LngLat lastPos;
	private Rect lastDisplayedRect;
	private Set<Point> retrievedTiles;
	private static final int TILES_ZOOM = 14;

	private Listener listener;

	public interface Listener
	{
		void onMapReady();
		void onClickedQuest(QuestGroup questGroup, Long questId);
		void onClickedMapAt(@Nullable LatLon position);
		/** Called once the given bbox comes into view first (listener should get quests there) */
		void onFirstInView(BoundingBox bbox);
	}

	@Override public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		listener = (Listener) activity;
	}

	@Override public void onStart()
	{
		super.onStart();
		/* while the map fragment is stopped, there could still be a download which retrieves new
		 * quests in progress. If the retrieved tiles memory would not be cleared, the map would not
 		 * retrieve these new quests from DB when the user scrolls over the map because the map
		 * thinks it already retrieved the quests from DB.
		 * (If a download is active while the user views the map, the quests are added on the fly) */
		retrievedTiles = new HashSet<>();
	}

	protected void initMap()
	{
		super.initMap();

		retrievedTiles = new HashSet<>();

		geometryLayer = controller.addDataLayer(GEOMETRY_LAYER);
		questsLayer = controller.addDataLayer(QUESTS_LAYER);

		controller.setLabelPickListener(this);
		controller.setTapResponder(this);
		controller.setRotateResponder(this);
		controller.setShoveResponder(this);
		controller.setScaleResponder(this);
		controller.setPanResponder(this);
		controller.setDoubleTapResponder(this);

		listener.onMapReady();
		updateView();
	}

	@Override public boolean onDoubleTap(float x, float y)
	{
		LngLat zoomTo = controller.screenPositionToLngLat(new PointF(x, y));
		controller.setPositionEased(zoomTo, 500);
		controller.setZoomEased(controller.getZoom() + 1.5f, 500);
		return true;
	}

	@Override public boolean onScale(float x, float y, float scale, float velocity)
	{
		updateView();
		// okay, scale
		return false;
	}

	@Override public boolean onPan(float startX, float startY, float endX, float endY)
	{
		updateView();
		// okay, pan
		return false;
	}

	@Override public boolean onFling(float posX, float posY, float velocityX, float velocityY)
	{
		updateView();
		// okay, fling
		return false;
	}

	@Override public boolean onShove(float distance)
	{
		// no tilting the map! (for now)
		return true;
	}

	@Override public boolean onRotate(float x, float y, float rotation)
	{
		// no rotating the map! (for now)
		return true;
	}

	@Override public boolean onSingleTapUp(float x, float y)
	{
		return false;
	}

	@Override public boolean onSingleTapConfirmed(float x, float y)
	{
		controller.pickLabel(x,y);

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

	@Override
	public void onLabelPick(LabelPickResult labelPickResult, float positionX, float positionY)
	{
		if(labelPickResult == null) return;
		if(labelPickResult.getType() != LabelPickResult.LabelType.ICON) return;
		Map<String,String> props = labelPickResult.getProperties();
		if(props == null) return;
		if(!props.containsKey(MARKER_QUEST_ID)) return;

		// move center a little because we have the bottom sheet blocking part of the map (hopefully temporary solution)
		LatLon pos = TangramConst.toLatLon(labelPickResult.getCoordinates());
		LngLat zoomTo = TangramConst.toLngLat(SphericalEarthMath.translate(pos,20,180));
		controller.setPositionEased(zoomTo, 500);
		controller.setZoomEased(19, 500);

		listener.onClickedQuest(
				QuestGroup.valueOf(props.get(MARKER_QUEST_GROUP)),
				Long.valueOf(props.get(MARKER_QUEST_ID))
		);
	}

	private void updateView()
	{
		if(controller.getZoom() < TILES_ZOOM) return;

		// check if anything changed (needs to be extended when I reenable tilt and rotation)
		LngLat positionNow = controller.getPosition();
		if(lastPos != null  && lastPos.equals(positionNow)) return;
		lastPos = positionNow;

		BoundingBox displayedArea = getDisplayedArea();
		if(displayedArea == null) return;

		Rect tilesRect = SlippyMapMath.enclosingTiles(displayedArea, TILES_ZOOM);
		if(lastDisplayedRect != null && lastDisplayedRect.equals(tilesRect)) return;
		lastDisplayedRect = tilesRect;

		List<Point> tiles = SlippyMapMath.asTileList(tilesRect);
		tiles.removeAll(retrievedTiles);

		Rect minRect = SlippyMapMath.minRect(tiles);
		if(minRect == null) return;
		BoundingBox bbox = SlippyMapMath.asBoundingBox(minRect, TILES_ZOOM);

		listener.onFirstInView(bbox);

		// debugging
		/*List<LatLon> corners = new ArrayList<LatLon>(4);
		corners.add(bbox.getMin());
		corners.add(new OsmLatLon(bbox.getMinLatitude(), bbox.getMaxLongitude()));
		corners.add(bbox.getMax());
		corners.add(new OsmLatLon(bbox.getMaxLatitude(), bbox.getMinLongitude()));
		ElementGeometry e = new ElementGeometry(null, Collections.singletonList(corners));
		addQuestGeometry(e);*/

		retrievedTiles.addAll(tiles);
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
/*
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
*/

	public void addQuests(Iterable quests, QuestGroup group)
	{
		if(questsLayer == null) return;

		StringBuilder geoJson = new StringBuilder();
		geoJson.append("{\"type\":\"FeatureCollection\",\"features\": [");

		boolean first = true;
		for(Object q : quests)
		{
			Quest quest = (Quest) q;
			if(first) first = false;
			else      geoJson.append(",");

			LatLon pos = quest.getMarkerLocation();

			geoJson.append("{\"type\":\"Feature\",");
			geoJson.append("\"geometry\":{\"type\":\"Point\",\"coordinates\": [");
			geoJson.append(pos.getLongitude());
			geoJson.append(",");
			geoJson.append(pos.getLatitude());
			geoJson.append("]},\"properties\": {\"type\":\"point\", \"kind\":\"");
			geoJson.append(quest.getType().getIconName());
			geoJson.append("\",\"");
			geoJson.append(MARKER_QUEST_GROUP);
			geoJson.append("\":\"");
			geoJson.append(group.name());
			geoJson.append("\",\"");
			geoJson.append(MARKER_QUEST_ID);
			geoJson.append("\":\"");
			geoJson.append(quest.getId());
			geoJson.append("\"}}");
		}
		geoJson.append("]}");

		questsLayer.addGeoJson(geoJson.toString());
	}

	public void removeQuests(Collection<Long> questIds, QuestGroup group)
	{
		// TODO: this method may also be called for quests that are not displayed on this map (anymore)

		if(questsLayer == null) return;
		// TODO (currently not possible with tangram, but it has been announced that this will soon
		// be added

		// so for now...:
		questsLayer.clear();
		retrievedTiles.clear();
		lastPos = null;
		lastDisplayedRect = null;
		updateView();
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
		positions[0] = getLatLonAtPos(new PointF(0,0));
		positions[1] = getLatLonAtPos(new PointF(size.x, 0));
		positions[2] = getLatLonAtPos(new PointF(0,size.y));
		positions[3] = getLatLonAtPos(new PointF(size));

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
