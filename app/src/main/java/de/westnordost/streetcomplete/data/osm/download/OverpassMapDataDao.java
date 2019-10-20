package de.westnordost.streetcomplete.data.osm.download;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmapi.ApiRequestWriter;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.common.errors.OsmApiException;
import de.westnordost.osmapi.common.errors.OsmBadUserInputException;

/** Get map data from overpass api */
public class OverpassMapDataDao
{
	private static final String TAG = "OverpassMapDataDao";

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
	public synchronized void get(final String query, MapDataWithGeometryHandler handler)
	{
		OverpassMapDataParser parser = parserProvider.get();
		parser.setHandler(handler);
		try
		{
			ApiRequestWriter writer = new ApiRequestWriter()
			{
				@Override public String getContentType()
				{
					return "application/x-www-form-urlencoded";
				}

				@Override public void write(OutputStream out) throws IOException
				{
					String request = "data=" + urlEncode(query);
					out.write(request.getBytes("UTF-8"));
				}
			};
			osm.makeRequest("interpreter", "POST", false, writer, parser);
		}
		catch(OsmApiException e)
		{
			if(e.getErrorCode() == 429)
				throw new OsmTooManyRequestsException(e.getErrorCode(), e.getErrorTitle(), e.getDescription());
			else
				throw e;
		}
	}

	/** Same as get(String, MapDataWithGeometryHandler), only that it automatically waits until the
	 *  app is allowed to do requests again by request quota if it hits the request quota.

	 * @param query Query string. Either Overpass QL or Overpass XML query string
	 * @param handler map data handler that is fed the map data and geometry
	 * @return false if it was interrupted while waiting for the quota to be replenished
	 *
	 * @throws OsmBadUserInputException if there is an error if the query
	 */
	public synchronized boolean getAndHandleQuota(String query, MapDataWithGeometryHandler handler)
	{
		try
		{
			get(query, handler);
		}
		catch(OsmTooManyRequestsException e)
		{
			OverpassStatus status = getStatus();
			if(status.availableSlots == 0)
			{
				// apparently sometimes Overpass does not tell the client when the next slot is
				// available when there is currently no slot available. So let's just wait 60s
				// before trying again
				// also, rather wait 1s longer than required cause we only get the time in seconds
				int waitInSeconds =
					status.nextAvailableSlotIn != null ? status.nextAvailableSlotIn + 1: 60;
				Log.i(TAG, "Hit Overpass quota. Waiting " + waitInSeconds + "s before continuing");
				try
				{
					Thread.sleep(waitInSeconds * 1000);
				}
				catch (InterruptedException ie)
				{
					Log.d(TAG, "Thread interrupted while waiting for Overpass quota to be replenished");
					return false;
				}
			}
			return getAndHandleQuota(query, handler);
		}
		return true;
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
