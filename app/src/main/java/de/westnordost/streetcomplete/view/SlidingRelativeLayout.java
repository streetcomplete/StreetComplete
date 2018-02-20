package de.westnordost.streetcomplete.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class SlidingRelativeLayout extends RelativeLayout
{
	private float yFraction = 0;
	private float xFraction = 0;

	public SlidingRelativeLayout(Context context)
	{
		super(context);
	}

	public SlidingRelativeLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public SlidingRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	private ViewTreeObserver.OnPreDrawListener preDrawListener = null;

	public void setYFraction(float fraction)
	{
		this.yFraction = fraction;
		if (getHeight() == 0) {
			if (preDrawListener == null) {
				preDrawListener = ()-> {
					getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
					setYFraction(yFraction);
					return true;
				};
				getViewTreeObserver().addOnPreDrawListener(preDrawListener);
			}
			return;
		}
		float translationY = getHeight() * fraction;
		setTranslationY(translationY);
	}

	public void setXFraction(float fraction)
	{
		this.xFraction = fraction;
		if (getWidth() == 0) {
			if (preDrawListener == null) {
				preDrawListener = ()-> {
					getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
					setXFraction(xFraction);
					return true;
				};
				getViewTreeObserver().addOnPreDrawListener(preDrawListener);
			}
			return;
		}
		float translationX = getWidth() * fraction;
		setTranslationX(translationX);
	}
}
