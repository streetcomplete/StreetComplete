package de.westnordost.streetcomplete.data.meta;

public class GeoJsonException extends RuntimeException
{
	public GeoJsonException(Throwable cause) { super(cause); }
	public GeoJsonException(String message) { super(message); }
	public GeoJsonException(String message, Throwable cause) { super(message, cause); }
}
