package de.westnordost.osmagent.data.osm.download;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmapi.OsmConnection;

/** Get map data from overpass api */
public class OverpassMapDataDao
{
	private final OsmConnection osm;
	private final Provider<OverpassMapDataParser> parserProvider;

	@Inject public OverpassMapDataDao(OsmConnection osm, Provider<OverpassMapDataParser> parserProvider)
	{
		this.osm = osm;
		this.parserProvider = parserProvider;
	}

	/**
	 * Feeds map data to the given MapDataWithGeometryHandler.
	 *
	 * @param oql Query string. Either Overpass QL or Overpass XML query string
	 * @param handler map data handler that is fed the map data and geometry
	 */
	public synchronized void get(String oql, MapDataWithGeometryHandler handler)
	{
		String request = "interpreter?data=" + urlEncode(oql);
		OverpassMapDataParser parser = parserProvider.get();
		parser.setHandler(handler);
		osm.makeRequest(request, parser);
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
