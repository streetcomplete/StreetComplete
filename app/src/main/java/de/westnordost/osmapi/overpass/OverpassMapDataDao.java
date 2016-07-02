package de.westnordost.osmapi.overpass;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.MapDataParser;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.map.handler.MapDataHandler;

/** Get map data from overpass api */
public class OverpassMapDataDao
{
	private final OsmConnection osm;
	private final MapDataFactory factory;

	public OverpassMapDataDao(OsmConnection osm, MapDataFactory factory)
	{
		this.osm = osm;
		this.factory = factory;
	}

	public OverpassMapDataDao(OsmConnection osm)
	{
		this(osm, new OsmMapDataFactory());
	}

	/**
	 * Feeds map data to the given MapDataHandler.
	 *
	 * @param oql Query string. Either Overpass QL or Overpass XML query string
	 * @param handler map data handler that is fed the map data
	 */
	public void get(String oql, MapDataHandler handler)
	{
		String request = "interpreter?data=" + urlEncode(oql);
		osm.makeRequest(request, new MapDataParser(handler, factory));
	}

	private String urlEncode(String text)
	{
		try
		{
			return URLEncoder.encode(text, OsmConnection.CHARSET);
		}
		catch (UnsupportedEncodingException e)
		{
			// should never happen since we use UTF-8
			throw new RuntimeException(e);
		}
	}
}
