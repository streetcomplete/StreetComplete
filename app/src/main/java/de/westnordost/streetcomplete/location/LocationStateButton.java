package de.westnordost.streetcomplete.location;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;

/**
 * An image button which shows the current location state
 */
public class LocationStateButton extends androidx.appcompat.widget.AppCompatImageButton
{
	// must be defined in the same order as the LocationState enum (but minus the first)
	private static final int[] STATES = {
			R.attr.state_allowed,
			R.attr.state_enabled,
			R.attr.state_searching,
			R.attr.state_updating,
	};

	private LocationState state;

	private ColorStateList tint;

	public LocationStateButton(Context context)
	{
		this(context, null);
	}

	public LocationStateButton(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public LocationStateButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LocationStateButton);
		state = determineStateFrom(a);
		tint = a.getColorStateList(R.styleable.LocationStateButton_tint);
		a.recycle();
	}

	private LocationState determineStateFrom(TypedArray a)
	{
		if (a.getBoolean(R.styleable.LocationStateButton_state_updating, false)) return LocationState.UPDATING;
		if (a.getBoolean(R.styleable.LocationStateButton_state_searching, false)) return LocationState.SEARCHING;
		if (a.getBoolean(R.styleable.LocationStateButton_state_enabled, false)) return LocationState.ENABLED;
		if (a.getBoolean(R.styleable.LocationStateButton_state_allowed, false)) return LocationState.ALLOWED;
		return LocationState.DENIED;
	}

	@Override protected void drawableStateChanged()
	{
		super.drawableStateChanged();
		// autostart
		if(getDrawable().getCurrent() instanceof Animatable)
		{
			Animatable animatable = (Animatable) getDrawable().getCurrent();
			if(!animatable.isRunning()) animatable.start();
		}
		if (tint != null && tint.isStateful())
		{
			setColorFilter(tint.getColorForState(getDrawableState(), 0));
		}
	}

	public void setState(LocationState state)
	{
		if(state == this.state) return;
		this.state = state;
		refreshDrawableState();
	}

	public LocationState getState()
	{
		if(state == null) return LocationState.DENIED;
		return state;
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace)
	{
		int additionalLength = STATES.length + 1;
		final int[] drawableState = super.onCreateDrawableState(extraSpace + additionalLength);
		int arrPos = getState().ordinal();
		int[] additionalArray = Arrays.copyOf(Arrays.copyOf(STATES, arrPos), additionalLength);
		mergeDrawableStates(drawableState, additionalArray);
		return drawableState;
	}

	@Override
	public Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.state = getState();
		ss.activated = isActivated();
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable s)
	{
		SavedState ss = (SavedState) s;
		super.onRestoreInstanceState(ss.getSuperState());
		this.state = ss.state;
		setActivated(ss.activated);
		requestLayout();
	}

	static class SavedState extends BaseSavedState
	{
		LocationState state;
		boolean activated;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in)
		{
			super(in);
			state = LocationState.valueOf(in.readString());
			activated = in.readInt() == 1;
		}

		@Override public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeString(state.name());
			out.writeInt(activated ? 1 : 0);
		}

		public static final Parcelable.Creator<SavedState> CREATOR
				= new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
