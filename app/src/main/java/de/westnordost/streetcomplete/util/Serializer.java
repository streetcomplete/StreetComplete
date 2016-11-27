package de.westnordost.streetcomplete.util;

public interface Serializer
{
	byte[] toBytes(Object object);
	<T> T toObject(byte[] bytes, Class<T> type);
}
