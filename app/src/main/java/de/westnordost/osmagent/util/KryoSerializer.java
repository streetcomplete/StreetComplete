package de.westnordost.osmagent.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class KryoSerializer implements Serializer
{
	// Kryo docs say that classes that are registered are serialized more space efficiently
	// (so it is not necessary that all classes that are serialized are registered here, but it is better)
	private static final Class[] registeredClasses =
	{
	//	StringMapEntryChange.class,
	//	StringMapChanges.class,
	//	OsmLatLon.class,
	//	ElementGeometry.class,
			// TODO
	};

	private static final ThreadLocal<Kryo> kryo = new ThreadLocal<Kryo>()
	{
		@Override protected Kryo initialValue()
		{
			Kryo kryo = new Kryo();
			for(Class reg : registeredClasses)
			{
				kryo.register(reg);
			}
			return kryo;
		}
	};

	@Override public byte[] toBytes(Object object)
	{
		Output output = new Output(1024,-1);
		kryo.get().writeObject(output, object);
		output.close();
		return output.toBytes();
	}

	@Override public <T> T toObject(byte[] bytes, Class<T> type)
	{
		Input input = new Input(bytes);
		T result = kryo.get().readObject(input, type);
		input.close();
		return result;
	}
}
