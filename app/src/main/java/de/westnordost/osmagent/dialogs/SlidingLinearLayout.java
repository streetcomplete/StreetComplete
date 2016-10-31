package de.westnordost.osmagent.dialogs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SlidingLinearLayout extends LinearLayout
{
	public SlidingLinearLayout(Context context)
	{
		super(context);
	}

	public SlidingLinearLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public SlidingLinearLayout(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public void setYFraction(float fraction)
	{
		// don't display if layout has not been measured yet
		setVisibility(getHeight() == 0 ? View.INVISIBLE : View.VISIBLE);

		float translationY = getHeight() * fraction;
		setTranslationY(translationY);
	}

	public float getYFraction()
	{
		if(getHeight() == 0) return 0;
		return getTranslationY() / getHeight();
	}
}
