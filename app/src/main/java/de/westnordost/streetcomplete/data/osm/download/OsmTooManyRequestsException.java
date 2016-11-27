package de.westnordost.streetcomplete.data.osm.download;

import de.westnordost.osmapi.common.errors.OsmApiException;

public class OsmTooManyRequestsException extends OsmApiException
{
	private static final long serialVersionUID = 1L;

	public OsmTooManyRequestsException(Throwable cause)
	{
		super(cause);
	}

	public OsmTooManyRequestsException(int errorCode, String errorTitle, String description)
	{
		super(errorCode, errorTitle, description);
	}
}
