package de.westnordost.osmagent.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

import javax.inject.Inject;

import de.westnordost.osmagent.Injector;

// TODO test
/** Saved states made easy ... :-P */
public class KryoSavedState extends View.BaseSavedState
{
	@Inject Serializer serializer;
	private Object object;
	private Parcel source;

	public KryoSavedState(Parcelable superState, @NonNull Object object)
	{
		super(superState);
		Injector.instance.getApplicationComponent().inject(this);
		this.object = object;
	}

	private KryoSavedState(@NonNull Parcel source)
	{
		super(source);
		Injector.instance.getApplicationComponent().inject(this);
		this.source = source;
	}

	public <T> T get(Class<T> tClass)
	{
		// Android does not necessarily write the object to parcel but reuses the object created for
		// saving instance state also for restoring
		if(object != null) return (T) object;
		return serializer.toObject(source.createByteArray(), tClass);
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		dest.writeByteArray(serializer.toBytes(object));
	}

	public static final Creator<KryoSavedState> CREATOR = new Creator<KryoSavedState>()
	{
		@Override public KryoSavedState createFromParcel(Parcel source)
		{
			return new KryoSavedState(source);
		}

		@Override public KryoSavedState[] newArray(int size)
		{
			return new KryoSavedState[size];
		}
	};
}
