package de.westnordost.streetcomplete.quests.localized_name.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;

public class PutRoadNameSuggestionsHandler implements MapDataWithGeometryHandler
{
	private static Pattern NAME_PATTERN = Pattern.compile("name(:(.*))?");

	private final RoadNameSuggestionsDao roadNameSuggestionsDao;

	@Inject public PutRoadNameSuggestionsHandler(RoadNameSuggestionsDao roadNameSuggestionsDao)
	{
		this.roadNameSuggestionsDao = roadNameSuggestionsDao;
	}

	@Override
	public void handle(@NonNull Element element, @Nullable ElementGeometry geometry)
	{
		if(element.getType() != Element.Type.WAY) return;
		if(geometry == null || geometry.polylines == null || geometry.polylines.isEmpty()) return;

		List<LatLon> points = geometry.polylines.get(0);

		roadNameSuggestionsDao.putRoad(
				element.getId(),
				toRoadNameByLanguage(element.getTags()),
				new ArrayList<>(points));
	}

	private static HashMap<String,String> toRoadNameByLanguage(Map<String,String> osmTags)
	{
		if(osmTags == null) return null;
		HashMap<String,String> result = new HashMap<>();
		for (Map.Entry<String, String> osmTag : osmTags.entrySet())
		{
			Matcher m = NAME_PATTERN.matcher(osmTag.getKey());
			if(m.matches())
			{
				String languageCode = m.group(2);
				if(languageCode == null) languageCode = "";
				result.put(languageCode, osmTag.getValue());
			}
		}
		if(result.isEmpty()) return null;
		return result;
	}
}
