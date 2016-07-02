package de.westnordost.osmagent;

import de.westnordost.osmapi.OsmConnection;
import oauth.signpost.OAuthConsumer;

public class OsmFactory
{
	public static String OSM_API_URL = "https://api.openstreetmap.org/api/0.6/";
	public static String USER_AGENT = "osmagent 1.0";

	public static OsmConnection createConnection(OAuthConsumer consumer)
	{
		return new OsmConnection(OSM_API_URL,USER_AGENT,consumer);
	}


}
