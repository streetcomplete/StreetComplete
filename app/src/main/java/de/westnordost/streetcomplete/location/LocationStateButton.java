package de.westnordost.streetcomplete.location;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import java.util.Arrays;

import de.westnordost.streetcomplete.R;

/**
 * An image button which shows the current location state
 */
public class LocationStateButton extends ImageButton
{
	// must be defined in the same order as the LocationState enum (but minus the first)
	private static final int[] STATES = {
			R.attr.state_allowed,
			R.attr.state_enabled,
			R.attr.state_searching,
			R.attr.state_updating,
	};

	// must also be defined in the same order as LocationState enum (but minus the first)
	private static final int[] STYLEABLES = {
			R.styleable.LocationStateButton_state_allowed,
			R.styleable.LocationStateButton_state_enabled,
			R.styleable.LocationStateButton_state_searching,
			R.styleable.LocationStateButton_state_updating,
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

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public LocationStateButton(Context context, AttributeSet attrs, int defStyle, int defStyleRes)
	{
		super(context, attrs, defStyle, defStyleRes);

		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.LocationStateButton,
				defStyle, defStyleRes);
		state = determineStateFrom(a);
		a.recycle();
		refreshDrawableState();
	}

	private LocationState determineStateFrom(TypedArray a)
	{
		for(int i=STYLEABLES.length-1; i>=0; --i)
		{
			if(a.getBoolean(STYLEABLES[i], false)) return LocationState.values()[i+1];
		}
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
		final int[] drawableState = super.onCreateDrawableState(extraSpace + STATES.length);
		int arrPos = getState().ordinal();
		if (arrPos > 0)
		{
			mergeDrawableStates(drawableState, Arrays.copyOf(STATES, arrPos));
		}
		return drawableState;
	}

	@Override
	public Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.state = getState();
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable s)
	{
		SavedState ss = (SavedState) s;
		super.onRestoreInstanceState(ss.getSuperState());
		this.state = ss.state;
		requestLayout();
	}

	static class SavedState extends BaseSavedState
	{
		LocationState state;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in)
		{
			super(in);
			state = LocationState.valueOf(in.readString());
		}

		@Override public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeString(state.name());
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