package de.westnordost.streetcomplete.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.inject.Singleton;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Fixed1E7LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.osmapi.user.User;
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction;
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitAtLinePosition;
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitAtPoint;
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.RevertUpdateElementTagsAction;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.SpatialPartsOfElement;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.SpatialPartsOfNode;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.SpatialPartsOfRelation;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.SpatialPartsOfWay;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify;
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction;
import de.westnordost.streetcomplete.quests.LocalizedName;
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OffDaysRow;
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow;
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow;
import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection;
import de.westnordost.streetcomplete.quests.opening_hours.model.Months;
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange;
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays;
import de.westnordost.streetcomplete.quests.postbox_collection_times.WeekdaysTimesRow;

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
			WeekdaysTimesRow.class,
			OsmLatLon.class,
			SplitAtPoint.class,
			SplitAtLinePosition.class,
			OffDaysRow.class,
			Months.class,
			DeletePoiNodeAction.class,
			SplitWayAction.class,
			RevertUpdateElementTagsAction.class,
			UpdateElementTagsAction.Serializable.class,
			SpatialPartsOfElement.class,
			SpatialPartsOfNode.class,
			SpatialPartsOfWay.class,
			SpatialPartsOfRelation.class
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
		try (Output output = new Output(1024, -1))
		{
			kryo.get().writeObject(output, object);
			return output.toBytes();
		}
	}

	@Override public <T> T toObject(byte[] bytes, Class<T> type)
	{
		try (Input input = new Input(bytes))
		{
			return kryo.get().readObject(input, type);
		}
	}
}
