package de.westnordost.streetcomplete.util;

import androidx.annotation.NonNull;

public interface Serializer
{
	@NonNull
	byte[] toBytes(Object object);

	<T> T toObject(byte[] bytes, Class<T> type);
}
