package de.westnordost.streetcomplete.data.osm.download;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.errors.OsmApiException;
import de.westnordost.osmapi.common.errors.OsmBadUserInputException;

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
	 * @param query Query string. Either Overpass QL or Overpass XML query string
	 * @param handler map data handler that is fed the map data and geometry
	 *
	 * @throws OsmTooManyRequestsException if the user is over his request quota. See getStatus, killMyQueries
	 * @throws OsmBadUserInputException if there is an error if the query
	 */
	public synchronized void get(String query, MapDataWithGeometryHandler handler)
	{
		String request = "interpreter?data=" + urlEncode(query);
		OverpassMapDataParser parser = parserProvider.get();
		parser.setHandler(handler);
		try
		{
			osm.makeRequest(request, parser);
		}
		catch(OsmApiException e)
		{
			if(e.getErrorCode() == 429)
				throw new OsmTooManyRequestsException(e.getErrorCode(), e.getErrorTitle(), e.getDescription());
			else
				throw e;
		}
	}

	/** Kills all the queries sent from this IP. Useful if there is a runaway query that takes far
	 *  too much time and blocks the user from making any more queries */
	public void killMyQueries()
	{
		osm.makeRequest("kill_my_queries", null);
	}

	/** Get info about how many queries the user may make until reaching his quota */
	public OverpassStatus getStatus()
	{
		return osm.makeRequest("status", new OverpassStatusParser());
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
