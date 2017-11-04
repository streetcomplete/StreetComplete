package de.westnordost.streetcomplete.tangram;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.mapzen.tangram.LabelPickResult;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.SceneError;
import com.mapzen.tangram.SceneUpdate;
import com.mapzen.tangram.TouchInput;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

public class QuestsMapFragment extends MapFragment implements TouchInput.TapResponder,
		MapController.LabelPickListener
{
	private static final String MARKER_QUEST_ID = "quest_id";
	private static final String MARKER_QUEST_GROUP = "quest_group";

	private static final String GEOMETRY_LAYER = "streetcomplete_geometry";
	private static final String QUESTS_LAYER = "streetcomplete_quests";

	private MapData questsLayer;
	private MapData geometryLayer;

	private Float previousZoom = null;

	private LngLat lastPos;
	private Rect lastDisplayedRect;
	private Set<Point> retrievedTiles;
	private static final int TILES_ZOOM = 14;

	private static float MAX_QUEST_ZOOM = 19;

	private Listener listener;

	private Rect questOffset;

	@Inject List<QuestType> questTypes;
	@Inject TangramQuestSpriteSheetCreator spriteSheetCreator;
	private final Map<QuestType, Integer> questTypeOrder;

	public interface Listener
	{
		void onClickedQuest(QuestGroup questGroup, Long questId);
		void onClickedMapAt(@Nullable LatLon position);
		/** Called once the given bbox comes into view first (listener should get quests there) */
		void onFirstInView(BoundingBox bbox);
	}

	public QuestsMapFragment()
	{
		Injector.instance.getApplicationComponent().inject(this);
		questTypeOrder = new HashMap<>();
		int order = 0;
		for (QuestType questType : questTypes)
		{
			questTypeOrder.put(questType, order++);
		}
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

	@Override public void onDestroy()
	{
		super.onDestroy();
		questsLayer = geometryLayer = null;
	}

	@Override public void getMapAsync(String apiKey, @NonNull final String sceneFilePath)
	{
		super.getMapAsync(apiKey, sceneFilePath);

		controller.setTapResponder(this);
		controller.setLabelPickListener(this);
		controller.setPickRadius(1);

		List<SceneUpdate> sceneUpdates = spriteSheetCreator.get();
		controller.updateSceneAsync(sceneUpdates);

		retrievedTiles = new HashSet<>();
	}

	@Override public void onSceneReady(int sceneId, SceneError sceneError)
	{
		super.onSceneReady(sceneId, sceneError);
		if (getActivity() == null) return;

		geometryLayer = controller.addDataLayer(GEOMETRY_LAYER);
		questsLayer = controller.addDataLayer(QUESTS_LAYER);
	}

	@Override public boolean onSingleTapUp(float x, float y)
	{
		return false;
	}

	@Override public boolean onSingleTapConfirmed(float x, float y)
	{
		if(controller != null) controller.pickLabel(x,y);
		return true;
	}

	@Override
	public void onLabelPick(LabelPickResult labelPickResult, float positionX, float positionY)
	{
		if(controller == null) return;

		if(labelPickResult == null
				|| labelPickResult.getType() != LabelPickResult.LabelType.ICON
				|| labelPickResult.getProperties() == null
				|| labelPickResult.getProperties().get(MARKER_QUEST_ID) == null)
		{
			onClickedMap(positionX, positionY);
			return;
		}

		Map<String,String> props = labelPickResult.getProperties();
		listener.onClickedQuest(
				QuestGroup.valueOf(props.get(MARKER_QUEST_GROUP)),
				Long.valueOf(props.get(MARKER_QUEST_ID))
		);
	}

	private void zoomAndMoveToContain(ElementGeometry g)
	{
		// never zoom back further than 17.5
		previousZoom = Math.max(17.5f,controller.getZoom());

		float targetZoom = getMaxZoomThatContains(g);
		if(Float.isNaN(targetZoom) || targetZoom > MAX_QUEST_ZOOM)
		{
			targetZoom = MAX_QUEST_ZOOM;
		}
		else
		{
			// zoom out a bit
			targetZoom -= 0.35;
		}

		float currentZoom = controller.getZoom();

		controller.setZoom(targetZoom);
		LngLat pos = getCenterWithOffset(g);
		controller.setZoom(currentZoom);

		controller.setPositionEased(pos, 500);
		controller.setZoomEased(targetZoom, 500);

		updateView();
	}

	private LngLat getCenterWithOffset(ElementGeometry geometry)
	{
		int w = getView().getWidth();
		int h = getView().getHeight();

		LngLat normalCenter = controller.screenPositionToLngLat(new PointF(w/2, h/2));

		LngLat offsetCenter = controller.screenPositionToLngLat(new PointF(
				questOffset.left + (w - questOffset.left - questOffset.right)/2,
				questOffset.top + (h - questOffset.top - questOffset.bottom)/2));

		LngLat pos = TangramConst.toLngLat(geometry.center);
		pos.latitude -= offsetCenter.latitude - normalCenter.latitude;
		pos.longitude -= offsetCenter.longitude - normalCenter.longitude;
		return pos;
	}

	private float getMaxZoomThatContains(ElementGeometry geometry)
	{
		BoundingBox objectBounds = geometry.getBounds();
		BoundingBox screenArea;
		float currentZoom;
		synchronized(controller) {
			screenArea = getDisplayedArea(questOffset);
			if(screenArea == null) return Float.NaN;
			currentZoom = controller.getZoom();
		}

		double screenWidth = screenArea.getMaxLongitude() - screenArea.getMinLongitude();
		double screenHeight = screenArea.getMaxLatitude() - screenArea.getMinLatitude();

		double objectWidth = objectBounds.getMaxLongitude() - objectBounds.getMinLongitude();
		double objectHeight = objectBounds.getMaxLatitude() - objectBounds.getMinLatitude();

		double zoomDeltaX = Math.log10(screenWidth / objectWidth) / Math.log10(2.);
		double zoomDeltaY = Math.log10(screenHeight / objectHeight) / Math.log10(2.);

		return (float) Math.max(1, currentZoom + Math.min(zoomDeltaX, zoomDeltaY));
	}

	private void onClickedMap(float positionX, float positionY)
	{
		final LngLat pos = controller.screenPositionToLngLat(new PointF(positionX, positionY));
		listener.onClickedMapAt(TangramConst.toLatLon(pos));
	}

	@Override protected boolean shouldCenterCurrentPosition()
	{
		// don't center position while displaying a quest
		return super.shouldCenterCurrentPosition() && previousZoom == null;
	}

	protected void updateView()
	{
		super.updateView();

		if(controller.getZoom() < TILES_ZOOM) return;

		// check if anything changed (needs to be extended when I reenable tilt and rotation)
		LngLat positionNow = controller.getPosition();
		if(lastPos != null  && lastPos.equals(positionNow)) return;
		lastPos = positionNow;

		BoundingBox displayedArea = getDisplayedArea(new Rect());
		if(displayedArea == null) return;

		Rect tilesRect = SlippyMapMath.enclosingTiles(displayedArea, TILES_ZOOM);
		if(lastDisplayedRect != null && lastDisplayedRect.equals(tilesRect)) return;
		lastDisplayedRect = tilesRect;

		// area to big -> skip ( see https://github.com/tangrams/tangram-es/issues/1492 )
		if(tilesRect.width() * tilesRect.height() > 4)
		{
			return;
		}

		List<Point> tiles = SlippyMapMath.asTileList(tilesRect);
		tiles.removeAll(retrievedTiles);

		Rect minRect = SlippyMapMath.minRect(tiles);
		if(minRect == null) return;
		BoundingBox bbox = SlippyMapMath.asBoundingBox(minRect, TILES_ZOOM);

		listener.onFirstInView(bbox);

		// debugging
		/*List<LatLon> corners = new ArrayList<LatLon>(5);
		corners.add(bbox.getMin());
		corners.add(new OsmLatLon(bbox.getMinLatitude(), bbox.getMaxLongitude()));
		corners.add(bbox.getMax());
		corners.add(new OsmLatLon(bbox.getMaxLatitude(), bbox.getMinLongitude()));
		corners.add(bbox.getMin());
		ElementGeometry e = new ElementGeometry(null, Collections.singletonList(corners));
		addQuestGeometry(e);*/

		retrievedTiles.addAll(tiles);
	}

	public void setQuestOffsets(Rect offsets)
	{
		questOffset = offsets;
	}

	@UiThread
	public void addQuestGeometry(ElementGeometry g)
	{
		if(geometryLayer == null) return; // might still be null - async calls...

		zoomAndMoveToContain(g);
		updateView();

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
	}

	@UiThread
	public void removeQuestGeometry()
	{
		if(geometryLayer != null) geometryLayer.clear();
		if(controller != null && previousZoom != null)
		{
			controller.setZoomEased(previousZoom, 500);
			previousZoom = null;
			followPosition();
		}
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

	@UiThread
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
			String questIconName = getActivity().getResources().getResourceEntryName(quest.getType().getIcon());

			Integer order = questTypeOrder.get(quest.getType());
			if(order == null) order = 0;

			geoJson.append("{\"type\":\"Feature\",");
			geoJson.append("\"geometry\":{\"type\":\"Point\",\"coordinates\": [");
			geoJson.append(pos.getLongitude());
			geoJson.append(",");
			geoJson.append(pos.getLatitude());
			geoJson.append("]},\"properties\": {\"type\":\"point\", \"kind\":\"");
			geoJson.append(questIconName);
			geoJson.append("\",\"");
			geoJson.append(MARKER_QUEST_GROUP);
			geoJson.append("\":\"");
			geoJson.append(group.name());
			geoJson.append("\",\"");
			geoJson.append(MARKER_QUEST_ID);
			geoJson.append("\":\"");
			geoJson.append(quest.getId());
			geoJson.append("\",\"");
			geoJson.append("order");
			geoJson.append("\":\"");
			geoJson.append(order);
			geoJson.append("\"}}");
		}
		geoJson.append("]}");

		questsLayer.addGeoJson(geoJson.toString());
	}

	@UiThread
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

	public BoundingBox getDisplayedArea(Rect offset)
	{
		if(controller == null) return null;
		if(getView() == null) return null;
		Point size = new Point(
				getView().getWidth() - offset.left - offset.right,
				getView().getHeight() - offset.top - offset.bottom);

		// the special cases here are: map tilt and map rotation:
		// * map tilt makes the screen area -> world map area into a trapezoid
		// * map rotation makes the screen area -> world map area into a rotated rectangle

		// dealing with tilt: this method is just not defined if the tilt is above a certain limit
		if(controller.getTilt() > Math.PI / 4f) return null; // 45°

		LatLon[] positions = new LatLon[4];
		positions[0] = getLatLonAtPos(new PointF(offset.left,          offset.top));
		positions[1] = getLatLonAtPos(new PointF(offset.left + size.x ,offset.top));
		positions[2] = getLatLonAtPos(new PointF(offset.left,          offset.top + size.y));
		positions[3] = getLatLonAtPos(new PointF(offset.left + size.x, offset.top + size.y));

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
