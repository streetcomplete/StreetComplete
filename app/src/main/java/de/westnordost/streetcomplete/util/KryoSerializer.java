package de.westnordost.streetcomplete.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.inject.Singleton;

import de.westnordost.osmapi.map.data.Fixed1E7LatLon;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify;
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow;
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow;
import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.osmapi.user.User;
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange;
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays;
import de.westnordost.streetcomplete.quests.localized_name.LocalizedName;
import de.westnordost.streetcomplete.quests.postbox_collection_times.WeekdaysTimes;

@Singleton
public class KryoSerializer implements Serializer
{
	// NEVER CHANGE THE ORDER OF THIS LIST. ALWAYS APPEND NEW CLASSES AT THE BOTTOM
	// IF CLASSES ARE DELETED, INSERT A PLACEHOLDER (i.e. Object.class) THERE
	private static final Class[] registeredClasses =
	{
			HashMap.class,
			ArrayList.class,
			Fixed1E7LatLon.class,
			Element.Type.class,
			OsmRelationMember.class,
			StringMapChanges.class,
			StringMapEntryAdd.class,
			StringMapEntryDelete.class,
			StringMapEntryModify.class,
			NoteComment.class,
			NoteComment.Action.class,
			Date.class,
			User.class,
			CircularSection.class,
			TimeRange.class,
			Weekdays.class,
			boolean[].class,
			OpeningMonthsRow.class,
			OpeningWeekdaysRow.class,
			LocalizedName.class,
			WeekdaysTimes.class,
			OsmLatLon.class,
	};


	private static final ThreadLocal<Kryo> kryo = new ThreadLocal<Kryo>()
	{
		@Override protected Kryo initialValue()
		{
			Kryo kryo = new Kryo();

			/* Kryo docs say that classes that are registered are serialized more space efficiently
	 		  (so it is not necessary that all classes that are serialized are registered here, but
	 		   it is better) */
			kryo.setRegistrationRequired(true);
			kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
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
