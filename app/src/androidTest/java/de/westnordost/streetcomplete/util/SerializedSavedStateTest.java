package de.westnordost.streetcomplete.util;

import android.os.Parcel;
import android.view.View;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class SerializedSavedStateTest
{
	@Test public void writeRead()
	{
		HashMap<String,String> data = new HashMap<>();
		data.put("A","B");
		data.put("C","D");
		SerializedSavedState savedState = new SerializedSavedState(View.BaseSavedState.EMPTY_STATE, data);
		savedState.serializer = new KryoSerializer();
		assertEquals(data, savedState.get(HashMap.class));

		Parcel parcel = Parcel.obtain();
		savedState.writeToParcel(parcel,0);

		// rewind parcel so that we can read it from pos 0. Could also marshal and then unmarshal,
		// but lets include as little functionality outside of our class as possible
		parcel.setDataPosition(0);

		SerializedSavedState savedState2 = SerializedSavedState.CREATOR.createFromParcel(parcel);
		savedState.serializer = new KryoSerializer();
		assertEquals(data, savedState2.get(HashMap.class));
	}
}
