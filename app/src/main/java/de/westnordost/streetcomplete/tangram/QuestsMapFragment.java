package de.westnordost.streetcomplete.tangram;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.mapzen.tangram.LabelPickResult;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.Marker;
import com.mapzen.tangram.SceneError;
import com.mapzen.tangram.SceneUpdate;
import com.mapzen.tangram.TouchInput;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry;
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry;
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider;
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway;
import de.westnordost.streetcomplete.util.DpUtil;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

public class QuestsMapFragment extends MapFragment implements TouchInput.TapResponder,
		MapController.LabelPickListener
{
	private static final String MARKER_QUEST_ID = "quest_id";
	private static final String MARKER_QUEST_GROUP = "quest_group";

	private static final String GEOMETRY_LAYER = "streetcomplete_geometry";
	private static final String QUESTS_LAYER = "streetcomplete_quests";

	private MapData questsLayer;
	private MapData geometryLayer;

	private Float zoomBeforeShowingQuest = null;
	private LngLat positionBeforeShowingQuest = null;

	private LngLat lastPos;
	private Float lastRotation, lastTilt;

	private LatLon lastClickPos;
	private double lastFingerRadiusInMeters;

	// TODO this could maybe solved instead by a scene update, see https://tangrams.readthedocs.io/en/latest/Syntax-Reference/layers/
	// (enabled key) but not until the fragment learnt how to properly reinitialize its markers on a scene update
	private boolean isShowingQuests = true;

	private Rect lastDisplayedRect;
	private final Set<Point> retrievedTiles;
	private static final int TILES_ZOOM = 14;

	private static final float MAX_QUEST_ZOOM = 19;

	private static final int CLICK_AREA_SIZE_IN_DP = 48;

	private String sceneFile;

	private Listener listener;

	private Rect questOffset;

	// LatLon -> Marker Id
	private final Map<LatLon, Long> markerIds = new HashMap<>();

	@Inject OrderedVisibleQuestTypesProvider questTypesProvider;
	@Inject TangramQuestSpriteSheetCreator spriteSheetCreator;
	private Map<QuestType, Integer> questTypeOrder;

	public interface Listener
	{
		void onClickedQuest(QuestGroup questGroup, Long questId);
		void onClickedMapAt(@NonNull LatLon position, double clickAreaSizeInMeters);
		/** Called once the given bbox comes into view first (listener should get quests there) */
		void onFirstInView(BoundingBox bbox);
	}

	public QuestsMapFragment()
	{
		Injector.instance.getApplicationComponent().inject(this);
		retrievedTiles = new HashSet<>();
	}

	@Override public void onAttach(Context context)
	{
		super.onAttach(context);
		listener = (Listener) context;
	}

	@Override public void onStart()
	{
		super.onStart();
		questTypeOrder = new HashMap<>();
		int order = 0;
		for (QuestType questType : questTypesProvider.get())
		{
			questTypeOrder.put(questType, order++);
		}

		if (isShowingQuests)
		{
			BoundingBox displayedArea = getDisplayedArea(new Rect());
			if (displayedArea != null)
			{
				lastDisplayedRect = SlippyMapMath.enclosingTiles(displayedArea, TILES_ZOOM);
				updateQuestsInRect(lastDisplayedRect);
			}
		}
	}

	@Override public void onResume()
	{
		super.onResume();
		if (sceneFile != null && !sceneFile.equals(getSceneFilePath()))
		{
			/* recreation needs to be delayed because otherwise
			* the activity might not be fully resumed yet before it is destroyed */
			new Handler().postDelayed(() -> getActivity().recreate(), 1);
		}
	}

	@Override public void onStop()
	{
		super.onStop();
		/* When reentering the fragment, the database may have changed (quest download in
		*  background or change in settings), so the quests must be pulled from DB again */
		clearQuests();
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
	}

	@Override protected void loadScene(String sceneFilePath)
	{
		List<SceneUpdate> sceneUpdates = spriteSheetCreator.get();
		sceneUpdates.add(new SceneUpdate("global.language", Locale.getDefault().getLanguage()));

		sceneFile = sceneFilePath;
		controller.loadSceneFile(sceneFilePath, sceneUpdates);
	}

	@Override public void onSceneReady(int sceneId, SceneError sceneError)
	{
		if (getActivity() != null)
		{
			retrievedTiles.clear();
			geometryLayer = controller.addDataLayer(GEOMETRY_LAYER);
			questsLayer = controller.addDataLayer(QUESTS_LAYER);
		}
		super.onSceneReady(sceneId, sceneError);
	}

	@Override public boolean onSingleTapUp(float x, float y)
	{
		return false;
	}

	@Override public boolean onSingleTapConfirmed(float x, float y)
	{

		if(controller != null) {
			onClickedMap(x, y);

			controller.pickLabel(x,y);
		}
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
			if (lastClickPos != null)
			{
				listener.onClickedMapAt(lastClickPos, lastFingerRadiusInMeters);
			}
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
		zoomBeforeShowingQuest = controller.getZoom();
		positionBeforeShowingQuest = controller.getPosition();

		float targetZoom = getMaxZoomThatContains(g);
		if(Float.isNaN(targetZoom) || targetZoom > MAX_QUEST_ZOOM)
		{
			targetZoom = MAX_QUEST_ZOOM;
		}
		else
		{
			// zoom out a bit
			targetZoom -= 0.4;
		}

		float currentZoom = controller.getZoom();

		controller.setZoom(targetZoom);
		LngLat pos = getCenterWithOffset(g);
		controller.setZoom(currentZoom);

		if(pos != null) controller.setPositionEased(pos, 500);
		controller.setZoomEased(targetZoom, 500);

		updateView();
	}

	private LngLat getCenterWithOffset(ElementGeometry geometry)
	{
		int w = getView().getWidth();
		int h = getView().getHeight();

		LngLat normalCenter = controller.screenPositionToLngLat(new PointF(w/2f, h/2f));

		LngLat offsetCenter = controller.screenPositionToLngLat(new PointF(
				questOffset.left + (w - questOffset.left - questOffset.right)/2,
				questOffset.top + (h - questOffset.top - questOffset.bottom)/2));

		if(normalCenter == null || offsetCenter == null) return null;

		LngLat pos = TangramConst.toLngLat(geometry.getCenter());
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
		LngLat pos = controller.screenPositionToLngLat(new PointF(positionX, positionY));
		if(pos != null) {
			float fingerSize = DpUtil.toPx(CLICK_AREA_SIZE_IN_DP, getContext())/2;
			LngLat fingerEdge = controller.screenPositionToLngLat(new PointF(positionX + fingerSize, positionY));
			if (fingerEdge != null)
			{
				LatLon clickPos = TangramConst.toLatLon(pos);
				LatLon fingerEdgePos = TangramConst.toLatLon(fingerEdge);
				double fingerRadiusInMeters = SphericalEarthMath.distance(clickPos, fingerEdgePos);
				lastClickPos = clickPos;
				lastFingerRadiusInMeters = fingerRadiusInMeters;
			}
		}
	}

	@Override protected boolean shouldCenterCurrentPosition()
	{
		// don't center position while displaying a quest
		return super.shouldCenterCurrentPosition() && zoomBeforeShowingQuest == null;
	}

	protected void updateView()
	{
		super.updateView();

		if (controller == null) return;

		if (!isShowingQuests) return;

		if(controller.getZoom() < TILES_ZOOM) return;

		LngLat positionNow = controller.getPosition();
		float tiltNow = controller.getTilt();
		float rotationNow = controller.getRotation();
		if(lastPos != null && lastTilt != null && lastRotation != null &&
			lastPos.equals(positionNow) && lastTilt == tiltNow && lastRotation == rotationNow) return;

		lastPos = positionNow;
		lastTilt = tiltNow;
		lastRotation = rotationNow;

		BoundingBox displayedArea = getDisplayedArea(new Rect());
		if(displayedArea == null) return;

		Rect tilesRect = SlippyMapMath.enclosingTiles(displayedArea, TILES_ZOOM);
		if(lastDisplayedRect != null && lastDisplayedRect.equals(tilesRect)) return;
		lastDisplayedRect = tilesRect;

		updateQuestsInRect(tilesRect);
	}

	private void updateQuestsInRect(Rect tilesRect)
	{
		// area too big -> skip ( see https://github.com/tangrams/tangram-es/issues/1492 )
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

	@UiThread public void addQuestGeometry(ElementGeometry g)
	{
		if(geometryLayer == null) return; // might still be null - async calls...
		if(!isShowingQuests) return;

		zoomAndMoveToContain(g);
		updateView();

		Map<String,String> props = new HashMap<>();

		if(g instanceof ElementPolygonsGeometry)
		{
			ElementPolygonsGeometry pg = (ElementPolygonsGeometry) g;
			props.put("type", "poly");
			geometryLayer.addPolygon(TangramConst.toLngLat(pg.getPolygons()), props);
		}
		else if(g instanceof ElementPolylinesGeometry)
		{
			ElementPolylinesGeometry pg = (ElementPolylinesGeometry) g;
			props.put("type", "line");
			List<List<LngLat>> polylines = TangramConst.toLngLat(pg.getPolylines());
			for(List<LngLat> polyline : polylines)
			{
				geometryLayer.addPolyline(polyline, props);
			}
		}
		else
		{
			props.put("type", "point");
			geometryLayer.addPoint(TangramConst.toLngLat(g.getCenter()), props);
		}
	}

	@UiThread public void removeQuestGeometry()
	{
		if(geometryLayer != null) geometryLayer.clear();
		if(controller != null)
		{
			for (Long markerId : markerIds.values())
			{
				controller.removeMarker(markerId);
			}
			markerIds.clear();
			if(zoomBeforeShowingQuest != null) controller.setZoomEased(zoomBeforeShowingQuest, 500);
			if(positionBeforeShowingQuest != null) controller.setPositionEased(positionBeforeShowingQuest, 500);
			zoomBeforeShowingQuest = null;
			positionBeforeShowingQuest = null;
			followPosition();
		}
	}

	@UiThread public void putMarkerForCurrentQuest(LatLon pos) {
		deleteMarkerForCurrentQuest(pos);
		if (controller != null) {
			Marker marker = controller.addMarker();
			marker.setDrawable(R.drawable.crosshair_marker);
			marker.setStylingFromString("{ style: 'points', color: 'white', size: 48px, order: 2000, collide: false }");
			marker.setPoint(TangramConst.toLngLat(pos));
			markerIds.put(pos, marker.getMarkerId());
		}
	}

	@UiThread public void deleteMarkerForCurrentQuest(LatLon pos) {
		if (controller != null) {
			Long markerId = markerIds.get(pos);
			if (markerId != null) {
				controller.removeMarker(markerId);
				markerIds.remove(pos);
			}
		}
	}

	private int getQuestPriority(Quest quest){
		// priority is decided by
		// - primarily by quest type to allow quest prioritization
		// - for quests of the same type - influenced by quest id,
		//   this is done to reduce chance that as user zoom in a quest disappears,
		//   especially in case where disappearing quest is one that user selected to solve

		// main priority part - values fit into Integer, but with as large steps as possible
		Integer order = questTypeOrder.get(quest.getType());
		if(order == null) order = 0;
		int freeValuesForEachQuest = Integer.MAX_VALUE / questTypeOrder.size();
		order *= freeValuesForEachQuest;

		// quest ID is used to add values unique to each quest to make ordering consistent
		// freeValuesForEachQuest is an int, so % freeValuesForEachQuest will fit into int
		int hopefullyUniqueValueForQuest = (int) (quest.getId() % freeValuesForEachQuest);

		return order + hopefullyUniqueValueForQuest;
	}

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

			// hack away cycleway quests for old Android SDK versions (#713)
			if(quest.getType() instanceof AddCycleway && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			{
				continue;
			}

			String questIconName = getActivity().getResources().getResourceEntryName(quest.getType().getIcon());

			LatLon[] positions = quest.getMarkerLocations();

			for (LatLon pos : positions)
			{
				if(first) first = false;
				else      geoJson.append(",");

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
				geoJson.append(getQuestPriority(quest));
				geoJson.append("\"}}");
			}
		}
		geoJson.append("]}");

		questsLayer.addGeoJson(geoJson.toString());
		controller.requestRender();
	}

	@UiThread
	public void removeQuests(Collection<Long> questIds, QuestGroup group)
	{
		// TODO: this method may also be called for quests that are not displayed on this map (anymore)

		if(questsLayer == null) return;
		// TODO (currently not possible with tangram, but it has been announced that this will soon
		// be added

		// so for now...:
		clearQuests();
		updateView();
	}

	private void clearQuests()
	{
		if(questsLayer != null) questsLayer.clear();
		retrievedTiles.clear();
		lastPos = null;
		lastTilt = null;
		lastRotation = null;
		lastDisplayedRect = null;
	}

	public void setIsShowingQuests(boolean showQuests)
	{
		if (isShowingQuests == showQuests) return;

		isShowingQuests = showQuests;
		if (!showQuests) {
			clearQuests();
		} else {
			updateView();
		}
	}


	public BoundingBox getDisplayedArea(Rect offset)
	{
		if(controller == null) return null;
		if(getView() == null) return null;
		Point size = new Point(
				getView().getWidth() - offset.left - offset.right,
				getView().getHeight() - offset.top - offset.bottom);
		if(size.equals(0,0)) return null;

		// the special cases here are: map tilt and map rotation:
		// * map tilt makes the screen area -> world map area into a trapezoid
		// * map rotation makes the screen area -> world map area into a rotated rectangle

		// dealing with tilt: this method is just not defined if the tilt is above a certain limit
		if(controller.getTilt() > Math.PI / 4f) return null; // 45°

		LngLat[] positions = new LngLat[4];
		positions[0] = getPositionAt(new PointF(offset.left,          offset.top));
		positions[1] = getPositionAt(new PointF(offset.left + size.x ,offset.top));
		positions[2] = getPositionAt(new PointF(offset.left,          offset.top + size.y));
		positions[3] = getPositionAt(new PointF(offset.left + size.x, offset.top + size.y));

		// dealing with rotation: find each the largest latlon and the smallest latlon, that'll
		// be our bounding box

		Double latMin = null, lonMin = null, latMax = null, lonMax = null;
		for (LngLat position : positions)
		{
			if(position == null) return null;
			double lat = position.latitude;
			double lon = position.longitude;

			if (latMin == null || latMin > lat) latMin = lat;
			if (latMax == null || latMax < lat) latMax = lat;
			if (lonMin == null || lonMin > lon) lonMin = lon;
			if (lonMax == null || lonMax < lon) lonMax = lon;
		}

		return new BoundingBox(latMin, lonMin, latMax, lonMax);
	}
}
