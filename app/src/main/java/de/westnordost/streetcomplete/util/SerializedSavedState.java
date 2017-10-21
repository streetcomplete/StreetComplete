package de.westnordost.streetcomplete.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

import javax.inject.Inject;

import de.westnordost.streetcomplete.ApplicationComponent;
import de.westnordost.streetcomplete.Injector;

/** Saved states made easy ... :-P */
public class SerializedSavedState extends View.BaseSavedState
{
	@Inject Serializer serializer;

	private final Object data;
	private final Parcel source;

	public SerializedSavedState(Parcelable superState, @NonNull Object data)
	{
		super(superState);
		inject();
		this.data = data;
		this.source = null;
	}

	private SerializedSavedState(@NonNull Parcel source)
	{
		super(source);
		inject();
		this.source = source;
		this.data = null;
	}

	private void inject()
	{
		ApplicationComponent component = Injector.instance.getApplicationComponent();
		// the component is null when testing
		if(component != null) component.inject(this);
	}

	public Object get(Class<?> tClass)
	{
		// Android does not necessarily write the data to parcel but may reuse the data created
		// for saving instance state also for restoring. In this case, source is null and data is
		// defined
		if(data != null) return data;
		return serializer.toObject(source.createByteArray(), tClass);
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		dest.writeByteArray(serializer.toBytes(data));
	}

	public static final Creator<SerializedSavedState> CREATOR = new Creator<SerializedSavedState>()
	{
		@Override public SerializedSavedState createFromParcel(Parcel source)
		{
			return new SerializedSavedState(source);
		}

		@Override public SerializedSavedState[] newArray(int size)
		{
			return new SerializedSavedState[size];
		}
	};
}
