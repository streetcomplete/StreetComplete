package de.westnordost.osmagent.tangram;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

import de.westnordost.osmagent.R;

/**
 * An image button with two states
 */
public class ToggleImageButton extends ImageButton implements Checkable
{
	private OnCheckedChangeListener onCheckedChangeListener;
	private boolean checked;
	private boolean isBroadcasting;

	public ToggleImageButton(Context context)
	{
		this(context, null);
	}

	public ToggleImageButton(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ToggleImageButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToggleImageButton);
		boolean checked = a.getBoolean( R.styleable.ToggleImageButton_checked, false);
		setChecked(checked);
		a.recycle();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ToggleImageButton(Context context, AttributeSet attrs, int defStyle, int defStyleRes)
	{
		super(context, attrs, defStyle, defStyleRes);

		TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.ToggleImageButton, defStyle, defStyleRes);
		boolean checked = a.getBoolean( R.styleable.ToggleImageButton_checked, false);
		setChecked(checked);
		a.recycle();
	}

	private static final int[] CheckedStateSet = { android.R.attr.state_checked };

	@Override
	public int[] onCreateDrawableState(int extraSpace)
	{
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked())
		{
			mergeDrawableStates(drawableState, CheckedStateSet);
		}
		return drawableState;
	}

	@Override
	public boolean isChecked()
	{
		return checked;
	}

	@Override
	public void setChecked(boolean checked)
	{
		if(this.checked != checked)
		{
			this.checked = checked;
			refreshDrawableState();

			// Avoid infinite recursions if setChecked() is called from a listener
			if (isBroadcasting) return;

			isBroadcasting = true;
			if (onCheckedChangeListener != null)
			{
				onCheckedChangeListener.onCheckedChanged(this, checked);
			}
			isBroadcasting = false;
		}
	}

	@Override
	public void toggle()
	{
		setChecked(!isChecked());
	}

	@Override
	public boolean performClick()
	{
		toggle();
		return super.performClick();
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener)
	{
		this.onCheckedChangeListener = onCheckedChangeListener;
	}

	public interface OnCheckedChangeListener
	{
		void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked);
	}

	static class SavedState extends BaseSavedState
	{
		boolean checked;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in)
		{
			super(in);
			checked = (Boolean) in.readValue(null);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeValue(checked);
		}

		@Override
		public String toString() {
			return "ToggleImageButton.SavedState{"
					+ Integer.toHexString(System.identityHashCode(this))
					+ " checked=" + checked + "}";
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

	@Override
	public Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);

		ss.checked = isChecked();
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state)
	{
		SavedState ss = (SavedState) state;

		super.onRestoreInstanceState(ss.getSuperState());
		checked = ss.checked;
		requestLayout();
	}
}